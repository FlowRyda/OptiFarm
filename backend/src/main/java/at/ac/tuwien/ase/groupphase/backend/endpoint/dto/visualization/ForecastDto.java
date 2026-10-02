package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization;


import java.util.Map;

public class ForecastDto {

    private Long id;

    private Map<Integer, Double> forecastTimeSeries;
    private Map<Integer, Double> lowerConfidenceIntervalTimeSeries;
    private Map<Integer, Double> upperConfidenceIntervalTimeSeries;


    public ForecastDto() {
    }

    public ForecastDto(Long id, Map<Integer, Double> forecastTimeSeries,
                       Map<Integer, Double> lowerConfidenceIntervalTimeSeries,
                       Map<Integer, Double> upperConfidenceIntervalTimeSeries) {
        this.id = id;
        this.forecastTimeSeries = forecastTimeSeries;
        this.lowerConfidenceIntervalTimeSeries = lowerConfidenceIntervalTimeSeries;
        this.upperConfidenceIntervalTimeSeries = upperConfidenceIntervalTimeSeries;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<Integer, Double> getForecastTimeSeries() {
        return forecastTimeSeries;
    }

    public void setForecastTimeSeries(Map<Integer, Double> forecastTimeSeries) {
        this.forecastTimeSeries = forecastTimeSeries;
    }

    public Map<Integer, Double> getLowerConfidenceIntervalTimeSeries() {
        return lowerConfidenceIntervalTimeSeries;
    }

    public void setLowerConfidenceIntervalTimeSeries(Map<Integer, Double> lowerConfidenceIntervalTimeSeries) {
        this.lowerConfidenceIntervalTimeSeries = lowerConfidenceIntervalTimeSeries;
    }

    public Map<Integer, Double> getUpperConfidenceIntervalTimeSeries() {
        return upperConfidenceIntervalTimeSeries;
    }

    public void setUpperConfidenceIntervalTimeSeries(Map<Integer, Double> upperConfidenceIntervalTimeSeries) {
        this.upperConfidenceIntervalTimeSeries = upperConfidenceIntervalTimeSeries;
    }

    @Override
    public String toString() {
        return "ForecastDto{"
            + "id=" + id
            + ", forecastTimeSeries=" + forecastTimeSeries
            + ", lowerConfidenceIntervalTimeSeries=" + lowerConfidenceIntervalTimeSeries
            + ", upperConfidenceIntervalTimeSeries=" + upperConfidenceIntervalTimeSeries
            + '}';
    }
}