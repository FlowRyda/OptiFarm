import {Component, OnInit} from '@angular/core';
import {ActionButton, BackAction, DetailConfig, InfoField} from "../../shared/panel-layout/data";
import {ActivatedRoute, Router} from "@angular/router";
import {Garage} from "../../../dtos/tool";
import {GarageService} from "../../../services/garage.service";
import {MapComponent} from "../../shared/map/map.component";
import {LocatedObjectService} from "../../../services/located-object.service";

@Component({
  selector: 'app-garage-detail',
  templateUrl: './garage-detail.component.html',
  styleUrl: './garage-detail.component.scss',
  standalone: false
})
export class GarageDetailComponent implements OnInit {
  garageDetailConfig?: DetailConfig<Garage>

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  }

  mapComponent!: MapComponent;

  garage?: Garage;
  isLoading = true;
  errorMessage = '';
  private mapReady = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private garageService: GarageService,
    private locatorService: LocatedObjectService
  ) {
  }


  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const actionButtons: ActionButton[] = [
      {label: 'Edit', color: 'primary', action: () => this.editGarage(),},
      {label: 'Delete', color: 'warn', action: () => this.openDeleteModal(),},
    ];
    if (id) {
      this.garageService.getById(id).subscribe({
        next: (garage) => {
          this.garage = garage;
          this.isLoading = false;

          const infoFields: InfoField[] = [
            {label: 'Name', value: garage.name},
            {label: 'Latitude', value: garage.coordinate.lat},
            {label: 'Longitude', value: garage.coordinate.lon},
          ];
          this.garageDetailConfig = {
            title: garage.name,
            subtitle: 'Garage Details',
            actionButtons: actionButtons,
            backAction: this.backAction,
            fields: infoFields,
            data: garage,
          }

          this.placeMarkerIfReady()
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Failed to load garage.';
          this.isLoading = false;
        },
      });
    } else {
      this.errorMessage = 'Invalid Garage ID.';
      this.isLoading = false;
    }
  }

  private placeMarkerIfReady() {
    const lat = this.garage.coordinate.lat;
    const lon = this.garage.coordinate.lon;
    console.log(lat, lon);
    this.mapComponent.addMarker(lat, lon, this.garage.name, false, 'assets/marker/garage.svg');
  }


  editGarage() {
    if (this.garage) {
      this.router.navigate(['/garage/edit', this.garage.id]);
    }
  }

  showDeleteModal = false;

  openDeleteModal() {
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
  }

  confirmDelete() {
    this.showDeleteModal = false;
    this.deleteGarage();
  }

  deleteGarage() {
    if (this.garage) {
      this.garageService.delete(this.garage.id).subscribe({
        next: () => {
          this.router.navigate(['/map-overview']);
        },
        error: (err) => {
          console.error(err);
          alert('Failed to delete garage.');
        }
      });
    }
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
