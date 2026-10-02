import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {ActiveField} from "./activeField";
import {PageEvent} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-active-field-list',
  templateUrl: './active-field-list.component.html',
  standalone: false,
  styleUrl: './active-field-list.component.scss'
})
export class ActiveFieldListComponent {
  config: EntityListConfig = {
    title: 'Active Fields Dashboard',
    subtitle: 'Displaying all owned fields with their current status',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<ActiveField>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'field.name'},
    {header: 'Area', valueGetter: (f) => `${f.area.toFixed(2)} ha`, sortable: true, sortField: 'area'},
    {header: 'Seed', field: 'seed'},
    {
      header: 'Start Date',
      valueGetter: (f) => f.startDate ? new Date(f.startDate).toLocaleDateString() : '',
      sortable: true,
      sortField: 'startDate'
    },
    {
      header: 'End Date',
      valueGetter: (f) => f.endDate ? new Date(f.endDate).toLocaleDateString() : '',
      sortable: true,
      sortField: 'endDate'
    }, {header: 'Growth', valueGetter: (f) => `${f.growthStatus ?? 0}%`, sortable: true, sortField: 'growthStatus'},
  ];

  rows: ActiveField[] = [];

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
      .getAllActiveFields(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<ActiveField>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load fields', err);
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
    this.router.navigate(['field', 'create']);
  }

  onEdit(selected: ActiveField[]) {
    this.router.navigate(['field', 'edit', selected[0].id]);
  }

  onDelete(selected: ActiveField[]) {
    this.router.navigate(['field', 'details', selected[0].id]);
  }
}
