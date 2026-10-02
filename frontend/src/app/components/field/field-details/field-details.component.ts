import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FieldService} from "../../../services/field.service";
import {AuthService} from "../../../services/auth.service";
import {MapComponent} from "../../shared/map/map.component";
import {ActiveFieldDetailDto, ActiveFieldUpdateDto} from "../../../dtos/activefield";
import {ActiveFieldService} from "../../../services/activefield.service";
import {FieldDetailDto} from "../../../dtos/field";
import {ActionButton, BackAction} from "../../shared/panel-layout/data";
import {LocatedObjectService} from "src/app/services/located-object.service";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-field-details',
  standalone: false,
  templateUrl: './field-details.component.html',
  styleUrl: './field-details.component.scss'
})
export class FieldDetailsComponent implements OnInit {
  isLoading = true;
  fieldId: number;
  activeField: ActiveFieldDetailDto;
  field: FieldDetailDto;
  showDeleteDialog = false;
  showResetDialog = false;
  showHarvestDialog = false;

  isEditingHarvestDate = false;
  harvestDateAdjustment = 0;
  validationMessage: string | null = null;

  displayProgressValue: number;

  private readonly MIN_DAYS_AFTER_PLANTING = 7;

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  backActionConfig: BackAction = {
    action: () => this.router.navigate(['/map-overview'])
  };

