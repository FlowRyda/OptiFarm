
export interface ForecastDto {
  id?: number;
  forecastTimeSeries: Map<Number, Number>;
  lowerConfidenceIntervalTimeSeries: Map<Number, Number>;
  upperConfidenceIntervalTimeSeries: Map<Number, Number>;
}
