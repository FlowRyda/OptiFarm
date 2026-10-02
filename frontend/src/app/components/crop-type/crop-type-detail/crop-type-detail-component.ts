import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CropType} from "../../../dtos/crop-type/crop-type";
import {CroptypeService} from "../../../services/croptype.service";
import {ActionButton, BackAction, DetailConfig, InfoField} from "../../shared/panel-layout/data";

@Component({
  selector: 'crop-type-detail',
  templateUrl: './crop-type-detail-component.html',
  styleUrls: ['./crop-type-detail-component.scss'],
  standalone: false
})
export class CropTypeDetailComponent implements OnInit {

  detailConfig?: DetailConfig<CropType>;
  cropType?: CropType;
  isLoading = true;
  errorMessage = '';

  showDeleteConfirmation = false;

  backAction: BackAction = {
    action: () => this.router.navigate(['/map-overview']),
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cropTypeService: CroptypeService
  ) {
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const actionButtons: ActionButton[] = [
      {label: 'Edit', color: 'primary', action: () => this.editCropType()},
      {label: 'Delete', color: 'warn', action: () => this.deleteCropType()},
    ];

    if (id) {
      this.cropTypeService.getById(id).subscribe({
        next: (crop) => {
          this.cropType = crop;
          this.isLoading = false;

          const infoFields: InfoField[] = [
            {label: 'Name', value: crop.name},
            {label: 'Growth Duration (days)', value: crop.growthDuration},
          ];

          this.detailConfig = {
            title: crop.name,
            subtitle: 'Crop Type Details',
            actionButtons: actionButtons,
            backAction: this.backAction,
            fields: infoFields,
            data: crop,
          };
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Failed to load crop type.';
          this.isLoading = false;
        }
      });
    } else {
      this.errorMessage = 'Invalid crop type ID.';
      this.isLoading = false;
    }
  }

  editCropType() {
    if (this.cropType) {
      this.router.navigate(['/crop-types', this.cropType.id, 'edit']);
    }
  }

  deleteCropType() {
    this.showDeleteConfirmation = true;
  }

  confirmDelete() {
    if (!this.cropType) return;

    this.cropTypeService.delete(this.cropType.id).subscribe({
      next: () => {
        this.router.navigate(['/map-overview']);
      },
      error: (err) => {
        console.error(err);
        alert('Failed to delete crop type.');
      }
    });

    this.closeDeleteModal();
  }

  closeDeleteModal() {
    this.showDeleteConfirmation = false;
  }
}
