import {Component, OnInit} from '@angular/core';
import {FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {SignupService} from "../../services/signup.service";
import {CreateFarmerDto} from "src/app/dtos/farmer/createFarmerDto";
import {AuthService} from "../../services/auth.service";
import {ActionButton, EditField, FormLayoutConfig} from "../shared/panel-layout/data";

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  standalone: false,
  styleUrl: './signup.component.scss'
})
export class SignupComponent implements OnInit {
  public signupForm: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  formConfig?: FormLayoutConfig;

  constructor(private formBuilder: UntypedFormBuilder, private signupService: SignupService, private authService: AuthService, private router: Router) {
    this.signupForm = this.formBuilder.group({
        email: ['', [Validators.required]],
        firstname: ['', [Validators.required]],
        lastname: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        repeatPassword: ['', Validators.required]
      },
      {
        validators: this.passwordMatchValidator
      }
    );

    const signupFields: EditField[] = [
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
        placeholder: 'Enter your lastname.',
        errorMessage: 'Lastname is required.'
      },
      {
        label: 'Password',
        key: 'password',
        type: 'password',
        placeholder: 'Enter your password.',
        errorMessage: 'Password is required.'
      },
      {
        label: 'Repeat Password',
        key: 'repeatPassword',
        type: 'password',
        placeholder: 'Enter your password again.',
        errorMessage: 'The passwords have to match.'
      }
    ];

    const actionButtons: ActionButton[] = [
      {label: 'Signup', color: 'primary', action: () => this.onSubmit()}
    ];

    this.formConfig = {
      title: 'Create a new Account',
      subtitle: 'Fill in the details below to create a new account.',
      actionButtons: actionButtons,
      fields: signupFields
    };
  }


  get email() {
    return this.signupForm.get('email');
  }

  get password() {
    return this.signupForm.get('password');
  }

  get repeatPassword() {
    return this.signupForm.get('repeatPassword');
  }

  passwordMatchValidator(formGroup: UntypedFormGroup) {
    const passwordControl = formGroup.get('password');
    const repeatPasswordControl = formGroup.get('repeatPassword');

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


  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/map-overview']);
    }
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const userDto: CreateFarmerDto = new CreateFarmerDto(this.signupForm.controls.firstname.value, this.signupForm.controls.lastname.value, this.signupForm.controls.email.value, this.signupForm.controls.password.value);
    this.signupService.createUser(userDto).subscribe({
      next: response => {
        if (response) {
          this.router.navigate(['/login']);
        } else {
          this.errorMessage = "Could not sign you up.";
        }
      },
      error: error => {
        this.errorMessage = "Could not sign you up.";
      }
    });
  }
}
