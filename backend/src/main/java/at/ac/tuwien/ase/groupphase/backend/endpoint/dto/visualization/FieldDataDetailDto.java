package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization;

public class FieldDataDetailDto {

    private Long fieldId;

    private String fieldName;

    private double totalEarnings;

    private double totalQuantity;

    private int numberOfHarvests;


    public FieldDataDetailDto() {
    }

    public FieldDataDetailDto(Long fieldId, String fieldName, double totalEarnings, double totalQuantity,
                              int numberOfHarvests) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.totalEarnings = totalEarnings;
        this.totalQuantity = totalQuantity;
        this.numberOfHarvests = numberOfHarvests;
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

    public int getNumberOfHarvests() {
        return numberOfHarvests;
    }

    public void setNumberOfHarvests(int numberOfHarvests) {
        this.numberOfHarvests = numberOfHarvests;
    }

    @Override
    public String toString() {
        return "FieldDataDetailDto{"
            + "fieldId=" + fieldId
            + ", fieldName='" + fieldName + '\''
            + ", totalEarnings=" + totalEarnings
            + ", totalQuantity=" + totalQuantity
            + ", numberOfHarvests=" + numberOfHarvests
            + '}';
    }
}