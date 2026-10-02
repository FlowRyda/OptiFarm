import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SeedService} from '../../../services/seed.service';
import {CroptypeService} from '../../../services/croptype.service';
import {Seed} from "../../../dtos/seed/seed";
import {CropType} from "../../../dtos/crop-type/crop-type";
import {ActionButton, BackAction} from "../../shared/panel-layout/data";

@Component({
  selector: 'seed-edit',
  templateUrl: './seed-edit.component.html',
  styleUrls: ['./seed-edit.component.scss'],
  standalone: false
})
export class SeedEditComponent implements OnInit {
  seedForm: FormGroup;
  seedId!: number;
  isSubmitting = false;
  isLoading = true;
  successMessage = '';
  errorMessage = '';
  cropTypes: CropType[] = [];
  formConfig?: {
    backAction: BackAction;
    data: Seed;
    subtitle: string;
    actionButtons: ActionButton[];
    title: string;
    fields: { errorMessage: string; label: string; placeholder: string; type: string; key: string }[]
  };

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  };

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private seedService: SeedService,
    private cropTypeService: CroptypeService
  ) {
    this.seedForm = this.fb.group({
      name: ['', Validators.required],
      cropTypeId: [null, Validators.required],
      buyingPrice: [0, [Validators.required, Validators.min(0)]],
      buyingTime: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.seedId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.seedId) {
      this.errorMessage = 'Invalid seed ID.';
      this.isLoading = false;
      return;
    }

    this.cropTypeService.getAll().subscribe({
      next: (types) => {
        this.cropTypes = types;

        this.seedService.getById(this.seedId).subscribe({
          next: (seed) => {
            this.seedForm.patchValue({
              name: seed.name,
              cropTypeId: seed.cropTypeId,
              buyingPrice: seed.buyingPrice,
              buyingTime: seed.buyingTime
                ? this.formatForDatetimeLocal(seed.buyingTime)
                : null
            });

            const actionButtons: ActionButton[] = [
              {label: 'Save', color: 'primary', action: () => this.onSubmit()},
              {label: 'Cancel', color: 'warn', action: () => this.cancel()},
            ];

            const editFields = [
              {label: 'Name', key: 'name', type: 'text', placeholder: seed.name, errorMessage: 'Name is required.'},
              {
                label: 'Buying Price ($)',
                key: 'buyingPrice',
                type: 'number',
                placeholder: seed.buyingPrice.toString(),
                errorMessage: 'Buying price must be 0 or more.'
              },
              {
                label: 'Buying Time',
                key: 'buyingTime',
                type: 'datetime-local',
                placeholder: seed.buyingTime,
                errorMessage: 'Buying time is required.'
              },
              {
                label: 'Crop Type',
                key: 'cropTypeId',
                type: 'select',
                placeholder: 'Select crop type',
                errorMessage: 'Crop type is required.',
                options: this.cropTypes.map(ct => ({value: ct.id, label: ct.name}))
              },
            ];

            this.formConfig = {
              title: `Edit ${seed.name}`,
              subtitle: `Update details for: ${seed.name} (${this.getCropTypeName(seed.cropTypeId)})`,
              actionButtons,
              backAction: this.backAction,
              fields: editFields,
              data: seed
            };

            this.isLoading = false;
          },
          error: () => {
            this.errorMessage = 'Failed to load seed.';
            this.isLoading = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Failed to load crop types.';
        this.isLoading = false;
      }
    });
  }

  private formatForDatetimeLocal(dateString: string): string {
    const date = new Date(dateString);

    // Convert to ISO and cut off seconds + timezone
    return date.toISOString().slice(0, 16);
  }

  onSubmit() {
    if (this.seedForm.invalid) {
      this.seedForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const updatedSeed: Seed = {
      id: this.seedId,
      ...this.seedForm.value
    };

    this.seedService.update(updatedSeed).subscribe({
      next: () => {
        this.successMessage = 'Seed updated successfully!';
        this.isSubmitting = false;
        setTimeout(() => this.router.navigate(['/map-overview']), 900);
      },
      error: () => {
        this.errorMessage = 'Failed to update seed.';
        this.isSubmitting = false;
      }
    });
  }

  getCropTypeName(id: number): string {
    const cropType = this.cropTypes.find(ct => ct.id === id);
    return cropType ? cropType.name : '';
  }

  cancel() {
    this.router.navigate(['/map-overview']);
  }
}
