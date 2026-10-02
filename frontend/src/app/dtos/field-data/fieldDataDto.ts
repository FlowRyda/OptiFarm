
export interface FieldDataDto {
  id?: number;
  fieldName: string;
  plantedCrops: Map<String, Number>;
  totalEarnings: number;
  diffPrevYear: number;
  harvestedCropsTimeSeries: Map<Number, Number>;
}
