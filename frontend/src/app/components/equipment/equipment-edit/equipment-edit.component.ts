import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs';

import {ToolService} from '../../../services/tool.service';
import {ToolCategoryService} from '../../../services/tool-category.service';
import {GarageService} from '../../../services/garage.service';
import {VehicleService} from '../../../services/vehicle.service';

import {Garage, Tool, ToolCategory} from '../../../dtos/tool';
import {Vehicle} from '../../../dtos/vehicle';
import {MapComponent} from '../../shared/map/map.component';

import {ActionButton, BackAction} from '../../shared/panel-layout/data';
import {LocatedObjectService} from "../../../services/located-object.service";

@Component({
  selector: 'app-equipment-edit',
  standalone: false,
  templateUrl: './equipment-edit.component.html',
  styleUrls: ['./equipment-edit.component.scss']
})
export class EquipmentEditComponent implements OnInit, AfterViewInit {
  categoryInputFocused: boolean = false;
  isSubmitting: boolean = false;
  loading: boolean = false;
  isVehicle: boolean = true;
  showCancelDialog = false;

  selectedTool: Tool | null = null;
  toolId: number | null = null;
  categories: ToolCategory[] = [];
  filteredCategories: ToolCategory[] = [];
  garages: Garage[] = [];
  selectedImageName: string | null = null;
  imagePreview: string | null = null;
  submitError: string | null = null;
  loadError: string | null = null;

  form: FormGroup;
  private mapReady = false;

