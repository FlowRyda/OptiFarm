import {Component, OnInit} from '@angular/core';
import {FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {ChangePasswordDto} from "src/app/dtos/farmer/changePasswordDto";
import {FarmerService} from "src/app/services/farmer.service";
import {AuthService} from "src/app/services/auth.service";
import {EditFarmerDto} from "../../../dtos/farmer/editFarmerDto";
import {ActionButton, BackAction, EditField, FormLayoutConfig} from "../../shared/panel-layout/data";

@Component({
  selector: 'app-change-password',
  templateUrl: './farmer-change-password.component.html',
  standalone: false,
  styleUrl: './farmer-change-password.component.scss'
})
export class FarmerChangePasswordComponent implements OnInit {
  changePasswordForm: FormGroup;
  isSubmitting = false;
  error = false;
  successMessage = '';
  errorMessage = '';
  changePasswordDto?: ChangePasswordDto;
  isLoading = true;
  backAction: BackAction = {
    action: () => this.router.navigate([`/farmer/details`]),
  }

  formConfig?: FormLayoutConfig<ChangePasswordDto>;

  constructor(private formBuilder: UntypedFormBuilder, private farmerService: FarmerService, private authService: AuthService, private router: Router) {
    this.changePasswordForm = this.formBuilder.group({
        oldPassword: ['', [Validators.required, Validators.minLength(8)]],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        repeatNewPassword: ['', Validators.required],
      },
      {
        validators: this.passwordMatchValidator
      });
  }

  passwordMatchValidator(formGroup: UntypedFormGroup) {
    const passwordControl = formGroup.get('newPassword');
    const repeatPasswordControl = formGroup.get('repeatNewPassword');

    if (!passwordControl || !repeatPasswordControl) {
      return null;
    }

    const password = passwordControl.value;
    const repeatPassword = repeatPasswordControl.value;

    if (password !== repeatPassword) {
      repeatPasswordControl.setErrors({passwordMismatch: true});
      return {passwordMismatch: true};
    } else {
      if (repeatPasswordControl.hasError('passwordMismatch')) {
        repeatPasswordControl.setErrors(null);
      }
      return null;
    }
  }

  get newPassword() {
    return this.changePasswordForm.get('newPassword');
  }

  get repeatNewPassword() {
    return this.changePasswordForm.get('repeatNewPassword');
  }

  ngOnInit() {
    const actionButtons: ActionButton[] = [
      {label: 'Save', color: 'primary', action: () => this.onSubmit(),},
      {label: 'Cancel', color: 'warn', action: () => this.cancel(),},
    ]

    const editFields: EditField[] = [
      {
        label: 'Old Password',
        key: 'oldPassword',
        type: 'password',
        placeholder: 'Enter your old Password',
        errorMessage: 'Old password is required and must be at least 8 characters long.'
      },
      {
        label: 'New Password',
        key: 'newPassword',
        type: 'password',
        placeholder: 'Enter your new Password',
        errorMessage: 'New password is required.'
      },
      {
        label: 'Repeat new Password',
        key: 'repeatNewPassword',
        type: 'password',
        placeholder: 'Repeat your new Password',
        errorMessage: 'The passwords have to match.'
      },
    ];

    this.formConfig = {
      title: 'Update your Password',
      subtitle: ``,
      actionButtons: actionButtons,
      backAction: this.backAction,
      fields: editFields,
      data: this.changePasswordDto
    };

    this.isLoading = false;
  }

  onSubmit() {
    if (this.changePasswordForm.invalid) {
      this.changePasswordForm.markAllAsTouched();
      return;
    }

    const changePasswordDto: ChangePasswordDto = new ChangePasswordDto(this.changePasswordForm.value.newPassword, this.changePasswordForm.value.oldPassword);

    this.farmerService.changePassword(changePasswordDto).subscribe({
      next: response => {
        if (response) {
          this.authService.logoutUser();
          this.router.navigate(['/login']);
        } else {
          this.errorMessage = 'Unable to change your password. Check your current password.';
        }
      },
      error: error => {
        this.errorMessage = 'Unable to change your password. Check your current password.';
      }
    });
  }

  cancel() {
    this.router.navigate([`/farmer/details`]);
  }
}
