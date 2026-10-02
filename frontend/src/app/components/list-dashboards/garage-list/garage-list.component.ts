import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {Garage} from "./garage";
import {PageEvent} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-garage-list',
  templateUrl: './garage-list.component.html',
  standalone: false,
  styleUrl: './garage-list.component.scss'
})
export class GarageListComponent {
  config: EntityListConfig = {
    title: 'Garage Dashboard',
    subtitle: 'Displaying all owned garages',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<Garage>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'name'},
    {header: 'Tools', valueGetter: (g) => `${g.toolCount}`, sortable: true, sortField: 'toolCount'},
    {
      header: 'Latitude',
      valueGetter: (t) => `${t.lat.toFixed(5)}`,
      sortable: false
    },
    {
      header: 'Longitude',
      valueGetter: (t) => `${t.lon.toFixed(5)}`,
      sortable: false
    },
  ];

  rows: Garage[] = [];
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
      .getAllGarages(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<Garage>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load seeds', err);
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
    this.router.navigate(['garage', 'create']);
  }

  onEdit(selected: Garage[]) {
    this.router.navigate(['garage', 'edit', selected[0].id]);
  }

  onDelete(selected: Garage[]) {
    this.router.navigate(['garage', 'details', selected[0].id]);
  }
}