  actionButtonsConfig: ActionButton[] = [];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fieldService: FieldService,
    public authService: AuthService,
    private readonly router: Router,
    private readonly activeFieldService: ActiveFieldService,
    private readonly locatorService: LocatedObjectService,
    private readonly toastr: ToastrService,
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.fieldId = Number(params.get('id'));
      this.loadFieldDetails();
      this.loadActiveFieldDetails();
      if (this.mapComponent) {
        this.onMapReady(this.mapComponent);
      }
    });
  }

  onMapReady(map: MapComponent): void {
    this.mapComponent = map;

    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        this.mapComponent.drawLocatedObjects(objects, this.fieldId);
      },
      error: (error) => {
        console.error('Error retrieving located objects:', error);
      }
    });
  }

  getDaysAgo(date: Date): number {
    const today = new Date();
    const planted = new Date(date);
    const diffTime = today.getTime() - planted.getTime();
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
  }

  calculateDaysRemaining(expectedDate: Date): number {
    const today = new Date();
    const expected = new Date(expectedDate);
    const diffTime = expected.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  startEditingHarvestDate(): void {
    this.isEditingHarvestDate = true;
    this.harvestDateAdjustment = 0;
    this.validationMessage = null;
  }

  cancelHarvestDateEdit(): void {
    this.isEditingHarvestDate = false;
    this.harvestDateAdjustment = 0;
    this.validationMessage = null;
  }

  adjustHarvestDate(days: number): void {
    const newAdjustment = this.harvestDateAdjustment + days;

    if (this.isValidAdjustment(newAdjustment)) {
      this.harvestDateAdjustment = newAdjustment;
      this.validationMessage = null;
      this.updateDisplayProgress();
    }
  }

  canAdjustBy(days: number): boolean {
    return this.isValidAdjustment(this.harvestDateAdjustment + days);
  }

  getAdjustmentTooltip(days: number): string {
    if (!this.canAdjustBy(days)) {
      const minHarvestDate = this.getMinimumHarvestDate();
      return `Harvest must be at least ${this.MIN_DAYS_AFTER_PLANTING} days after planting (${minHarvestDate.toLocaleDateString()})`;
    }
    return '';
  }

  private isValidAdjustment(adjustment: number): boolean {
    // Check if adjusted date is at least 7 days after planting
    const adjustedDate = this.getAdjustedHarvestDate(adjustment);
    const minHarvestDate = this.getMinimumHarvestDate();

    return adjustedDate >= minHarvestDate;
  }

  private getMinimumHarvestDate(): Date {
    const minDate = new Date(this.activeField.plantedDate);
    minDate.setDate(minDate.getDate() + this.MIN_DAYS_AFTER_PLANTING);
    return minDate;
  }

  getAdjustedHarvestDate(adjustment: number = this.harvestDateAdjustment): Date {
    const adjustedDate = new Date(this.activeField.expectedDate);
    adjustedDate.setDate(adjustedDate.getDate() + adjustment);
    return adjustedDate;
  }

  private updateDisplayProgress(): void {
    // Recalculate progress based on adjusted harvest date
    const plantedDate = new Date(this.activeField.plantedDate);
    const adjustedHarvestDate = this.getAdjustedHarvestDate();
    const today = new Date();

    const totalDays = Math.floor((adjustedHarvestDate.getTime() - plantedDate.getTime()) / (1000 * 60 * 60 * 24));
    const daysPassed = Math.floor((today.getTime() - plantedDate.getTime()) / (1000 * 60 * 60 * 24));

    this.displayProgressValue = Math.min(100, Math.max(0, Math.round((daysPassed / totalDays) * 100)));
  }

  saveHarvestDateAdjustment(): void {
    if (this.harvestDateAdjustment === 0) {
      this.toastr.info('No changes to save', 'No Adjustment');
      return;
    }

    if (!this.isValidAdjustment(this.harvestDateAdjustment)) {
      this.toastr.error('Invalid adjustment', 'Error');
      return;
    }

    const adjustedDate = this.getAdjustedHarvestDate();
    const oldDate = new Date(this.activeField.expectedDate);

    const activeFieldDto: ActiveFieldUpdateDto = {
      id: this.activeField.id,
      startDate: this.activeField.plantedDate,
      endDate: null,
      expectedDate: adjustedDate,
      cropTypeId: this.activeField.cropTypeId,
      fieldId: this.activeField.fieldId
    };

    this.activeFieldService.editActiveField(activeFieldDto, this.activeField.id).subscribe({
      next: () => {
        const dayText = Math.abs(this.harvestDateAdjustment) === 1 ? 'day' : 'days';
        const direction = this.harvestDateAdjustment > 0 ? 'delayed' : 'advanced';

        this.toastr.success(
          `Harvest date ${direction} by ${Math.abs(this.harvestDateAdjustment)} ${dayText}`,
          'Harvest Date Updated'
        );

        // Reload the active field to get updated data
        this.loadActiveFieldDetails();
        this.isEditingHarvestDate = false;
        this.harvestDateAdjustment = 0;
        this.onMapReady(this.mapComponent);
      },
      error: (error) => {
        console.error('Error updating harvest date:', error);
        this.toastr.error('Failed to update harvest date. Please try again.', 'Error');
      }
    });
  }

  getAdjustedHarvestDateString(): string {
    const date = this.getAdjustedHarvestDate();
    return date.toISOString().split('T')[0];
  }

  getMinimumHarvestDateString(): string {
    const minDate = this.getMinimumHarvestDate();
    return minDate.toISOString().split('T')[0];
  }

  onDatePickerChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const selectedDate = new Date(input.value);

    // Reset to midnight to avoid timezone issues
    selectedDate.setHours(0, 0, 0, 0);

    const originalDate = new Date(this.activeField.expectedDate);
    originalDate.setHours(0, 0, 0, 0);

    // Calculate the difference in days
    const diffTime = selectedDate.getTime() - originalDate.getTime();
    const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

    // Validate the adjustment
    if (this.isValidAdjustment(diffDays)) {
      this.harvestDateAdjustment = diffDays;
      this.validationMessage = null;
      this.updateDisplayProgress();
    } else {
      // If invalid, show validation message and reset the input
      const minDate = this.getMinimumHarvestDate();
      this.validationMessage = `Harvest must be at least ${this.MIN_DAYS_AFTER_PLANTING} days after planting (${minDate.toLocaleDateString()})`;
      // Reset the input to current adjusted date
      input.value = this.getAdjustedHarvestDateString();
    }
  }

  resetToOriginalDate(): void {
    this.harvestDateAdjustment = 0;
    this.validationMessage = null;
    this.updateDisplayProgress();
  }

  handleHarvestAction(): void {
    if (this.activeField.growthStatus <= 50) {
      this.showHarvestDialog = true;
    } else {
      this.router.navigate(['/activeField/harvestPage/', this.activeField.id]);
    }
  }

  onHarvestConfirmed(): void {
    this.showHarvestDialog = false;
    this.router.navigate(['/activeField/harvestPage/', this.activeField.id]);
  }

  onHarvestCancelled(): void {
    this.showHarvestDialog = false;
  }

  openDeleteDialog(): void {
    this.showDeleteDialog = true;
  }

  openResetDialog(): void {
    this.showResetDialog = true;
  }

  onDeleteConfirmed() {
    this.showDeleteDialog = false;
    this.fieldService.deleteField(this.fieldId).subscribe({
      next: () => {
        console.log('Field deleted successfully');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error deleting field:', error);
      }
    });
  }

  onDeleteCancelled() {
    this.showDeleteDialog = false;
  }

  onResetCancelled() {
    this.showResetDialog = false;
  }

  onResetConfirmed(): void {
    this.showResetDialog = false;
    this.activeFieldService.deleteField(this.activeField.id).subscribe({
      next: () => {
        console.log('Active Field deleted successfully');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error deleting field:', error);
      }
    });
  }

  updateActionButtons(): void {
    this.actionButtonsConfig = [];

    if (this.activeField) {
      this.actionButtonsConfig.push({
          label: 'Harvest Field',
          color: 'accent',
          action: () => this.handleHarvestAction()
        },
        {
          label: 'Reset Field',
          color: 'warn',
          action: () => this.openResetDialog()
        });
    } else {
      this.actionButtonsConfig.push({
        label: 'Plant Seed',
        color: 'accent',
        action: () => this.router.navigate(['/field/plant-seed', this.fieldId])
      });
    }

    this.actionButtonsConfig.push(
      {
        label: 'Edit Field',
        color: 'primary',
        action: () => this.router.navigate(['/field/edit/', this.fieldId])
      },
      {
        label: 'Visualize Data',
        color: 'secondary',
        action: () => this.router.navigate(['/field/data/', this.fieldId])
      }
    );

    if (!this.activeField) {
      this.actionButtonsConfig.push({
        label: 'Delete Field',
        color: 'warn',
        action: () => this.openDeleteDialog()
      });
    }
  }

  private loadFieldDetails(): void {
    this.fieldService.getFieldById(this.fieldId).subscribe({
      next: (field: FieldDetailDto) => {
        this.field = field;
        this.isLoading = false;
      },
      error: () => {
        console.error('Error loading field details');
        this.isLoading = false;
      }
    });
  }

  private loadActiveFieldDetails(): void {
    this.activeFieldService.getActiveFieldByFieldId(this.fieldId).subscribe({
      next: (activeField: ActiveFieldDetailDto) => {
        this.activeField = activeField;
        this.displayProgressValue = activeField.growthStatus;
        this.isLoading = false;
        this.updateActionButtons();
      },
      error: () => {
        console.error('Error loading active field details');
        this.isLoading = false;
        this.activeField = null;
        this.updateActionButtons();
      }
    });
  }

  formatExpectedHarvestDate(expectedDate: Date): string {
    const daysRemaining = this.calculateDaysRemaining(expectedDate);
    const date = new Date(expectedDate);
    const formattedDate = `${date.getDate()}. ${date.toLocaleString('en-US', {month: 'long'})} ${date.getFullYear()}`;

    if (daysRemaining < 0) {
      return `${formattedDate} (${Math.abs(daysRemaining)} days ago)`;
    } else if (daysRemaining === 0) {
      return `${formattedDate} (today)`;
    } else {
      return `${formattedDate} (${daysRemaining} days remaining)`;
    }
  }
}
