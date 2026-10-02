import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SeedService} from '../../../services/seed.service';
import {CroptypeService} from '../../../services/croptype.service';
import {Seed} from '../../../dtos/seed/seed';
import {CropType} from '../../../dtos/crop-type/crop-type';
import {Router} from '@angular/router';
import {ActionButton, BackAction} from '../../shared/panel-layout/data';

@Component({
  selector: 'app-seed',
  templateUrl: './seed.component.html',
  styleUrls: ['./seed.component.scss'],
  standalone: false
})
export class SeedComponent implements OnInit {

  public seedForm: FormGroup;
  cropTypes: CropType[] = [];

  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  actionButtons: ActionButton[] = [];
  backAction: BackAction = {
    action: () => this.cancel()
  };

  constructor(
    private fb: FormBuilder,
    public router: Router,
    private seedService: SeedService,
    private cropTypeService: CroptypeService
  ) {
    this.seedForm = this.fb.group({
      cropTypeId: ['', Validators.required],
      buyingPrice: ['', [Validators.required, Validators.min(0)]],
      buyingTime: ['', Validators.required],
      name: ['', Validators.required]
    });
  }

  get panelTitle(): string {
    return 'Create New Seed';
  }

  get panelSubtitle(): string {
    return 'Add seed information for a crop type';
  }

  ngOnInit(): void {
    this.updateActionButtons();
    this.loadCropTypes();
  }

  private updateActionButtons(): void {
    this.actionButtons = [
      {
        label: 'Create Seed',
        color: 'primary',
        action: () => this.onSubmit(),
      },
    ];
  }

  loadCropTypes(): void {
    this.cropTypeService.getAll().subscribe({
      next: (types) => this.cropTypes = types,
      error: () => this.errorMessage = 'Failed to load crop types.'
    });
  }

  get cropTypeId() {
    return this.seedForm.get('cropTypeId');
  }

  get buyingPrice() {
    return this.seedForm.get('buyingPrice');
  }

  get buyingTime() {
    return this.seedForm.get('buyingTime');
  }

  get name() {
    return this.seedForm.get('name');
  }

  onSubmit(): void {
    if (this.seedForm.invalid) {
      this.seedForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const newSeed: Seed = {
      id: 0,
      cropTypeId: Number(this.seedForm.value.cropTypeId),
      buyingPrice: Number(this.seedForm.value.buyingPrice),
      buyingTime: this.seedForm.value.buyingTime,
      name: this.seedForm.value.name
    };

    this.seedService.createCropType(newSeed).subscribe({
      next: (response) => {
        this.successMessage = `Seed for CropType ID ${response.cropTypeId} added successfully!`;
        this.seedForm.reset();
        this.isSubmitting = false;
        this.router.navigate([`/seed/${response.id}`]);

      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to add seed. Please try again.';
        this.isSubmitting = false;
      }
    });
  }

  cancel(): void {
    if (this.seedForm.dirty) {
      const confirmed = confirm('Discard changes? All unsaved changes will be lost.');
      if (!confirmed) return;
    }
    this.router.navigate(['/map-overview']);
  }
}
