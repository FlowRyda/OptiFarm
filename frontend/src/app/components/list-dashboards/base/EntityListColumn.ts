export interface EntityListColumn<T = any> {
  header: string;
  field?: keyof T | string;
  valueGetter?: (row: T) => string | number;

  sortable?: boolean;
  sortField?: string;
}
