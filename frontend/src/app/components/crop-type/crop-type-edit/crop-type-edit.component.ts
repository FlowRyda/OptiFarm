import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CroptypeService} from '../../../services/croptype.service';
import {CropType} from '../../../dtos/crop-type/crop-type';
import {ActionButton, FormLayoutConfig, EditField, BackAction} from "../../shared/panel-layout/data";

@Component({
  selector: 'crop-type-edit',
  templateUrl: './crop-type-edit.component.html',
  styleUrls: ['./crop-type-edit.component.scss'],
  standalone: false
})
export class CropTypeEditComponent implements OnInit {
  cropTypeForm: FormGroup;
  cropTypeId!: number;
  isLoading = true;
  isSubmitting = false;

  backAction: BackAction = {
    action: () => this.router.navigate([`/cropType/${this.cropTypeId}`]),
  }

  formConfig?: FormLayoutConfig<CropType>;

  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private cropTypeService: CroptypeService
  ) {
    this.cropTypeForm = this.fb.group({
      name: ['', Validators.required],
      growthDuration: [1, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.cropTypeId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.cropTypeId) {
      this.errorMessage = 'Invalid crop type ID.';
      return;
    }

    this.cropTypeService.getById(this.cropTypeId).subscribe({
      next: (crop) => {
        const actionButtons: ActionButton[] = [
          { label: 'Save', color: 'primary', action: () => this.onSubmit(), },
          { label: 'Cancel', color: 'warn', action: () => this.cancel(), },
        ]

        const editFields: EditField[] = [
          { label: 'Name',
            key: 'name',
            type: 'text',
            placeholder: crop.name,
            errorMessage: 'Name is required.'
          },
          {
            label: 'Growth Duration (days)',
            key: 'growthDuration',
            type: 'number',
            placeholder: crop.growthDuration.toString(),
            errorMessage: 'Growth duration must be a number and 0 or more.',
          },
        ];

        this.formConfig = {
          title: 'Edit Crop Type',
          subtitle: `Update details for: ${crop.name}`,
          actionButtons: actionButtons,
          backAction: this.backAction,
          fields: editFields,
          data: crop
        };

        this.cropTypeForm.patchValue({
          name: crop.name,
          growthDuration: crop.growthDuration
        });

        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load crop type.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.cropTypeForm.invalid) {
      this.cropTypeForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const updatedCrop: CropType = {
      id: this.cropTypeId,
      ...this.cropTypeForm.value
    };

    const SuccessMessageDelayMs = 900;
    this.cropTypeService.update(updatedCrop).subscribe({
      next: () => {
        this.successMessage = 'Crop type updated successfully!';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate([`/cropType/${this.cropTypeId}`]);
        }, SuccessMessageDelayMs);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to update crop type.';
        this.isSubmitting = false;
      }
    });
  }

  cancel() {
    this.router.navigate([`/cropType/${this.cropTypeId}`]);
  }


}
