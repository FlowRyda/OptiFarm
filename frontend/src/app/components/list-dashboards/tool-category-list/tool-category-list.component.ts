import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {PageEvent} from "@angular/material/paginator";
import {ToolCategory} from "./toolCategory";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-tool-category-list',
  templateUrl: './tool-category-list.component.html',
  standalone: false,
  styleUrl: './tool-category-list.component.scss'
})
export class ToolCategoryListComponent {
  config: EntityListConfig = {
    title: 'Tool Category Dashboard',
    subtitle: 'Displaying all tool categories',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<ToolCategory>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'name'}
  ];

  rows: ToolCategory[] = [];
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
      .getAllToolCategories(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<ToolCategory>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load tool categories', err);
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
