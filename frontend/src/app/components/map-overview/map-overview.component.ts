import {Component, OnInit, ViewChild} from '@angular/core';
import {CroptypeService} from "../../services/croptype.service";
import {CropType} from 'src/app/dtos/crop-type/crop-type';
import {Seed} from "../../dtos/seed/seed";
import {SeedService} from "../../services/seed.service";
import {Garage, Tool} from '../../dtos/tool';
import {ToolService} from '../../services/tool.service';
import {MapComponent} from "../shared/map/map.component";
import {SimpleFieldDto} from "../../dtos/field";
import {FieldService} from "../../services/field.service";
import {SimpleActiveFieldDto} from "../../dtos/activefield";
import {ActiveFieldService} from "../../services/activefield.service";
import {Router} from "@angular/router";
import {Vehicle} from "../../dtos/vehicle";
import {VehicleService} from "../../services/vehicle.service";
import {GarageService} from "../../services/garage.service";
import {LocatedObjectService} from "src/app/services/located-object.service";

@Component({
  selector: 'app-map-overview',
  templateUrl: './map-overview.component.html',
  standalone: false,
  styleUrl: './map-overview.component.scss'
})
export class MapOverviewComponent implements OnInit {
  openSections: Set<string> = new Set();
  tools: Tool[] = [];
  vehicles: Vehicle[] = [];
  cropTypes: CropType[] = [];
  garages: Garage[] = [];
  seeds: Seed[] = [];
  fields: SimpleFieldDto[] = [];
  activeFields: SimpleActiveFieldDto[] = [];
  emptyFields: SimpleFieldDto[] = [];
  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  maxItemsPerSection = 5;

  searchTerm: {
    fields: string;
    cropTypes: string;
    seeds: string;
    vehicles: string;
    tools: string;
    garages: string;
    activeFields: string;
  } = {
    fields: '',
    cropTypes: '',
    seeds: '',
    vehicles: '',
    tools: '',
    garages: '',
    activeFields: ''
  };

  getVisibleItems<T extends { name?: string }>(
    list: T[],
    section: keyof MapOverviewComponent['searchTerm']
  ): T[] {
    const term = this.searchTerm[section]?.trim().toLowerCase();

    const filtered = term
      ? list.filter(item =>
        item.name && item.name.toLowerCase().includes(term)
      )
      : list;

    return filtered.slice(0, this.maxItemsPerSection);
  }

  getFilteredCount<T extends { name?: string }>(
    list: T[],
    section: keyof MapOverviewComponent['searchTerm']
  ): number {
    const term = this.searchTerm[section]?.trim().toLowerCase();
    if (!term) {
      return list.length;
    }
    return list.filter(item =>
      item.name && item.name.toLowerCase().includes(term)
    ).length;
  }

  ngOnInit(): void {
    this.loadCropTypes();
    this.loadSeeds();
    this.loadFields();
    this.loadActiveFields();
    this.loadGarages();
    this.loadTools();
    this.loadVehicles();

    window.addEventListener('field-navigate', ((event: CustomEvent) => {
      this.navigateToField(event.detail);
    }) as EventListener);
  }


  constructor(private readonly cropTypeService: CroptypeService,
              private readonly seedService: SeedService,
              private readonly fieldService: FieldService,
              private readonly activeFieldService: ActiveFieldService,
              private readonly router: Router,
              private readonly garageService: GarageService,
              private readonly vehicleService: VehicleService,
              private readonly toolService: ToolService,
              private readonly locatorService: LocatedObjectService) {
  }

  loadFields() {
    this.fieldService.getAllFields().subscribe({
      next: (response) => {
        console.log('Fields retrieved successfully:', response);
        this.fields = response;
        this.filterEmptyFields();
      },
      error: (error) => {
        console.error('Error retrieving fields:', error);
      }
    });
  }

  loadActiveFields() {
    this.activeFieldService.getAllActiveFieldsWithoutEndDate().subscribe({
      next: (response) => {
        console.log('Active fields retrieved successfully:', response);
        this.activeFields = response;
        this.filterEmptyFields();
      },
      error: (error) => {
        console.error('Error retrieving active fields:', error);
      }
    });
  }

  private filterEmptyFields(): void {
    if (!this.fields || !this.activeFields) {
      return;
    }
    const activeFieldIds = new Set(this.activeFields.map(af => af.fieldId));
    this.emptyFields = this.fields.filter(field => !activeFieldIds.has(field.id));
  }

  toggleSection(section: string, event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (target.closest('.add-btn')) {
      return;
    }

    if (this.openSections.has(section)) {
      this.openSections.delete(section);
    } else {
      this.openSections.add(section);
    }
  }

  loadCropTypes() {
    this.cropTypeService.getAll().subscribe({
      next: (types) => (this.cropTypes = types),
      error: (err) => console.error('Failed to load crop types', err),
    });
  }

  loadSeeds() {
    this.seedService.getAll().subscribe({
      next: (types) => (this.seeds = types),
      error: (err) => console.error('Failed to load crop types', err),
    });
  }

  isSectionOpen(section: string): boolean {
    return this.openSections.has(section);
  }

  loadTools(): void {
    this.toolService.getAll().subscribe({
      next: tools => {
        this.tools = tools;
        this.filterOutVehiclesFromTools();
      },
      error: err => {
        console.error('Failed to load tools', err);
      }
    });
  }

  loadVehicles(): void {
    this.vehicleService.getAll().subscribe({
      next: vehicles => {
        this.vehicles = vehicles;
        this.filterOutVehiclesFromTools();
      },
      error: err => {
        console.error('Failed to load vehicles', err);
      }
    });
  }

  loadGarages(): void {
    this.garageService.getAll().subscribe({
      next: garages => {
        this.garages = garages;
      },
      error: err => {
        console.error('Failed to load garages', err);
      }
    });
  }

  private filterOutVehiclesFromTools(): void {
    if (!this.tools || !this.vehicles) {
      return;
    }

    const vehicleIds = new Set(this.vehicles.map(v => v.id));

    this.tools = this.tools.filter(tool => !vehicleIds.has(tool.id));
  }

  onMapReady(map: MapComponent) {
    this.mapComponent = map;

    this.locatorService.getAllLocatedObjects().subscribe({
      next: (objects) => {
        this.mapComponent.drawLocatedObjects(objects);
      },
      error: (error) => {
        console.error('Error retrieving fields:', error);
      }
    })
  }


  /**
   * Zoom map to show a specific field
   */
  zoomToField(fieldId: number, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }

    if (this.mapComponent) {
      this.mapComponent.zoomToField(fieldId);
    }
  }

  /**
   * Navigate to field details page
   */
  navigateToField(fieldId: number): void {
    this.router.navigate(['/field/details', fieldId]);
  }

  /**
   * Zoom map to show specific equipment/vehicle marker
   */
  zoomToEquipment(id: number, type: 'tool' | 'vehicle', event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }

    const item = type === 'vehicle'
      ? this.vehicles.find(v => v.id === id)
      : this.tools.find(t => t.id === id);

    if (!item?.locationDto) return;

    this.mapComponent.zoomToLocation(item.locationDto.lat, item.locationDto.lon, 18);
  }

  /**
   * Zoom map to show specific garage marker
   */
  zoomToGarage(id: number, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }

    const garage = this.garages.find(g => g.id === id);
    if (!garage?.coordinate) return;

    this.mapComponent.zoomToLocation(garage.coordinate.lat, garage.coordinate.lon, 18);
  }
}
