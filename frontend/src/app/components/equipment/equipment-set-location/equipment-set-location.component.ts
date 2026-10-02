import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";
import {ToolService} from "../../../services/tool.service";
import {CoordinateDto} from "../../../dtos/locator/coordinateDto";
import {MapComponent} from "../../shared/map/map.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MarkerDto} from "../../../dtos/locator/markerDto";

@Component({
  selector: 'app-equipment-set-location',
  standalone: false,
  templateUrl: './equipment-set-location.component.html',
  styleUrl: './equipment-set-location.component.scss'
})
export class EquipmentSetLocationComponent implements OnInit, AfterViewInit {
  status = 'Click the button to set your current position.';
  sending = false;
  private posting = false;

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  activeLat: number;
  activeLon: number;

  private mapIsReady = false;
  private markerPlaced = false;

  ngAfterViewInit(): void {
    this.mapIsReady = true;
    this.tryPlaceMarker();
  }

  constructor(
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private service: ToolService
  ) {
  }

  async ngOnInit(): Promise<void> {
    if (!('geolocation' in navigator)) {
      this.status = 'Geolocation not supported.';
      return;
    }

    try {
      this.status = 'Requesting location permission…';

      const pos = await this.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000,
      });

      this.activeLat = pos.coords.latitude;
      this.activeLon = pos.coords.longitude;

      this.tryPlaceMarker();

    } catch (err: any) {
      const msg = err?.message ?? String(err);
      this.status = `Location permission denied or unavailable: ${msg}`;
    }
  }


  private tryPlaceMarker() {
    if (this.markerPlaced) return;
    if (!this.mapIsReady) return;
    if (this.activeLat == null || this.activeLon == null) return;
    if (!this.mapComponent) return;

    this.mapComponent.addMarker(this.activeLat, this.activeLon, "New Location", true)
    this.markerPlaced = true;
  }

  async submitPosition(): Promise<void> {
    if (this.sending) return;
    this.sending = true;

    if (!('geolocation' in navigator)) {
      this.status = 'Geolocation not supported.';
      return;
    }

    try {
      this.status = 'Sending location…';

      const coords: CoordinateDto = {
        lat: this.activeLat,
        lon: this.activeLon
      }

      const {id, token} = this.route.snapshot.params as { id: number; token: string };

      this.service.setPosition(id, token, coords).subscribe({
        next: () => {
          this.sending = false;
          this.showToast('Position updated', 'success');
        },
        error: () => {
          this.sending = false;
          this.showToast('Update currently not possible.', 'error');
        }
      });
    } catch (err: any) {
      const msg = err?.message ?? String(err);
      this.status = `Location permission denied or unavailable: ${msg}`;
    }
  }

  private showToast(message: string, type: 'success' | 'error') {
    this.snackBar.open(message, undefined, {
      duration: 3000,
      panelClass: [`toast-${type}`],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  private getCurrentPosition(options?: PositionOptions): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, options);
    });
  }

  onMarkerDragged(coords: { lat: number; lon: number }) {
    this.activeLon = coords.lon;
    this.activeLat = coords.lat;
  }

  onMapReady(map: MapComponent) {
    this.mapComponent = map;
    this.mapIsReady = true;
    this.tryPlaceMarker();
  }
}
