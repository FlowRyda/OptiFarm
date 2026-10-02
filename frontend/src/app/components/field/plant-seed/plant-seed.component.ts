import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SeedService} from '../../../services/seed.service';
import {Seed} from '../../../dtos/seed/seed';
import {ActivatedRoute, Router} from "@angular/router";
import {ActiveFieldCreateDto} from "../../../dtos/activefield";
import {ActiveFieldService} from "../../../services/activefield.service";
import {MapComponent} from "../../shared/map/map.component";
import {ActionButton, BackAction} from "../../shared/panel-layout/data";
import {FieldDetailDto} from "../../../dtos/field";
import {LocatedObjectService} from "../../../services/located-object.service";
import {FieldService} from "../../../services/field.service";
import {CroptypeService} from "../../../services/croptype.service";

@Component({
  selector: 'app-plant-seed',
  templateUrl: './plant-seed.component.html',
  styleUrl: './plant-seed.component.scss',
  standalone: false
})
export class PlantSeedComponent implements OnInit {
  public plantSeedForm: FormGroup;
  seeds: Seed[] = [];
  field?: FieldDetailDto;
  isLoading: boolean = false;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  validationErrors: string[] = [];
  fieldId: number;
  showCancelDialog = false;

  private growthDurationMap: Map<number, number> = new Map();

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  constructor(
    private readonly fb: FormBuilder,
    public router: Router,
    private readonly seedService: SeedService,
    private readonly activeFieldService: ActiveFieldService,
    private readonly route: ActivatedRoute,
    private readonly locatorService: LocatedObjectService,
    private readonly fieldService: FieldService,
    private readonly croptypeService: CroptypeService
  ) {
    this.plantSeedForm = this.fb.group({
      cropTypeId: ['', Validators.required],
      sellingPrice: ['', [Validators.required, Validators.min(0)]],
      plantDate: ['', Validators.required],
      expectedHarvestDate: [''],
    });

    this.setupAutoCalculation();
  }

  actionButtonsConfig: ActionButton[] = [
    {
      label: 'Plant Seed',
      color: 'primary',
      action: () => this.onSubmit(),
    },
  ];

  backActionConfig: BackAction = {
    action: () => this.cancel()
  };

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.fieldId = Number(params.get('id'));
      this.loadFieldDetails();
      this.loadCropTypes();
      this.loadSeeds();
    })
  };

  private setupAutoCalculation(): void {
    this.plantSeedForm.get('cropTypeId')?.valueChanges.subscribe(() => {
      this.calculateExpectedHarvestDate();
    });

    this.plantSeedForm.get('plantDate')?.valueChanges.subscribe(() => {
      this.calculateExpectedHarvestDate();
    });
  }

  private calculateExpectedHarvestDate(): void {
    const seedId = this.plantSeedForm.get('cropTypeId')?.value;
    const cropTypeId = this.seeds.find(seed => seed.id === Number(seedId)).cropTypeId;
    const plantDate = this.plantSeedForm.get('plantDate')?.value;

    if (cropTypeId && plantDate) {
      const growthDuration = this.growthDurationMap.get(Number(cropTypeId));

      if (growthDuration) {
        const plantDateTime = new Date(plantDate);
        const expectedHarvestDateTime = new Date(plantDateTime);
        expectedHarvestDateTime.setDate(plantDateTime.getDate() + growthDuration);

        const harvestDate = new Date(plantDate);
        harvestDate.setDate(harvestDate.getDate() + growthDuration);

        const formattedDate = harvestDate.toISOString().slice(0, 16);

        console.log('Calculated expected harvest date:', formattedDate);
        this.plantSeedForm.patchValue(
          {expectedHarvestDate: formattedDate},
          {emitEvent: false}
        );
      }
    }
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

  private loadFieldDetails() {
    this.fieldService.getFieldById(this.fieldId).subscribe({
      next: (field: FieldDetailDto) => {
        this.field = field;
        this.isLoading = false;
      },
      error: () => {
        console.error('Error loading field details:');
        this.isLoading = false;
      }
    });
  }

  getSelectedSeedGrowthDuration(): number | string {
    const seedId = this.plantSeedForm.get('cropTypeId')?.value;
    if (!seedId) return '—';

    const seed = this.seeds.find(s => s.id === Number(seedId));
    if (seed?.cropTypeId) {
      return this.growthDurationMap.get(seed.cropTypeId) ?? 'Unknown';
    }
    return 'Unknown';
  }

  private loadCropTypes(): void {
    this.croptypeService.getAll().subscribe({
      next: (cropTypes) => {
        console.log('Crop Types:', cropTypes);
        cropTypes.forEach((cropType) => {
          if (cropType.id && cropType.growthDuration) {
            this.growthDurationMap.set(cropType.id, cropType.growthDuration);
          }
        });
        console.log('Growth Duration Map:', this.growthDurationMap);
      },
      error: (err) => {
        console.error('Error loading crop types:', err);
      }
    });
  }

  loadSeeds(): void {
    this.seedService.getAll().subscribe({
      next: (seeds) => {
        this.seeds = seeds;
        console.log(this.seeds);
      },
      error: () => {
        this.errorMessage = 'Failed to load seeds.';
        console.error('Error loading seeds:');
      }
    });
  }

  get cropTypeId() {
    return this.plantSeedForm.get('cropTypeId');
  }

  get sellingPrice() {
    return this.plantSeedForm.get('sellingPrice');
  }

  get plantDate() {
    return this.plantSeedForm.get('plantDate');
  }

  get expectedHarvestDate() {
    return this.plantSeedForm.get('expectedHarvestDate');
  }

  get name() {
    return this.plantSeedForm.get('name');
  }

  onSubmit(): void {
    if (this.plantSeedForm.invalid) {
      this.plantSeedForm.markAllAsTouched();
      console.log('Form is invalid');
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.validationErrors = [];

    const activeField: ActiveFieldCreateDto = {
      startDate: this.plantSeedForm.value.plantDate,
      cropTypeId: Number(this.plantSeedForm.value.cropTypeId),
      fieldId: this.fieldId,
      expectedHarvestDate: this.plantSeedForm.value.expectedHarvestDate || null,
      sellingPrice: Number(this.plantSeedForm.value.sellingPrice)
    };

    this.activeFieldService.createActiveField(activeField).subscribe({
      next: (response) => {
        this.successMessage = `Active field for field ${response.fieldId} added successfully!`;
        this.plantSeedForm.reset();
        this.isSubmitting = false;
        this.router.navigate(['/map-overview']);
      },
      error: (err) => {
        console.error(err);
        this.isSubmitting = false;

        if (err.error && typeof err.error === 'string') {
          let errorText = err.error.replace('Active field validation failed: ', '');
          this.validationErrors = errorText.split(', ').map(e => e.trim());
        } else {
          this.validationErrors = ['Failed to add active field. Please try again.'];
        }
      }
    })
  }

  clearValidationErrors(): void {
    this.validationErrors = [];
  }

  cancel(): void {
    if (!this.plantSeedForm.dirty) {
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
