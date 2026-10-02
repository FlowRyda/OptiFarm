import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Tool} from '../../../dtos/tool';
import {Vehicle} from '../../../dtos/vehicle';
import {ToolService} from '../../../services/tool.service';
import {VehicleService} from '../../../services/vehicle.service';
import {MapComponent} from '../../shared/map/map.component';
import {ActionButton, BackAction, DetailConfig, InfoField} from '../../shared/panel-layout/data';
import {QrcodeService} from "../../../services/qrcode.service";
import {LocatedObjectService} from "../../../services/located-object.service";

@Component({
  selector: 'app-tool-detail',
  standalone: false,
  templateUrl: './equipment-detail.component.html',
  styleUrl: './equipment-detail.component.scss'
})
export class EquipmentDetailComponent implements AfterViewInit {

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  tool: Tool | null = null;
  isVehicle: boolean = false;
  loading: boolean = false;
  error: string | null = null;
  showDeleteConfirmation: boolean = false;

  detailConfig?: DetailConfig<Tool | Vehicle>;
  backAction: BackAction = {
    action: () => this.cancel()
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private toolService: ToolService,
    private vehicleService: VehicleService,
    private qrCodeService: QrcodeService,
    private locatorService: LocatedObjectService
  ) {
  }

  ngAfterViewInit(): void {
    this.route.data.subscribe(data => {
      this.isVehicle = !!data['isVehicle'];

      const idParam = this.route.snapshot.paramMap.get('id');
      const id = idParam ? +idParam : null;

      if (id == null || isNaN(id)) {
        this.error = 'Invalid equipment id.';
        return;
      }

      this.loadItem(id);
    });
  }

  private loadItem(id: number): void {
    this.loading = true;
    this.error = null;

    const obs = this.isVehicle
      ? this.vehicleService.getById(id)
      : this.toolService.getById(id);

    obs.subscribe({
      next: (data: Tool | Vehicle) => {
        this.loading = false;
        this.tool = data as Tool;
        this.buildDetailConfig(data);
        this.placeMarkerIfReady();
      },
      error: err => {
        this.loading = false;
        console.error('Failed to load item', err.error);
        this.error = err.error;
      }
    });
  }

  private buildDetailConfig(data: Tool | Vehicle): void {
    const tool = data as Tool;
    const vehicle = data as Vehicle;

    const fields: InfoField[] = [
      {label: 'Name', value: tool.name},
      {label: 'Description', value: tool.description},
      {label: 'Category', value: tool.categoryDto?.name ?? '—'},
      {label: 'Garage', value: tool.garageDto?.name ?? '—'},
      {label: 'Location (Lat, Lon)', value: tool.locationDto?.lat + ', ' + tool.locationDto?.lon},
      {label: 'Value ($)', value: tool.remainingValue},
      {label: 'Usable', value: tool.usable ? 'Yes' : 'No'}
    ];

    if (this.isVehicle) {
      fields.push(
        {label: 'Driven distance (km)', value: vehicle.drivenKm},
        {label: 'Fuel level (%)', value: vehicle.fuelLevel}
      );
    }

    const actionButtons: ActionButton[] = [
      {label: 'Edit', color: 'primary', action: () => this.editTool()},
      {label: 'Delete', color: 'warn', action: () => this.openDeleteModal()},
      {label: 'Generate Tracking QR Code', color: 'accent', action: () => this.generateQrCode()}
    ];

    this.detailConfig = {
      title: this.isVehicle ? 'Vehicle Details' : 'Equipment Details',
      subtitle: this.isVehicle
        ? 'Details of the selected vehicle'
        : 'Details of the selected equipment',
      fields: fields,
      actionButtons: actionButtons,
      backAction: this.backAction,
      data: data
    };
  }

  editTool(): void {
    if (!this.tool || this.tool.id == null) {
      return;
    }

    const editPrefix = this.isVehicle ? '/vehicle/edit' : '/equipment/edit';

    this.router.navigate([`${editPrefix}/${this.tool.id}`]);
  }

  deleteTool(): void {
    if (!this.tool || this.tool.id == null) {
      return;
    }

    this.error = null;

    const obs = this.isVehicle
      ? this.vehicleService.delete(this.tool.id)
      : this.toolService.delete(this.tool.id);

    obs.subscribe({
      next: () => {
        this.router.navigate(['/map-overview']);
      },
      error: err => {
        console.error('Failed to delete item', err.error);
        this.error = err.error;
      }
    });
  }

  generateQrCode() {
    if (!this.tool?.id) return;

    this.toolService.generateQrCode(this.tool.id).subscribe({
      next: async (qrBlob) => {
        try {
          await this.qrCodeService.createQrDocument([
            {name: this.tool?.name ?? '—', blob: qrBlob}
          ]);
        } catch (e) {
          console.error('Failed to build QR PDF', e);
          this.error = 'Failed to generate QR document.';
        }
      },
      error: err => {
        console.error('Failed to generate qr code', err.error);
        this.error = err.error;
      }
    });
  }

  openDeleteModal() {
    this.showDeleteConfirmation = true;
  }

  closeDeleteModal() {
    this.showDeleteConfirmation = false;
  }

  confirmDelete() {
    this.showDeleteConfirmation = false;
    this.deleteTool();
  }

  cancel(): void {
    this.router.navigate(['/map-overview']);
  }

  private placeMarkerIfReady() {
    if (!this.tool || !this.mapComponent)
      return;

    const lat = this.tool.locationDto.lat;
    const lon = this.tool.locationDto.lon;
    this.mapComponent.addMarker(lat, lon, this.tool.name, false, this.isVehicle ? 'assets/marker/tractor.svg' : 'assets/marker/tool.svg');
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
