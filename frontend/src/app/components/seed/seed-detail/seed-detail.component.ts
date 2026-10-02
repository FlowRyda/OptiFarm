import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Seed} from "../../../dtos/seed/seed";
import {CropType} from "../../../dtos/crop-type/crop-type";
import {SeedService} from "../../../services/seed.service";
import {CroptypeService} from "../../../services/croptype.service";
import {ActionButton, BackAction, DetailConfig, InfoField} from "../../shared/panel-layout/data";
import {DatePipe} from '@angular/common';

@Component({
  selector: 'app-seed-detail',
  templateUrl: './seed-detail.component.html',
  styleUrls: ['./seed-detail.component.scss'],
  standalone: false,
  providers: [DatePipe]
})
export class SeedDetailComponent implements OnInit {
  seed?: Seed;
  cropTypes: CropType[] = [];
  cropTypeName = '';
  isLoading = true;
  errorMessage = '';
  detailConfig?: DetailConfig<Seed>;

  showDeleteConfirmation = false;

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private seedService: SeedService,
    private cropTypeService: CroptypeService,
    private datePipe: DatePipe
  ) {
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.errorMessage = 'Invalid seed ID.';
      this.isLoading = false;
      return;
    }

    this.cropTypeService.getAll().subscribe({
      next: (cropTypes) => {
        this.cropTypes = cropTypes;

        this.seedService.getById(id).subscribe({
          next: (seed) => {
            this.seed = seed;

            const foundType = this.cropTypes.find(c => c.id === seed.cropTypeId);
            this.cropTypeName = foundType ? foundType.name : 'Unknown';
            this.isLoading = false;

            const actionButtons: ActionButton[] = [
              {label: 'Edit', color: 'primary', action: () => this.editSeed()},
              {label: 'Delete', color: 'warn', action: () => this.deleteSeed()},
            ];

            const buyingTimeFormatted = this.datePipe.transform(seed.buyingTime, 'dd.MM.yyyy HH:mm');

            const infoFields: InfoField[] = [
              {label: 'Name', value: seed.name},
              {label: 'Buying Price ($)', value: `${seed.buyingPrice.toFixed(2)}$`},
              {label: 'Buying Time', value: buyingTimeFormatted ?? seed.buyingTime},
              {label: 'Crop Type', value: this.cropTypeName},
            ];

            this.detailConfig = {
              title: seed.name,
              subtitle: 'Seed Details',
              actionButtons,
              backAction: this.backAction,
              fields: infoFields,
              data: seed
            };
          },
          error: () => {
            this.errorMessage = 'Failed to load seed details.';
            this.isLoading = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Failed to load crop types.';
        this.isLoading = false;
      }
    });
  }

  editSeed() {
    if (this.seed) {
      this.router.navigate(['/seed', this.seed.id, 'edit']);
    }
  }

  deleteSeed() {
    this.showDeleteConfirmation = true;
  }

  confirmDelete() {
    if (!this.seed) return;

    this.seedService.delete(this.seed.id).subscribe({
      next: () => {
        this.router.navigate(['/map-overview']);
      },
      error: () => {
        alert('Failed to delete seed.');
      }
    });

    this.closeDeleteModal();
  }

  closeDeleteModal() {
    this.showDeleteConfirmation = false;
  }
}
