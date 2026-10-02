import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {ActiveField} from "../active-field-list/activeField";
import {SortEvent} from "../base/SortEvent";
import {PageEvent} from "@angular/material/paginator";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Router} from "@angular/router";
import {Page} from "src/app/dtos/page";

@Component({
  selector: 'app-field-list',
  standalone: false,
  templateUrl: './field-list.component.html',
  styleUrl: './field-list.component.scss'
})
export class FieldListComponent {
  config: EntityListConfig = {
    title: 'Fields Dashboard',
    subtitle: 'Displaying all owned fields',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<ActiveField>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'name'},
    {header: 'Area', valueGetter: (f) => `${f.area.toFixed(2)} ha`, sortable: true, sortField: 'area'}];

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
      .getAllFields(page, this.pageSize, this.sortActive || undefined,
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
