import {Component, OnInit} from '@angular/core';
import {FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {LoginDto} from "src/app/dtos/loginDto";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements OnInit {

  public loginForm: FormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  loginUser() {
    this.submitted = true;
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    if (this.loginForm.valid) {
      const loginDto: LoginDto = new LoginDto(this.loginForm.value.email, this.loginForm.value.password);
      this.authenticateUser(loginDto);
    } else {
      this.error = true;
      this.errorMessage = 'Invalid Credentials';
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param loginDto authentication data from the user login form
   */
  authenticateUser(loginDto: LoginDto) {
    this.authService.loginUser(loginDto).subscribe({
      next: () => {
        console.log('Successfully logged in user: ' + loginDto.email);
        this.router.navigate(['/map-overview']);
      },
      error: error => {
        this.error = true;
        this.errorMessage = 'Invalid email or password';
      }
    });
  }

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/map-overview']);
    }
  }

}
