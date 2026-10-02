import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CroptypeService} from '../../../services/croptype.service';
import {CropType} from '../../../dtos/crop-type/crop-type';
import {Router} from "@angular/router";
import {ActionButton, FormLayoutConfig, EditField, BackAction} from "../../shared/panel-layout/data";

@Component({
  selector: 'crop-type',
  templateUrl: './crop-type.component.html',
  styleUrls: ['./crop-type.component.scss'],
  standalone: false
})
export class CropTypeComponent {
  public cropTypeForm: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  formConfig?: FormLayoutConfig;

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  }

  constructor(
    private fb: FormBuilder,
    public router: Router,
    private cropTypeService: CroptypeService
  ) {
    this.cropTypeForm = this.fb.group({
      name: ['', Validators.required],
      growthDuration: ['', [Validators.required, Validators.min(0)]]
    });

    // Configure the form fields and action buttons
    const editFields: EditField[] = [
      {
        label: 'Name',
        key: 'name',
        type: 'text',
        placeholder: 'Enter name of a Crop (e.g., Corn, ...)',
        errorMessage: 'Name is required.'
      },
      {
        label: 'Growth Duration (days)',
        key: 'growthDuration',
        type: 'number',
        placeholder: 'Enter growth duration in days',
        errorMessage: 'Growth duration must be a number and 1 or more.'
      }
    ];

    const actionButtons: ActionButton[] = [
      { label: 'Create', color: 'primary', action: () => this.onSubmit() }
    ];

    this.formConfig = {
      title: 'Create a new Crop Type',
      subtitle: 'Fill in the details below to add a new crop type.',
      actionButtons: actionButtons,
      backAction: this.backAction,
      fields: editFields
    };
  }


  onSubmit(): void {
    if (this.cropTypeForm.invalid) {
      this.cropTypeForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const newCropType: CropType = {
      name: this.cropTypeForm.value.name,
      growthDuration: Number(this.cropTypeForm.value.growthDuration)
    };

    this.cropTypeService.createCropType(newCropType).subscribe({
      next: (response) => {
        this.successMessage = `Crop Type "${response.name}" added successfully!`;
        this.cropTypeForm.reset();
        this.isSubmitting = false;
        // navigate to the newly created crop type detail page
        if (response && response.id){
          this.router.navigate([`/cropType/${response.id}`]);
        } else {
          this.router.navigate(['/map-overview']);
        }
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to add crop type. Please try again.';
        this.isSubmitting = false;
      }
    });
  }


}
