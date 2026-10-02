package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization;

import java.util.List;
import java.util.Map;


public class FarmDataDto {

    private Long farmerId;

    private Map<String, Integer> plantedCrops;

    private double totalEarnings;

    private double totalQuantity;

    private double diffPrevYear;

    private Map<Integer, Double> farmTimeSeries;

    private List<Map<Integer, Double>> fieldTimeSeries;

    public FarmDataDto() {
    }

    public FarmDataDto(Long farmerId, Map<String, Integer> plantedCrops, double totalEarnings, double totalQuantity,
                        double diffPrevYear, Map<Integer, Double> farmTimeSeries, List<Map<Integer, Double>> fieldTimeSeries) {
        this.farmerId = farmerId;
        this.plantedCrops = plantedCrops;
        this.totalEarnings = totalEarnings;
        this.totalQuantity = totalQuantity;
        this.diffPrevYear = diffPrevYear;
        this.farmTimeSeries = farmTimeSeries;
        this.fieldTimeSeries = fieldTimeSeries;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Map<String, Integer> getPlantedCrops() {
        return plantedCrops;
    }

    public void setPlantedCrops(Map<String, Integer> plantedCrops) {
        this.plantedCrops = plantedCrops;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getDiffPrevYear() {
        return diffPrevYear;
    }

    public void setDiffPrevYear(double diffPrevYear) {
        this.diffPrevYear = diffPrevYear;
    }

    public Map<Integer, Double> getFarmTimeSeries() {
        return farmTimeSeries;
    }

    public void setHarvestedCropsTimeSeries(Map<Integer, Double> farmTimeSeries) {
        this.farmTimeSeries = farmTimeSeries;
    }

    public List<Map<Integer, Double>> getFieldTimeSeries() {
        return fieldTimeSeries;
    }

    public void setFieldTimeSeries(List<Map<Integer, Double>> fieldTimeSeries) {
        this.fieldTimeSeries = fieldTimeSeries;
    }

    @Override
    public String toString() {
        return "FarmDataDto{"
            + "farmerId=" + farmerId
            + ", plantedCrops=" + plantedCrops
            + ", totalEarnings=" + totalEarnings
            + ", totalQuantity=" + totalQuantity
            + ", diffPrevYear=" + diffPrevYear
            + ", farmTimeSeries=" + farmTimeSeries
            + ", fieldTimeSeries=" + fieldTimeSeries
            + '}';
    }
}
