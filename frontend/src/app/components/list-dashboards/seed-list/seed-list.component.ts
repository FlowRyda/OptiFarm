import {Component} from '@angular/core';
import {EntityListConfig} from "../base/EntityListConfig";
import {EntityListColumn} from "../base/EntityListColumn";
import {Seed} from "./seed";
import {PageEvent} from '@angular/material/paginator';
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-seed-list',
  templateUrl: './seed-list.component.html',
  standalone: false,
  styleUrl: './seed-list.component.scss'
})
export class SeedListComponent {
  config: EntityListConfig = {
    title: 'Seed Dashboard',
    subtitle: 'Displaying all owned seeds',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
  };

  columns: EntityListColumn<Seed>[] = [
    {header: 'Name', field: 'name', sortable: true, sortField: 'name'},
    {
      header: 'Buying Price',
      valueGetter: (s) => `${s.buyingPrice.toFixed(2)} $`,
      sortable: true,
      sortField: 'buyingPrice'
    },
    {
      header: 'Buying Time', valueGetter: (s) => {
        const raw = s.buyingTime;
        if (!raw) return '';
        const date = new Date(raw);
        return date.toLocaleDateString();
      }, sortable: true, sortField: 'buyingTime'
    },
    {header: 'Crop Type', field: 'cropType', sortable: true, sortField: 'type.name'},
  ];

  rows: Seed[] = [];

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
      .getAllSeeds(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<Seed>) => {
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
    this.router.navigate(['seed']);
  }

  onEdit(selected: Seed[]) {
    this.router.navigate(['seed', selected[0].id, 'edit']);
  }

  onDelete(selected: Seed[]) {
    this.router.navigate(['seed', selected[0].id]);
  }
}
