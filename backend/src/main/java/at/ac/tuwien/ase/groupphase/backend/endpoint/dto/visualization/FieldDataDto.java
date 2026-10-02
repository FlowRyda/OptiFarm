package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization;

import java.util.Map;

public class FieldDataDto {

    private Long fieldId;

    private String fieldName;

    private Map<String, Integer> plantedCrops;

    private double totalEarnings;

    private double diffPrevYear;

    private Map<Integer, Double> harvestedCropsTimeSeries;

    public FieldDataDto() {
    }

    public FieldDataDto(Long fieldId, String fieldName, Map<String, Integer> plantedCrops, double totalEarnings,
                   double diffPrevYear, Map<Integer, Double> harvestedCropsTimeSeries) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.plantedCrops = plantedCrops;
        this.totalEarnings = totalEarnings;
        this.diffPrevYear = diffPrevYear;
        this.harvestedCropsTimeSeries = harvestedCropsTimeSeries;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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

    public double getDiffPrevYear() {
        return diffPrevYear;
    }

    public void setDiffPrevYear(double diffPrevYear) {
        this.diffPrevYear = diffPrevYear;
    }

    public Map<Integer, Double> getHarvestedCropsTimeSeries() {
        return harvestedCropsTimeSeries;
    }

    public void setHarvestedCropsTimeSeries(Map<Integer, Double> harvestedCropsTimeSeries) {
        this.harvestedCropsTimeSeries = harvestedCropsTimeSeries;
    }

    @Override
    public String toString() {
        return "FieldDataDto{"
            + "fieldId=" + fieldId
            + ", fieldName='" + fieldName
            + ", plantedCrops=" + plantedCrops
            + ", totalEarnings=" + totalEarnings
            + ", diffPrevYear=" + diffPrevYear
            + ", harvestedCropsTimeSeries=" + harvestedCropsTimeSeries
            + '}';
    }
}
