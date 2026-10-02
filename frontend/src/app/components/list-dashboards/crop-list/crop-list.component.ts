import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {Crop} from "./crop";
import {PageEvent} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-crop-list',
  templateUrl: './crop-list.component.html',
  standalone: false,
  styleUrl: './crop-list.component.scss'
})
export class CropListComponent {
  config: EntityListConfig = {
    title: 'Crop Dashboard',
    subtitle: 'Displaying all owned crops',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };


  columns: EntityListColumn<Crop>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'type.name'},
    {
      header: 'Current Price',
      valueGetter: (c) => `${c.currentPrice.toFixed(2)} $`, sortable: true, sortField: 'currentPrice'
    },
    {
      header: 'Quantity',
      valueGetter: (c) => `${c.quantity} tons`, sortable: true, sortField: 'quantity'
    },
    {
      header: 'Harvest Date',
      valueGetter: (f) => f.harvestDate ? new Date(f.harvestDate).toLocaleDateString() : '',
      sortable: true,
      sortField: 'harvestDate'
    },
  ];

  rows: Crop[] = [];

  pageIndex = 0;
  pageSize = 20;
  sortActive: string | number | symbol | null = null;
  sortDirection: 'asc' | 'desc' | '' = '';
  totalElements = 0;

  isLoading = false;

  constructor(private dashboardService: DashboardListService) {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.isLoading = true;

    this.dashboardService
      .getAllCrops(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<Crop>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load crops', err);
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
}