  actionButtons: ActionButton[] = [];
  backAction: BackAction = {
    action: () => this.cancel(),
  };

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private toolService: ToolService,
    private toolCategoryService: ToolCategoryService,
    private vehicleService: VehicleService,
    private garageService: GarageService,
    private locatorService: LocatedObjectService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      category: ['', Validators.required],
      garageId: ['', Validators.required],
      location: ["0,0", [Validators.required, Validators.pattern(/^-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?$/)]],
      remainingValue: [0, [Validators.required, Validators.min(0)]],
      usable: [true],
      image: [null],
      drivenKm: [0],
      fuelLevel: [0],
    });
  }

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;

  ngAfterViewInit(): void {
    this.mapReady = true;
  }

  private placeMarkerIfReady() {
    if (!this.mapReady || !this.mapComponent) {
      return;
    }

    const locationValue = this.form.get('location')?.value;

    if (locationValue) {
      const [lat, lon] = locationValue.split(',').map(coord => parseFloat(coord.trim()));

      if (!isNaN(lat) && !isNaN(lon)) {
        this.mapComponent.addMarker(lat, lon, 'Tool Location', true, this.isVehicle ? 'assets/marker/tractor.svg' : 'assets/marker/tool.svg');
      }
    }
  }


  get panelTitle(): string {
    return this.isVehicle ? 'Edit Vehicle' : 'Edit Equipment';
  }

  get panelSubtitle(): string {
    return this.isVehicle
      ? 'Update the selected vehicle'
      : 'Update the selected equipment';
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.isVehicle = !!data['isVehicle'];

      const idParam = this.route.snapshot.paramMap.get('id');
      this.toolId = idParam ? +idParam : null;

      if (this.toolId != null) {
        this.loadToolOrVehicle(this.toolId);
      } else {
        this.loadError = 'Invalid equipment id.';
      }

      if (this.isVehicle) {
        this.enableVehicleValidators();
      } else {
        this.disableVehicleValidators();
      }

      this.setupActionButtons();
    });

    this.loading = true;
    this.loadGarages();
    this.loadCategories();
    this.loading = false;

    this.form
      .get('category')!
      .valueChanges.pipe(debounceTime(200), distinctUntilChanged())
      .subscribe(value => {
        const search = (value || '').toLowerCase();
        if (!search) {
          this.filteredCategories = this.categories;
        } else {
          this.filteredCategories = this.categories.filter(c =>
            c.name.toLowerCase().includes(search),
          );
        }
      });
    setTimeout(() => {
      if (this.mapComponent) {
        this.placeMarkerIfReady();
      }
    }, 100);
  }

  private setupActionButtons(): void {
    this.actionButtons = [
      {
        label: 'Save Changes',
        color: 'primary',
        action: () => this.onSubmit(),
      },
    ];
  }

  private enableVehicleValidators(): void {
    this.form.get('drivenKm')?.setValidators([Validators.min(0)]);
    this.form.get('fuelLevel')?.setValidators([Validators.min(0), Validators.max(100)]);
    this.form.get('drivenKm')?.updateValueAndValidity();
    this.form.get('fuelLevel')?.updateValueAndValidity();
  }

  private disableVehicleValidators(): void {
    this.form.get('drivenKm')?.clearValidators();
    this.form.get('fuelLevel')?.clearValidators();
    this.form.get('drivenKm')?.updateValueAndValidity();
    this.form.get('fuelLevel')?.updateValueAndValidity();
  }

  private loadCategories(): void {
    this.loadError = null;
    this.toolCategoryService.getAll().subscribe({
      next: categories => {
        this.categories = categories;
        this.filteredCategories = categories;
      },
      error: err => {
        console.error('Failed to load categories', err.error);
        this.loadError = err.error;
      },
    });
  }

  private loadGarages(): void {
    this.loadError = null;
    this.garageService.getAll().subscribe({
      next: garages => {
        this.garages = garages;
      },
      error: err => {
        console.error('Failed to load garages', err.error);
        this.loadError = err.error;
      },
    });
  }

  private loadToolOrVehicle(id: number): void {
    this.loadError = null;

    const obs = this.isVehicle
      ? this.vehicleService.getById(id)
      : this.toolService.getById(id);

    obs.subscribe({
      next: (data: Tool | Vehicle) => {
        const tool = data as Tool;
        const vehicle = data as Vehicle;

        this.form.patchValue({
          name: tool.name,
          description: tool.description,
          remainingValue: tool.remainingValue,
          usable: tool.usable,
          image: tool.image ?? null,
          location:
            tool.locationDto.lat +
            ',' +
            tool.locationDto.lon,
          category: tool.categoryDto?.name ?? '',
          garageId: tool.garageDto?.id ?? null,
          drivenKm: this.isVehicle ? vehicle.drivenKm : 0,
          fuelLevel: this.isVehicle ? vehicle.fuelLevel : 0,
        });

        this.form.markAsPristine();
        this.selectedTool = tool;

        if (tool.image) {
          this.imagePreview = 'data:image/*;base64,' + tool.image;
        }
      },
      error: err => {
        console.error('Failed to load equipment', err.error);
        this.loadError = err.error;
      },
    });
  }

  onCategoryInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value ?? '';
    const term = value.trim().toLowerCase();

    if (!term) {
      this.filteredCategories = [];
      this.categoryInputFocused = false;
      return;
    }

    this.filteredCategories = this.categories.filter(cat =>
      cat.name.toLowerCase().includes(term),
    );

    this.categoryInputFocused = this.filteredCategories.length > 0;
  }

  onCategoryFocus(): void {
    this.categoryInputFocused = true;
  }

  onCategoryBlur(): void {
    setTimeout(() => {
      this.categoryInputFocused = false;
    }, 150);
  }

  selectCategory(name: string): void {
    this.form.patchValue({category: name});
    this.categoryInputFocused = false;
  }

  onSubmit(): void {
    if (this.form.invalid || this.toolId == null) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.submitError = null;

    const value = this.form.value;
    const categoryName: string = (value.category || '').trim();

    let categoryToSave = this.categories.find(
      c => c.name.toLowerCase() === categoryName.toLowerCase(),
    );

    if (!categoryToSave) {
      categoryToSave = {id: null, name: categoryName};
    }

    const selectedGarageId = Number(value.garageId);
    const garageToSave = this.garages.find(g => g.id === selectedGarageId);

    if (!garageToSave) {
      console.error('Garage not found for ID:', selectedGarageId);
      console.log('Garages:', this.garages);
      this.isSubmitting = false;
      return;
    }

    const body: Tool = {
      id: this.toolId,
      name: value.name,
      description: value.description,
      remainingValue: value.remainingValue,
      usable: value.usable,
      image: value.image,
      categoryDto: categoryToSave,
      garageDto: garageToSave,
      locationDto: {
        lat: Number(value.location.split(',')[0]),
        lon: Number(value.location.split(',')[1])
      },
    };

    let request$;

    if (this.isVehicle) {
      const vehicleBody: Vehicle = {
        ...(body as any),
        drivenKm: value.drivenKm,
        fuelLevel: value.fuelLevel,
      };

      request$ = this.vehicleService.update(this.toolId, vehicleBody);
    } else {
      request$ = this.toolService.update(this.toolId, body);
    }

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        const detailPrefix = this.isVehicle ? '/vehicle/detail' : '/equipment/detail';
        this.router.navigate([`${detailPrefix}/${this.toolId}`]);
      },
      error: err => {
        this.isSubmitting = false;
        console.error('Failed to save equipment/vehicle', err.error);
        this.submitError = err.error;
      },
    });
  }

  onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];
    this.selectedImageName = file.name;

    const reader = new FileReader();
    reader.onload = (e: ProgressEvent<FileReader>) => {
      const result = e.target?.result as string;
      this.imagePreview = result;

      const base64 = result.split(',')[1];

      this.form.patchValue({
        image: base64,
      });
    };

    reader.readAsDataURL(file);
  }

  getImageUploadButtonText(): string {
    return this.form.get('image')?.value ? 'Change image' : 'Upload image';
  }

  cancel(): void {
    if (!this.form.dirty) {
      this.navigateBack();
      return;
    }
    this.showCancelDialog = true;
  }

  confirmDiscardChanges(): void {
    this.showCancelDialog = false;
    this.navigateBack();
  }

  keepEditing(): void {
    this.showCancelDialog = false;
  }

  private navigateBack(): void {
    const detailPrefix = this.isVehicle ? '/vehicle/detail' : '/equipment/detail';

    if (this.toolId != null) {
      this.router.navigate([`${detailPrefix}/${this.toolId}`]);
    } else {
      this.router.navigate(['/map-overview']);
    }
  }


  onMarkerDragged(coords: { lat: number; lon: number }) {
    this.form.patchValue({
      location: `${coords.lat},${coords.lon}`
    });
  }

  onMapClicked(coords: { lat: number; lon: number }) {
    this.form.patchValue({
      location: `${coords.lat},${coords.lon}`
    });

    this.mapComponent.clearMarker();
    this.mapComponent.addMarker(coords.lat, coords.lon, this.form.value.name, true, 'assets/marker/tool.svg');
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
