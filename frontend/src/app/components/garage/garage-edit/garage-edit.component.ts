import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActionButton, BackAction, FormLayoutConfig} from "../../shared/panel-layout/data";
import {ActivatedRoute, Router} from "@angular/router";
import {Garage} from "../../../dtos/tool";
import {GarageService} from "../../../services/garage.service";
import {MapComponent} from "../../shared/map/map.component";
import {GARAGE_EDIT_FIELDS} from '../garage-form/garage-form.config';
import {LocatedObjectService} from "../../../services/located-object.service";

@Component({
  selector: 'app-garage-edit',
  templateUrl: './garage-edit.component.html',
  styleUrl: './garage-edit.component.scss',
  standalone: false
})
export class GarageEditComponent implements OnInit, AfterViewInit {
  garageForm: FormGroup;
  garageId!: number;
  isLoading = true;
  isSubmitting = false;
  garage?: Garage;

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;


  backAction: BackAction = {
    action: () => this.router.navigate([`/garage/details/${this.garageId}`]),
  }

  formConfig?: FormLayoutConfig<Garage>;
  private mapReady = false;

  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private garageService: GarageService,
    private locatorService: LocatedObjectService
  ) {
    this.garageForm = this.fb.group({
      name: ['', Validators.required],
      lat: ['', Validators.required],
      lon: ['', Validators.required]
    });
  }

  ngAfterViewInit(): void {
    this.mapReady = true;
    this.placeMarkerIfReady();
  }

  private placeMarkerIfReady() {
    const lat = this.garage.coordinate.lat;
    const lon = this.garage.coordinate.lon;

    this.mapComponent.addMarker(lat, lon, this.garage.name, true, 'assets/marker/garage.svg');
  }


  ngOnInit(): void {
    this.garageId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.garageId) {
      this.errorMessage = 'Invalid garage ID.';
      return;
    }

    this.garageService.getById(this.garageId).subscribe({
      next: (garage) => {
        this.garage = garage;
        const actionButtons: ActionButton[] = [
          {label: 'Save', color: 'primary', action: () => this.onSubmit(),},
          {label: 'Cancel', color: 'warn', action: () => this.cancel(),},
        ]


        this.formConfig = {
          title: 'Edit Garage',
          subtitle: `Update details for: ${garage.name}`,
          actionButtons: actionButtons,
          backAction: this.backAction,
          fields: GARAGE_EDIT_FIELDS,
          data: garage
        };

        this.garageForm.patchValue({
          name: garage.name,
          lat: garage.coordinate.lat,
          lon: garage.coordinate.lon
        });

        this.isLoading = false;
        this.placeMarkerIfReady();
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load garage.';
        this.isLoading = false;
      }
    });
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
    this.mapComponent.addMarker(Number(addr.lat), Number(addr.lon), this.garageForm.value.name, true, 'assets/marker/garage.svg');
  }

  onSubmit() {
    if (this.garageForm.invalid) {
      this.garageForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const updatedGarage: Garage = {
      id: this.garageId,
      ownerId: this.garage.ownerId,
      name: this.garageForm.value.name,
      coordinate: {
        lat: this.garageForm.value.lat,
        lon: this.garageForm.value.lon
      }
    };

    const SuccessMessageDelayMs = 900;
    this.garageService.update(updatedGarage).subscribe({
      next: () => {
        this.successMessage = 'Garage updated successfully!';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate([`/garage/details/${this.garageId}`]);
        }, SuccessMessageDelayMs);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to update garage.';
        this.isSubmitting = false;
      }
    });
  }

  cancel() {
    this.router.navigate([`/garage/details/${this.garageId}`]);
  }

  onMarkerDragged(coords: { lat: number; lon: number }) {
    this.garageForm.patchValue({
      lat: coords.lat,
      lon: coords.lon
    });
  }

  onMapClicked(coords: { lat: number; lon: number }) {
    this.garageForm.patchValue({
      lat: coords.lat,
      lon: coords.lon
    });

    this.mapComponent.clearMarker();
    this.mapComponent.addMarker(coords.lat, coords.lon, this.garageForm.value.name);
  }

  onMapReady(map: MapComponent): void {
    this.mapComponent = map;

    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        this.mapComponent.drawLocatedObjects(objects);
      },
      error: (error) => {
        console.error('Error retrieving located objects:', error);
      }
    });
  }

}
