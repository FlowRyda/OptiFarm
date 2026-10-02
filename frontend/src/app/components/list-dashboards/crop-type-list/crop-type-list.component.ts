import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {PageEvent} from "@angular/material/paginator";
import {CropType} from "./cropType";
import {Seed} from "../seed-list/seed";
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-crop-type-list',
  templateUrl: './crop-type-list.component.html',
  standalone: false,
  styleUrl: './crop-type-list.component.scss'
})
export class CropTypeListComponent {
  config: EntityListConfig = {
    title: 'Crop Type Dashboard',
    subtitle: 'Displaying all crop types',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<CropType>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'name'},
  ];

  rows: CropType[] = [];
  pageIndex = 0;
  pageSize = 20;
  sortActive: string | number | symbol | null = null;
  sortDirection: 'asc' | 'desc' | '' = '';
  totalElements = 0;

  isLoading = false;

  constructor(private dashboardService: DashboardListService, private router: Router) {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.isLoading = true;

    this.dashboardService
      .getAllCropTypes(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<CropType>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load crop types', err);
          this.isLoading = false;
        },
      });
  }

  onSortChange(event: SortEvent) {
    this.sortActive = event.active || null;
    this.sortDirection = event.direction;

    this.loadPage(0);
  }


  onMatPage(e: PageEvent) {
    this.pageSize = e.pageSize;
    this.loadPage(e.pageIndex);
  }

  onAdd() {
    this.router.navigate(['cropType']);
  }

  onEdit(selected: CropType[]) {
    this.router.navigate(['crop-types', selected[0].id, 'edit']);
  }

  onDelete(selected: CropType[]) {
    this.router.navigate(['cropType', selected[0].id]);
  }
}
