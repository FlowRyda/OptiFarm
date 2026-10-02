import {Component} from '@angular/core';
import {EntityListConfig} from '../base/EntityListConfig';
import {EntityListColumn} from '../base/EntityListColumn';
import {Equipment} from "./equipment";
import {PageEvent} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {DashboardListService} from "src/app/services/dashboard-list.service";
import {Page} from "src/app/dtos/page";
import {SortEvent} from "../base/SortEvent";
import {ToolService} from "../../../services/tool.service";
import {forkJoin, map} from "rxjs";
import {QrcodeService} from "../../../services/qrcode.service";


@Component({
  selector: 'app-equipment-list',
  templateUrl: './equipment-list.component.html',
  standalone: false,
  styleUrls: ['./equipment-list.component.scss'],
})
export class EquipmentListComponent {
  config: EntityListConfig = {
    title: 'Equipment Dashboard',
    subtitle: 'Displaying all owned Equipment with their current status',
    addLabel: 'Add',
    editLabel: 'Edit',
    deleteLabel: 'Delete',
    multiSelectLabel: "Generate QR Code"
  };

  columns: EntityListColumn<Equipment>[] = [
    {header: 'Name', field: 'name', sortable: true},
    {
      header: 'Fuel Level',
      valueGetter: (t) => `${t.fuelLevel} %`,
      sortField: 'fuelLevel', // backend field name
      sortable: false,
    },
    {
      header: 'Driven Km',
      valueGetter: (t) => t.drivenKm.toFixed(3),
      sortField: 'drivenKm',
      sortable: false,
    },
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

  rows: Equipment[] = [];

  pageIndex = 0;
  pageSize = 20;
  sortActive: string | number | symbol | null = null;
  sortDirection: 'asc' | 'desc' | '' = '';
  totalElements = 0;

  isLoading = false;

  constructor(private dashboardService: DashboardListService, private toolService: ToolService, private router: Router, private qrCodeService: QrcodeService) {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.isLoading = true;

    this.dashboardService
      .getAllEquipment(page, this.pageSize, this.sortActive || undefined,
        this.sortDirection || undefined)
      .subscribe({
        next: (pageResult: Page<Equipment>) => {
          console.log(pageResult.content);
          this.rows = pageResult.content;
          this.pageIndex = pageResult.number;
          this.pageSize = pageResult.size;
          this.totalElements = pageResult.totalElements;
          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load equipment', err);
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
    this.router.navigate(['equipment', 'create']);
  }

  onEdit(selected: Equipment[]) {
    console.log(selected);

    if (selected[0].isVehicle === true) {
      this.router.navigate(['vehicle', 'edit', selected[0].id]);
    } else {
      this.router.navigate(['equipment', 'edit', selected[0].id]);
    }
  }

  onDelete(selected: Equipment[]) {
    if (selected[0].isVehicle === true) {
      this.router.navigate(['vehicle', 'detail', selected[0].id]);
    } else {
      this.router.navigate(['equipment', 'detail', selected[0].id]);
    }
  }

  async onMultiSelectAction(selected: Equipment[]) {

    const requests = selected.map(equipment =>
      this.toolService.generateQrCode(equipment.id).pipe(
        map(blob => ({
          name: equipment.name,
          blob
        }))
      )
    );

    forkJoin(requests).subscribe({
      next: async qrItems => {
        await this.qrCodeService.createQrDocument(qrItems);
      },
      error: err => console.error(err)
    });
  }


}
