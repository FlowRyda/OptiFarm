import {Component, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Subject, takeUntil} from "rxjs";
import {CoordinateDto, FieldCreateDto, FieldDetailDto, FieldValidationRequestDto} from "../../../dtos/field";
import {FieldService} from "../../../services/field.service";
import {AuthService} from "../../../services/auth.service";
import {MapComponent} from "../../shared/map/map.component";
import {ActionButton, BackAction} from "../../shared/panel-layout/data";
import {DrawingService} from "../../../services/drawing.service";
import * as L from 'leaflet';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {LocatedObjectService} from "../../../services/located-object.service";
import {ToastrService} from "ngx-toastr";
import {MapColors} from "../../../constants/map-colors";

@Component({
  selector: 'app-field-create-edit',
  standalone: false,
  templateUrl: './field-create-edit.component.html',
  styleUrl: './field-create-edit.component.scss'
})
export class FieldCreateEditComponent implements OnInit, OnDestroy {
  public createEditForm: FormGroup;

  mode: 'create' | 'edit' = 'create';
  fieldId?: number;
  field?: FieldDetailDto;
  isLoading: boolean = false;
  actionButtons: ActionButton[] = [];
  globalErrorMessage: string | null = null;
  showCancelDialog = false;
  markerCount: number = 0;
  showDeleteDialog = false;
  overlapGeometry: L.LatLng[] | null = null;
  overlappingFieldName: string | null = null;

  editPhase: 'outer' | 'exclusion' = 'outer';
  isInManagementView: boolean = false;
  lockedOuterBoundary: L.LatLng[] | null = null;
  lockedOuterPolygon?: L.Polygon;
  exclusions: L.LatLng[][] = [];
  lockedExclusionPolygons: L.Polygon[] = [];
  currentExclusionIndex: number = -1;
  previousLockedOuterBoundary: L.LatLng[] | null = null;

  private get hasUnlockedBoundary(): boolean {
    return this.editPhase === 'outer' && this.markerCount >= 3 && !this.lockedOuterBoundary;
  }

  private get isDrawingExclusion(): boolean {
    return this.currentExclusionIndex >= 0 && this.markerCount > 0;
  }

  private get hasUnsavedDrawing(): boolean {
    return this.hasUnlockedBoundary || this.isDrawingExclusion;
  }

  showUnsavedDrawingDialog = false;

  showOuterShortcuts = false;
  showManagementShortcuts = false;
  showExclusionShortcuts = false;

  private destroy$ = new Subject<void>();
  private overlapPolygon?: L.Polygon;

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  constructor(
    private readonly fb: FormBuilder,
    private readonly fieldService: FieldService,
    public authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly drawingService: DrawingService,
    private readonly locatorService: LocatedObjectService,
    private readonly toastr: ToastrService
  ) {
    this.subscribeToDrawingState();
  }

