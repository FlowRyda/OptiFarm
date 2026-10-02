import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {EditFarmerDto} from "src/app/dtos/farmer/editFarmerDto";
import {AuthService} from "src/app/services/auth.service";
import {FarmerService} from "src/app/services/farmer.service";
import {ActionButton, BackAction, DetailConfig, InfoField} from "../../shared/panel-layout/data";

@Component({
  selector: 'app-details',
  templateUrl: './farmer-details.component.html',
  standalone: false,
  styleUrl: './farmer-details.component.scss'
})
export class FarmerDetailsComponent implements OnInit {
  farmerDetailConfig?: DetailConfig<EditFarmerDto>
  farmerDetails: EditFarmerDto;
  isLoading = true;
  showDeleteModal = false;
  error = false;
  errorMessage = '';

  constructor(private farmerService: FarmerService, private authService: AuthService, private router: Router) {
  }

  ngOnInit() {
    this.getLoggedInFarmer();
  }


  openDeleteModal() {
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
  }

  confirmDelete() {
    this.showDeleteModal = false;
    this.deleteFarmer();
  }

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  }

  getLoggedInFarmer() {

    const actionButtons: ActionButton[] = [
      {label: 'Edit', color: 'primary', action: () => this.router.navigate(['/farmer/edit'])},
      {label: 'Change Password', color: 'accent', action: () => this.router.navigate(['/farmer/password'])},
      {label: 'Delete', color: 'warn', action: () => this.openDeleteModal(),},
    ];

    this.farmerService.viewFarmer().subscribe({
      next: farmer => {
        this.isLoading = false;
        this.farmerDetails = farmer;
        const infoFields: InfoField[] = [
          {label: 'E-Mail', value: this.farmerDetails.email},
          {label: 'Firstname', value: this.farmerDetails.firstname},
          {label: 'Lastname', value: this.farmerDetails.lastname},
        ];
        this.farmerDetailConfig = {
          title: 'Welcome',
          subtitle: 'Your Account Details',
          actionButtons: actionButtons,
          backAction: this.backAction,
          fields: infoFields,
          data: this.farmerDetails,
        }
      },
      error: error => {
        console.log('Could not change password of user due to:');
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

  deleteFarmer() {
    console.log('Try to delete farmer.');
    this.farmerService.deleteFarmer().subscribe({
      next: deleted => {
        if (deleted) {
          this.authService.logoutUser();
          this.router.navigate(['/']);
        } else {
          this.errorMessage = 'Unable to delete farmer.';
        }
      },
      error: error => {
        console.log('Could not delete farmer due to:');
        console.log(error);
        this.error = true;
        this.errorMessage = 'Unable to delete farmer.';
      }
    });
  }
}
