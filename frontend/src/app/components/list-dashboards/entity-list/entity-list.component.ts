import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EntityListConfig} from '../base/EntityListConfig';
import {EntityListColumn} from '../base/EntityListColumn';
import {SortEvent} from "../base/SortEvent";

@Component({
  selector: 'app-entity-list',
  templateUrl: './entity-list.component.html',
  styleUrls: ['./entity-list.component.scss'],
  standalone: false
})
export class EntityListComponent<T = any> {
  @Input() config!: EntityListConfig;
  @Input() columns: EntityListColumn<T>[] = [];
  @Input() rows: T[] = [];
  @Input() hideAdd: boolean = false;
  @Input() hideDelete: boolean = false;
  @Input() hideEdit: boolean = false;
  @Input() hideMultiSelectAction: boolean = true;

  @Input() selectionMode: 'single' | 'multiple' = 'single';

  @Output() add = new EventEmitter<void>();
  @Output() edit = new EventEmitter<T[]>();
  @Output() delete = new EventEmitter<T[]>();
  @Output() multiSelectAction = new EventEmitter<T[]>();
  @Output() sortChange = new EventEmitter<SortEvent>();

  selectedRows: T[] = [];
  sortActive: string | number | symbol | null = null;
  sortDirection: 'asc' | 'desc' | '' = '';

  get hasSelection(): boolean {
    return this.selectedRows.length === 1;
  }

  get hasMultiSelection(): boolean {
    return this.selectedRows.length >= 1;
  }

  isSelected(row: T): boolean {
    return this.selectedRows.includes(row);
  }

  onRowClick(row: T) {
    if (this.selectionMode === 'single') {
      this.selectedRows = [row];
    } else {
      this.toggleRow(row);
    }
  }

  toggleRow(row: T, event?: MouseEvent) {
    if (event) {
      event.stopPropagation();
    }

    const idx = this.selectedRows.indexOf(row);
    if (idx >= 0) {
      this.selectedRows.splice(idx, 1);
    } else {
      if (this.selectionMode === 'single') {
        this.selectedRows = [row];
      } else {
        this.selectedRows = [...this.selectedRows, row];
      }
    }
  }

  onAddClick() {
    this.add.emit();
  }

  onEditClick() {
    this.edit.emit(this.selectedRows);
  }

  onDeleteClick() {
    this.delete.emit(this.selectedRows);
  }

  onMultiSelectClick() {
    this.multiSelectAction.emit(this.selectedRows);
  }

  getCellValue(col: EntityListColumn<T>, row: T): any {
    if (col.valueGetter) {
      return col.valueGetter(row);
    }
    if (col.field != null) {
      return (row as any)[col.field as any];
    }
    return '';
  }

  isColumnSortable(col: EntityListColumn<T>): boolean {
    return (col.sortable !== false) && !!this.getSortField(col);
  }

  getSortField(col: EntityListColumn<T>): string | number | symbol | null {
    return col.sortField || (col.field ?? null);
  }

  onHeaderClick(col: EntityListColumn<T>) {
    if (!this.isColumnSortable(col)) {
      return;
    }

    const field = this.getSortField(col);
    if (!field) {
      return;
    }

    // toggle sort direction
    if (this.sortActive !== field) {
      this.sortActive = field;
      this.sortDirection = 'asc';
    } else {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else if (this.sortDirection === 'desc') {
        // third click clears sorting
        this.sortDirection = '';
        this.sortActive = null;
      } else {
        this.sortDirection = 'asc';
      }
    }

    this.sortChange.emit({
      active: this.sortActive ?? '',
      direction: this.sortDirection,
    });
  }
}