  ngOnInit(): void {
    this.createEditForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1)]],
    });


    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.mode = 'edit';
      this.fieldId = +id;
      this.loadField();
    }
    this.updateActionButtons();
  }

  private updateActionButtons(): void {
    this.actionButtons = [
      {
        label: this.submitButtonLabel,
        color: 'primary',
        action: () => this.onSubmit()
      }
    ];

    if (this.mode === 'edit') {
      this.actionButtons.push({
        label: 'Delete Field',
        color: 'warn',
        action: () => this.onDelete()
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.drawingService.clearBoundary();
    this.clearOverlapVisualization();
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardShortcuts(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') {
      return;
    }

    if (this.showCancelDialog || this.showDeleteDialog || this.showUnsavedDrawingDialog) {
      return;
    }

    switch (event.key) {
      case 'Escape':
        event.preventDefault();
        this.handleEscapeKey();
        break;

      case 'Enter':
      case ' ':
        event.preventDefault();
        this.handleEnterKey();
        break;

      case 'Backspace':
      case 'Delete':
        if (!this.isInManagementView && this.markerCount > 0) {
          event.preventDefault();
          this.removeLastPoint();
        }
        break;

      case 'c':
      case 'C':
        if (!this.isInManagementView && this.markerCount > 0) {
          event.preventDefault();
          this.onClearBoundary();
        }
        break;
    }
  }

  private handleEscapeKey(): void {
    if (this.currentExclusionIndex >= 0) {
      this.cancelExclusionDrawing();
      return;
    }

    if (this.editPhase === 'outer' && this.previousLockedOuterBoundary) {
      this.mapComponent.clearBoundary();

      this.lockedOuterBoundary = this.previousLockedOuterBoundary;
      this.previousLockedOuterBoundary = null;

      this.redrawOuterBoundary();

      if (this.exclusions.length > 0) {
        this.redrawLockedExclusions();
      }

      this.editPhase = 'exclusion';
      this.isInManagementView = true;
      this.mapComponent.setExclusionMode(false);
      this.clearOverlapVisualization()
      this.toastr.info('Edit cancelled, boundary restored', '', {timeOut: 2000});
      return;
    }

    if (this.editPhase === 'outer' && this.markerCount > 0 && !this.lockedOuterBoundary) {
      this.onClearBoundary();
    }
  }

  private handleEnterKey(): void {
    if (this.isInManagementView && this.editPhase === 'exclusion' && this.currentExclusionIndex === -1) {
      this.addNewExclusion();
      return;
    }

    if (this.editPhase === 'outer' && this.markerCount >= 3 && !this.lockedOuterBoundary) {
      this.lockOuterBoundary();
      return;
    }

    if (this.currentExclusionIndex >= 0 && this.markerCount >= 3) {
      this.lockExclusion();
      return;
    }
  }

  private removeLastPoint(): void {
    if (!this.mapComponent || this.markerCount === 0) return;

    const coords = this.drawingService.getCurrentCoordinates();
    if (coords.length === 0) return;

    const newCoords = coords.slice(0, -1);
    this.mapComponent.clearBoundary();

    if (newCoords.length > 0) {
      this.mapComponent.initializeWithCoordinates(newCoords);
    }

    this.toastr.info('Last point removed', '', {timeOut: 1500});
  }


  onMapReady(map: MapComponent) {
    this.mapComponent = map;

    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        const filteredObjects = this.fieldId
          ? objects.filter(obj => !('fieldId' in obj) || obj.fieldId !== this.fieldId)
          : objects;

        this.mapComponent.drawLocatedObjects(filteredObjects);
      },
      error: (error) => {
        console.error('Error retrieving fields:', error);
        this.toastr.error('Failed to load map objects', 'Error');
      }
    })
  }

  get pageTitle(): string {
    if (this.mode === 'create') {
      return 'Add Field';
    }
    return `Edit ${this.field?.name || 'Field'}`;
  }

  get pageSubtitle(): string {
    if (this.mode === 'create') {
      return 'Creating a new Field for the Farm';
    }
    return 'Editing a field for the farm';
  }

  get submitButtonLabel(): string {
    if (this.mode === 'create') {
      return 'Add Field';
    }
    return 'Save changes';
  }

  get backActionConfig(): BackAction {
    return {
      action: () => this.cancel()
    };
  }

  cancel(): void {
    if (this.hasUnsavedDrawing) {
      this.showUnsavedDrawingDialog = true;
      return;
    }

    if (!this.createEditForm.dirty &&
      !this.lockedOuterBoundary &&
      this.exclusions.length === 0) {
      this.navigateBack();
      return;
    }

    this.showCancelDialog = true;
  }

  get shouldEnableDrawing(): boolean {
    if (this.isInManagementView) {
      return false;
    }

    if (this.editPhase === 'outer' && !this.lockedOuterBoundary) {
      return true;
    }

    if (this.currentExclusionIndex >= 0) {
      return true;
    }

    return false;
  }

  confirmDiscardChanges(): void {
    this.showCancelDialog = false;
    this.navigateBack();
  }

  navigateBack(): void {
    this.router.navigate(['/map-overview']);
  }

  keepEditing(): void {
    this.showCancelDialog = false;
  }

  onUnsavedDrawingConfirmed(): void {
    this.showUnsavedDrawingDialog = false;
    this.mapComponent.clearBoundary();
    if (this.isDrawingExclusion) {
      this.cancelExclusionDrawing();
    }
    this.navigateBack();
  }

  onUnsavedDrawingCancelled(): void {
    this.showUnsavedDrawingDialog = false;
  }

  isFormValid(): boolean {
    const nameValid = this.createEditForm.get('name')?.valid ?? false;
    const boundariesValid = this.lockedOuterBoundary
      ? this.lockedOuterBoundary.length >= 3
      : this.markerCount >= 3;
    return nameValid && boundariesValid;
  }

  get nameError(): string | null {
    const nameControl = this.createEditForm.get('name');
    if (nameControl?.hasError('required') && nameControl.touched) {
      return 'Field name is required';
    }
    return null;
  }

  get boundariesError(): string | null {
    if (this.markerCount === 0) {
      return 'Please draw the field boundaries on the map';
    }
    if (this.markerCount > 0 && this.markerCount < 3) {
      return `At least 3 points required (currently: ${this.markerCount})`;
    }
    return null;
  }

  private subscribeToDrawingState(): void {
    this.drawingService.markerCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.markerCount = count;
      });
  }

  private loadField(): void {
    if (!this.fieldId) return;

    this.isLoading = true;
    this.fieldService.getFieldById(this.fieldId).subscribe({
      next: (field: FieldDetailDto) => {
        this.field = field;

        this.createEditForm.patchValue({
          name: field.name || ''
        });

        if (field.boundaries && field.boundaries.length >= 3) {
          const latlngs = field.boundaries.map(coord =>
            L.latLng(coord.lon, coord.lat)
          );

          this.lockedOuterBoundary = latlngs;
          this.isInManagementView = true;
          this.editPhase = 'exclusion';

          if (field.exclusions && field.exclusions.length > 0) {
            this.exclusions = field.exclusions.map(exclusion =>
              exclusion.map(coord => L.latLng(coord.lon, coord.lat))
            );
          }

          setTimeout(() => {
            if (this.mapComponent) {
              this.redrawOuterBoundary();

              if (this.exclusions.length > 0) {
                this.redrawLockedExclusions()
              }

              this.mapComponent.zoomToCoordinates(this.lockedOuterBoundary);
            }
          }, 100);
        }

        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading field:', error);
        this.toastr.error('Failed to load field data', 'Error');
        this.isLoading = false;
        this.router.navigate(['/map-overview']);
      }
    });
  }

  onClearBoundary(): void {
    this.mapComponent.clearBoundary();
    this.clearOverlapVisualization();
  }

  get canClearBoundary(): boolean {
    return this.markerCount > 0;
  }

  onSubmit(): void {
    this.createEditForm.markAllAsTouched();

    this.globalErrorMessage = null;
    this.clearOverlapVisualization();

    if (this.hasUnlockedBoundary) {
      this.globalErrorMessage = 'Please finish the outer boundary before saving. Click "Done" to finalize your boundary.';
      return;
    }

    if (this.isDrawingExclusion) {
      this.globalErrorMessage = 'Please finish or cancel the current exclusion zone before saving.';
      return;
    }

    if (!this.createEditForm.get('name')?.valid) {
      this.globalErrorMessage = 'Please provide a valid field name';
      return;
    }

    if (!this.lockedOuterBoundary || this.lockedOuterBoundary.length < 3) {
      this.globalErrorMessage = 'Please draw at least 3 boundary points on the map';
      return;
    }

    const coordinates = this.drawingService.getCurrentCoordinates();
    const fieldDto: FieldCreateDto = {
      name: this.createEditForm.get('name')?.value.trim(),
      boundaries: this.lockedOuterBoundary?.map(coord => ({
        lat: coord.lat,
        lon: coord.lng
      })) || [],
      exclusions: this.exclusions.map(exclusion =>
        exclusion.map(coord => ({
          lat: coord.lat,
          lon: coord.lng
        }))
      )
    };

    if (this.mode === 'create') {
      this.createField(fieldDto);
    } else {
      this.updateField(fieldDto);
    }
  }

  private createField(fieldDto: FieldCreateDto): void {
    this.fieldService.createField(fieldDto).subscribe({
      next: (response) => {
        this.toastr.success(`Field "${fieldDto.name}" has been created successfully`, 'Success');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error creating field:', error);
        this.handleFieldError(error);
      }
    });
  }

  private updateField(fieldDto: FieldCreateDto): void {
    if (!this.fieldId) return;

    this.fieldService.editField(fieldDto, this.fieldId).subscribe({
      next: (response) => {
        this.toastr.success(`Field "${fieldDto.name}" has been updated successfully`, 'Success');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error updating field:', error);
        this.handleFieldError(error);
      }
    });
  }

  private handleFieldError(error: any): void {
    let overlapData = error?.error || error;

    if (overlapData?.overlappingGeometry) {

      console.log('siuu', overlapData);
      if (overlapData.overlappingFieldName) {
        this.globalErrorMessage = `This field overlaps with "${overlapData.overlappingFieldName}"`;
      } else {
        this.globalErrorMessage = overlapData.error || overlapData.message || overlapData.errorMessage || 'Invalid exclusion zone placement';
      }

      this.overlapGeometry = overlapData.overlappingGeometry.map((coord: any) =>
        L.latLng(coord.lat, coord.lon)
      );
      this.overlappingFieldName = overlapData.overlappingFieldName;

      this.visualizeOverlap();
    } else {
      console.log(error);
      this.globalErrorMessage = overlapData?.error || overlapData?.message || overlapData?.errorMessage || 'Failed to save field. Please try again.';
    }
  }

  private visualizeOverlap(): void {
    if (!this.mapComponent || !this.overlapGeometry || this.overlapGeometry.length < 3) {
      return;
    }

    if (this.overlapPolygon) {
      this.mapComponent.removeOverlapPolygon(this.overlapPolygon);
      this.overlapPolygon = undefined;
    }

    this.overlapPolygon = this.mapComponent.drawOverlapPolygon(this.overlapGeometry);
  }

  public clearOverlapVisualization(): void {
    this.overlapGeometry = null;
    this.overlappingFieldName = null;

    if (this.overlapPolygon && this.mapComponent) {
      this.mapComponent.removeOverlapPolygon(this.overlapPolygon);
      this.overlapPolygon = undefined;
    }
    this.globalErrorMessage = null;
  }

  onDelete(): void {
    if (!this.fieldId) return;
    this.showDeleteDialog = true;
  }

  onDeleteConfirmed(): void {
    this.showDeleteDialog = false;

    const fieldName = this.field?.name || 'this field';


    this.fieldService.deleteField(this.fieldId).subscribe({
      next: () => {
        this.toastr.success(`Field "${fieldName}" has been deleted successfully`, 'Success');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error deleting field:', error);
        const errorMessage = error?.error?.message || 'Failed to delete field';
        this.toastr.error(errorMessage, 'Error');
      }
    });
  }

  onDeleteCancelled(): void {
    this.showDeleteDialog = false;
  }

  lockOuterBoundary(): void {
    if (this.markerCount < 3) {
      return;
    }

    const outerCoords = this.drawingService.getCurrentCoordinates();

    const boundaries = this.convertToCoordinateDtos(outerCoords);
    const exclusions = this.exclusions.map(e => this.convertToCoordinateDtos(e));

    const validationRequest: FieldValidationRequestDto = {
      boundaries: boundaries,
      exclusions: exclusions,
      excludeFieldId: this.fieldId
    };

    this.fieldService.validateField(validationRequest).subscribe({
      next: (response) => {
        if (response.valid) {
          this.proceedWithLockingOuterBoundary(outerCoords);

        } else {
          console.log(response)
          this.handleFieldError(response);
        }
      },
      error: (error) => {
        console.error('Validation error:', error);
        this.handleFieldError(error);
      }
    });
  }

  private proceedWithLockingOuterBoundary(newOuterBoundary: L.LatLng[]): void {
    this.lockedOuterBoundary = newOuterBoundary;
    this.previousLockedOuterBoundary = null;

    this.redrawOuterBoundary();
    if (this.exclusions.length > 0) {
      this.redrawLockedExclusions();
    }

    this.mapComponent.clearBoundary();
    this.editPhase = 'exclusion';
    this.isInManagementView = true;
    this.mapComponent.zoomToCoordinates(this.lockedOuterBoundary);
    this.clearOverlapVisualization();
    this.globalErrorMessage = null;
  }

  unlockOuter(): void {
    if (this.lockedOuterPolygon) {
      this.lockedOuterPolygon.closePopup();
      this.mapComponent.removeLockedPolygon(this.lockedOuterPolygon);
      this.lockedOuterPolygon = undefined;
    }

    this.previousLockedOuterBoundary = this.lockedOuterBoundary;
    this.lockedOuterBoundary = null;

    if (this.previousLockedOuterBoundary) {
      this.mapComponent.initializeWithCoordinates(this.previousLockedOuterBoundary);
    }

    if (this.exclusions.length > 0) {
      this.redrawLockedExclusions(false);
    }

    this.editPhase = 'outer';
    this.isInManagementView = false;
    this.mapComponent.setExclusionMode(false);

    if (this.previousLockedOuterBoundary) {
      this.mapComponent.zoomToCoordinates(this.previousLockedOuterBoundary);
    }
  }

  addNewExclusion(): void {
    this.currentExclusionIndex = this.exclusions.length;
    this.isInManagementView = false;

    this.redrawOuterBoundary(false);
    if (this.exclusions.length > 0) {
      this.redrawLockedExclusions(false);
    }

    this.mapComponent.clearBoundary();
    this.mapComponent.setExclusionMode(true);
  }

  lockExclusion(): void {
    if (this.markerCount < 3) {
      this.globalErrorMessage = 'Need at least 3 points for a valid exclusion zone';
      return;
    }

    const exclusionCoords = this.drawingService.getCurrentCoordinates();

    const boundaries = this.convertToCoordinateDtos(this.lockedOuterBoundary!);

    let allExclusions: L.LatLng[][];
    if (this.currentExclusionIndex < this.exclusions.length) {
      allExclusions = this.exclusions.map((exc, i) =>
        i === this.currentExclusionIndex ? exclusionCoords : exc
      );
    } else {
      allExclusions = [...this.exclusions, exclusionCoords];
    }

    const validationRequest: FieldValidationRequestDto = {
      boundaries: boundaries,
      exclusions: allExclusions.map(e => this.convertToCoordinateDtos(e)),
      excludeFieldId: this.fieldId
    };

    this.fieldService.validateField(validationRequest).subscribe({
      next: (response) => {
        if (response.valid) {
          this.proceedWithLockingExclusion(exclusionCoords);
        } else {
          this.handleFieldError(response || 'Validation failed. Please check your exclusion zone.')
        }
      },
      error: (error) => {
        this.handleFieldError(error);
        console.error('Validation error:', error);
        this.globalErrorMessage = 'Failed to validate exclusion zone. Please try again.';
      }
    });
  }

  private proceedWithLockingExclusion(exclusionCoords: L.LatLng[]): void {
    if (this.currentExclusionIndex >= this.exclusions.length) {
      this.exclusions.push(exclusionCoords);
    } else {
      this.exclusions[this.currentExclusionIndex] = exclusionCoords;
    }

    const index = this.currentExclusionIndex;

    const polygon = this.mapComponent.drawLockedPolygon(
      exclusionCoords,
      MapColors.EXCLUSION_PRIMARY,
      `Exclusion ${index + 1}`,
      true,
      {
        title: `Exclusion ${index + 1}`,
        actions: [
          {label: 'Edit', callback: () => this.editExclusion(index), style: 'primary'},
          {label: 'Delete', callback: () => this.deleteExclusion(index), style: 'danger'}
        ]
      },
      undefined,
      true
    );

    if (index >= this.lockedExclusionPolygons.length) {
      this.lockedExclusionPolygons.push(polygon);
    } else {
      if (this.lockedExclusionPolygons[index]) {
        this.mapComponent.removeLockedPolygon(this.lockedExclusionPolygons[index]);
      }
      this.lockedExclusionPolygons[index] = polygon;
    }

    this.mapComponent.clearBoundary();
    this.currentExclusionIndex = -1;
    this.isInManagementView = true;
    this.mapComponent.setExclusionMode(false);

    this.redrawOuterBoundary();
    this.redrawLockedExclusions();

    if (this.lockedOuterBoundary) {
      this.mapComponent.zoomToCoordinates(this.lockedOuterBoundary);
    }

    this.globalErrorMessage = null;
  }

  editExclusion(index: number): void {
    if (this.lockedExclusionPolygons[index]) {
      this.lockedExclusionPolygons[index].closePopup();
      this.mapComponent.removeLockedPolygon(this.lockedExclusionPolygons[index]);
    }

    const editingExclusion = this.exclusions[index];
    this.exclusions.splice(index, 1);

    this.redrawOuterBoundary(false);
    this.redrawLockedExclusions(false);

    this.exclusions.splice(index, 0, editingExclusion);

    this.currentExclusionIndex = index;
    this.isInManagementView = false;
    this.mapComponent.initializeWithCoordinates(this.exclusions[index]);
    this.mapComponent.setExclusionMode(true);
    this.mapComponent.zoomToCoordinates(this.exclusions[index]);
  }

  deleteExclusion(index: number): void {
    if (this.lockedExclusionPolygons[index]) {
      this.mapComponent.removeLockedPolygon(this.lockedExclusionPolygons[index]);
    }

    this.exclusions.splice(index, 1);
    this.lockedExclusionPolygons.splice(index, 1);
    this.redrawOuterBoundary();
    this.redrawLockedExclusions();
    this.toastr.success('Exclusion zone deleted', '', {timeOut: 2000});
  }

  private redrawOuterBoundary(interactive: boolean = true): void {
    if (this.lockedOuterPolygon) {
      this.lockedOuterPolygon.closePopup();
      this.mapComponent.removeLockedPolygon(this.lockedOuterPolygon);
      this.lockedOuterPolygon = undefined;
    }

    if (this.lockedOuterBoundary) {
      this.lockedOuterPolygon = this.mapComponent.drawLockedPolygon(
        this.lockedOuterBoundary,
        MapColors.OUTER_BOUNDARY,
        '✓ Outer Boundary',
        interactive,
        interactive ? {
          title: 'Outer Boundary',
          actions: [{label: 'Edit', callback: () => this.unlockOuter(), style: 'primary'}]
        } : undefined,
        this.exclusions
      );
    }
    this.clearOverlapVisualization();
    this.mapComponent.zoomToCoordinates(this.lockedOuterBoundary);
  }

  private redrawLockedExclusions(interactive: boolean = true, skipIndex?: number): void {
    this.lockedExclusionPolygons.forEach((p, i) => {
      if (i !== skipIndex) {
        p.closePopup();
        this.mapComponent.removeLockedPolygon(p);
      }
    });

    this.lockedExclusionPolygons = this.exclusions.map((coords, i) => {
      if (i === skipIndex) {
        return this.lockedExclusionPolygons[i];
      }
      return this.mapComponent.drawLockedPolygon(
        coords,
        MapColors.EXCLUSION_PRIMARY,
        `Exclusion ${i + 1}`,
        interactive,
        interactive ? {
          title: `Exclusion ${i + 1}`,
          actions: [
            {label: 'Edit', callback: () => this.editExclusion(i), style: 'primary'},
            {label: 'Delete', callback: () => this.deleteExclusion(i), style: 'danger'}
          ]
        } : undefined,
        undefined,
        true
      );
    });
  }

  cancelExclusionDrawing(): void {
    this.mapComponent.clearBoundary();
    this.currentExclusionIndex = -1;
    this.isInManagementView = true;
    this.mapComponent.setExclusionMode(false);
    this.clearOverlapVisualization();

    this.redrawOuterBoundary();
    this.redrawLockedExclusions();
  }

  onCancelOuterDrawing(): void {
    if (this.editPhase === 'outer') {
      this.handleEscapeKey();
    }
  }

  private convertToCoordinateDtos(latLngs: L.LatLng[]): CoordinateDto[] {
    return latLngs.map(ll => ({
      lat: ll.lat,
      lon: ll.lng
    }));
  }

  private redrawMapObjects(): void {
    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        const filteredObjects = this.fieldId
          ? objects.filter(obj => !('fieldId' in obj) || obj.fieldId !== this.fieldId)
          : objects;

        this.mapComponent.drawLocatedObjects(filteredObjects);
      }
    });
  }

  zoomToField(): void {
    if (this.lockedOuterBoundary) {
      this.mapComponent.zoomToCoordinates(this.lockedOuterBoundary);
    }
  }

}
