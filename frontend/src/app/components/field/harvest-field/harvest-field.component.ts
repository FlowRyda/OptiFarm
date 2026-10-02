import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../services/auth.service";
import {MapComponent} from "../../shared/map/map.component";
import {ActiveFieldDetailDto} from "../../../dtos/activefield";
import {ActiveFieldService} from "../../../services/activefield.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CropCreateDto} from "../../../dtos/crop.s";
import {ActionButton, BackAction} from "../../shared/panel-layout/data";
import {LocatedObjectService} from "../../../services/located-object.service";

@Component({
  selector: 'harvest-field',
  standalone: false,
  templateUrl: './harvest-field.component.html',
  styleUrl: './harvest-field.component.scss'
})

export class HarvestFieldComponent implements OnInit {
  public harvestForm: FormGroup;

  isLoading = true;
  fieldId: number;
  activeField: ActiveFieldDetailDto;
  errorMessage = '';
  isSubmitting = false;
  successMessage = '';
  showDeleteDialog = false;
  showCancelDialog = false;
  actionButtonsConfig: ActionButton[] = [
    {
      label: 'Harvest Field',
      color: 'primary',
      action: () => this.onSubmit(),
    },
    {
      label: 'Reset Field',
      color: 'warn',
      action: () => this.openDeleteDialog()
    }
  ];


  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  constructor(private readonly fb: FormBuilder,
              private readonly route: ActivatedRoute,
              public authService: AuthService,
              private readonly router: Router,
              private readonly activeFieldService: ActiveFieldService,
              private readonly locatorService: LocatedObjectService) {
  }

  backActionConfig: BackAction = {
    action: () => this.cancel()
  };

  ngOnInit(): void {
    this.harvestForm = this.fb.group({
      currentPrice: ['', [Validators.required, Validators.min(0)]],
      harvestDate: ['', Validators.required],
      yield: ['', [Validators.required, Validators.min(0)]],
    });
    this.route.paramMap.subscribe((params) => {
      this.fieldId = Number(params.get('id'));
      this.loadActiveFieldDetails();
      if (this.mapComponent) {
        this.onMapReady(this.mapComponent)
      }
    });
  }

  onMapReady(map: MapComponent): void {
    this.mapComponent = map;

    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        this.mapComponent.drawLocatedObjects(objects, this.activeField.fieldId);
      },
      error: (error) => {
        console.error('Error retrieving located objects:', error);
      }
    });
  }

  private loadActiveFieldDetails() {
    this.activeFieldService.getActiveFieldById(this.fieldId).subscribe({
      next: (activeField: ActiveFieldDetailDto) => {
        this.activeField = activeField;
        this.isLoading = false;
      },
      error: () => {
        console.error('Error loading active field details:');
        this.isLoading = false;
      }
    });
  }

  deleteField() {
    if (confirm('Are you sure you want to remove the seed?')) {
      this.activeFieldService.deleteField(this.fieldId).subscribe({
        next: () => {
          console.log('Active Field deleted successfully');
          this.router.navigate(['/map-overview']);
        },
        error: (error) => {
          console.error('Error deleting field:', error);
        }
      });
    }
  }

  get currentPrice() {
    return this.harvestForm.get('currentPrice');
  }

  get harvestDate() {
    return this.harvestForm.get('harvestDate');
  }

  get yield() {
    return this.harvestForm.get('yield');
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
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }

  onSubmit(): void {
    if (this.harvestForm.invalid) {
      this.harvestForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const cropDto: CropCreateDto = {
      currentPrice: this.harvestForm.value.currentPrice,
      harvestDate: this.harvestForm.value.harvestDate,
      quantity: this.harvestForm.value.yield,
      cropTypeId: this.activeField.cropTypeId,
      farmerId: this.activeField.farmerId,
      fieldId: this.activeField.fieldId
    };
    this.activeFieldService.harvestField(cropDto, this.fieldId).subscribe({
      next: (response) => {
        this.successMessage = `Successfully documented harvest of ${this.activeField.seedName} on field ${this.activeField.name}`;
        this.harvestForm.reset();
        this.isSubmitting = false;
        this.router.navigate(['/map-overview']);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to harvest crop. Please try again.';
        this.isSubmitting = false;
      }
    })
  }

  openDeleteDialog(): void {
    this.showDeleteDialog = true;
  }

  onDeleteConfirmed(): void {
    this.showDeleteDialog = false;
    this.activeFieldService.deleteField(this.fieldId).subscribe({
      next: () => {
        console.log('Active Field deleted successfully');
        this.router.navigate(['/map-overview']);
      },
      error: (error) => {
        console.error('Error deleting field:', error);
      }
    });
  }

  onDeleteCancelled(): void {
    this.showDeleteDialog = false;
  }

  cancel(): void {
    if (!this.harvestForm.dirty) {
      this.navigateBack();
      return;
    }
    this.showCancelDialog = true;
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
}
