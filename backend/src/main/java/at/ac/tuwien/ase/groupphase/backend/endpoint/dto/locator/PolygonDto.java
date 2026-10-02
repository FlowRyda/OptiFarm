package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;


public class PolygonDto extends LocatedObjectDto {

    private String currentSeed;
    private String color;
    private String fillColor;
    private double fillOpacity = 0.2;
    private double weight = 3;
    private Long fieldId;
    private double[] center;


    public PolygonDto() {
    }

    public PolygonDto(Field field) {
        super(field.getName(), field.getBoundaries());
        this.color = "#00E5FF";
        this.fillColor = "#00E5FF";
        this.fillOpacity = 0.25;
        this.weight = 3;
        this.fieldId = field.getId();
        this.center = calculateCenter(field.getBoundaries());
    }

    public PolygonDto(ActiveField field) {
        this(field.getField());
        this.color = "#FFD500";
        this.fillColor = "#FFD500";
        this.fillOpacity = 0.2 + (field.getGrowthStatus() / 100.0) * 0.6;
        this.currentSeed = field.getSeed().getName();
    }

    public PolygonDto(String title, Polygon lineString) {
        super(title, lineString);
        this.center = calculateCenter(lineString);
    }

    public PolygonDto(String title, Polygon lineString, String color, String fillColor, double fillOpacity, double weight) {
        super(title, lineString);
        this.color = color;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
        this.weight = weight;
        this.center = calculateCenter(lineString);
    }

    private double[] calculateCenter(Polygon polygon) {
        if (polygon == null) {
            return null;
        }
        MaximumInscribedCircle mic = new MaximumInscribedCircle(polygon, 0.00001);
        Point centerPoint = mic.getCenter();
        return new double[]{centerPoint.getY(), centerPoint.getX()};
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public double getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(double fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getCurrentSeed() {
        return currentSeed;
    }

    public void setCurrentSeed(String currentSeed) {
        this.currentSeed = currentSeed;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public double[] getCenter() {
        return center;
    }

    public void setCenter(double[] center) {
        this.center = center;
    }
}