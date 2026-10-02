import {Component, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActionButton, BackAction, EditField, FormLayoutConfig} from "../../shared/panel-layout/data";
import {Router} from "@angular/router";
import {GarageService} from "../../../services/garage.service";
import {Garage} from "../../../dtos/tool";
import {AuthService} from "../../../services/auth.service";
import {MapComponent} from "../../shared/map/map.component";
import {GARAGE_EDIT_FIELDS} from '../garage-form/garage-form.config';

@Component({
  selector: 'app-garage',
  templateUrl: './garage.component.html',
  styleUrl: './garage.component.scss',
  standalone: false
})
export class GarageComponent {
  public garageForm: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  mapComponent!: MapComponent;

  formConfig?: FormLayoutConfig;

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  }

  constructor(
    private fb: FormBuilder,
    public router: Router,
    private authService: AuthService,
    private garageService: GarageService
  ) {
    this.garageForm = this.fb.group({
      name: ['', Validators.required],
      lat: ['', Validators.required],
      lon: ['', Validators.required]
    });

    const actionButtons: ActionButton[] = [
      {label: 'Create', color: 'primary', action: () => this.onSubmit()}
    ];

    this.formConfig = {
      title: 'Add a new Garage',
      subtitle: 'Fill in the details below to add a new garge.',
      actionButtons: actionButtons,
      backAction: this.backAction,
      fields: GARAGE_EDIT_FIELDS
    };
  }

  onMapReady(map: MapComponent) {
    this.mapComponent = map;
    this.mapComponent.showUserLocation();
  }

  onChange() {
    if (this.garageForm.invalid)
      return

    this.mapComponent.clearMarker();
    this.mapComponent.addMarker(Number(this.garageForm.value.lat), Number(this.garageForm.value.lon), this.garageForm.value.name);
  }

  onAddressSelected(addr: {
    street: string;
    housenr: string;
    city: string;
    postalcode: string;
    country: string;
    lat: string;
    lon: string;
  }) {
    this.garageForm.patchValue({
      lat: addr.lat,
      lon: addr.lon
    });

    this.mapComponent.clearMarker();
    this.mapComponent.addMarker(Number(addr.lat), Number(addr.lon), this.garageForm.value.name);
  }


  onSubmit(): void {
    if (this.garageForm.invalid) {
      this.garageForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const newGarage: Garage = {
      name: this.garageForm.value.name,
      coordinate: {lat: this.garageForm.value.lat, lon: this.garageForm.value.lon},
      id: null,
      ownerId: this.authService.getUserId()
    };

    this.garageService.create(newGarage).subscribe({
      next: (response) => {
        this.successMessage = `Garage "${response.name}" added successfully!`;
        this.garageForm.reset();
        this.isSubmitting = false;
        if (response && response.id) {
          this.router.navigate([`/garage/details/${response.id}`]);
        } else {
          this.router.navigate(['/map-overview']);
        }
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to add garage. Please try again.';
        this.isSubmitting = false;
      }
    });
  }

  onMarkerDragged(coords: { lat: number; lon: number }) {
    this.garageForm.patchValue({
      lat: coords.lat,
      lon: coords.lon
    });

    console.log('Marker dragged to:', coords);
  }

  onMapClicked(coords: { lat: number; lon: number }) {
    this.garageForm.patchValue({
      lat: coords.lat,
      lon: coords.lon
    });

    this.mapComponent.clearMarker();
    this.mapComponent.addMarker(coords.lat, coords.lon, this.garageForm.value.name)
  }

}
