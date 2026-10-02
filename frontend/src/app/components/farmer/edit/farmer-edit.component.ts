import {Component, OnInit} from '@angular/core';
import {FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {EditFarmerDto} from "src/app/dtos/farmer/editFarmerDto";
import {FarmerService} from "src/app/services/farmer.service";
import {ActionButton, BackAction, EditField, FormLayoutConfig} from "../../shared/panel-layout/data";
import {Router} from "@angular/router";

@Component({
  selector: 'app-edit',
  templateUrl: './farmer-edit.component.html',
  standalone: false,
  styleUrl: './farmer-edit.component.scss'
})
export class FarmerEditComponent implements OnInit {
  farmerForm: FormGroup;
  isSubmitting = false;
  error = false;
  successMessage = '';
  errorMessage = '';
  farmer?: EditFarmerDto;
  isLoading = true;
  backAction: BackAction = {
    action: () => this.router.navigate([`/farmer/details`]),
  }

  formConfig?: FormLayoutConfig<EditFarmerDto>;

  constructor(private formBuilder: UntypedFormBuilder, private farmerService: FarmerService, private router: Router,) {
    this.farmerForm = this.formBuilder.group({
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required,]],
    });
  }

  ngOnInit() {
    this.farmerService.viewFarmer().subscribe({
      next: (farmer) => {
        this.farmer = farmer;

        const actionButtons: ActionButton[] = [
          {label: 'Save', color: 'primary', action: () => this.onSubmit(),},
          {label: 'Cancel', color: 'warn', action: () => this.cancel(),},
        ]

        const editFields: EditField[] = [
          {
            label: 'E-Mail',
            key: 'email',
            type: 'text',
            placeholder: 'Enter your E-Mail',
            errorMessage: 'E-Mail is required.'
          },
          {
            label: 'Firstname',
            key: 'firstname',
            type: 'text',
            placeholder: 'Enter your firstname.',
            errorMessage: 'Firstname is required.'
          },
          {
            label: 'Lastname',
            key: 'lastname',
            type: 'text',
            placeholder: 'Enter the your lastname.',
            errorMessage: 'Lastname is required.'
          }
        ];

        this.formConfig = {
          title: 'Update Profile',
          subtitle: `Update your Account Details`,
          actionButtons: actionButtons,
          backAction: this.backAction,
          fields: editFields,
          data: farmer
        };

        this.farmerForm.patchValue({
          firstname: this.farmer.firstname,
          lastname: this.farmer.lastname,
          email: this.farmer.email
        });

        this.isLoading = false;
      },
      error: error => {
        console.log('Could not load farmer details.:');
        console.log(error);
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.error;
        } else {
          this.errorMessage = error.error;
        }
      }
    });
  }

  onSubmit() {
    if (this.farmerForm.invalid) {
      this.farmerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';
    const editFarmerDto: EditFarmerDto = new EditFarmerDto(this.farmerForm.value.firstname, this.farmerForm.value.lastname, this.farmerForm.value.email);

    this.farmerService.updateFarmer(editFarmerDto).subscribe({
      next: () => {
        this.successMessage = 'Profile updated successfully!';
        this.isSubmitting = false;
        const SuccessMessageDelayMs = 900;

        setTimeout(() => {
          this.router.navigate([`/farmer/details`]);
        }, SuccessMessageDelayMs);
      },
      error: error => {
        this.errorMessage = 'Failed to update your profile.';
        this.isSubmitting = false;
      }
    });
  }

  cancel() {
    this.router.navigate([`/farmer/details`]);
  }
}
