
export interface FarmDataDto {
  id?: number;
  plantedCrops: Map<String, Number>;
  totalEarnings: number;
  diffPrevYear: number;
  farmTimeSeries: Map<Number, Number>;
  fieldTimeSeries: Map<Number, Number>[];
}
