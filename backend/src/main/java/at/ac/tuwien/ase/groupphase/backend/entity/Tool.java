package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.Objects;

@Entity
public class Tool extends LocatedObject {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    private ToolCategory category;

    @Column(nullable = false, length = 4096)
    private String description;

    @Column(nullable = false)
    private double remainingValue;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage homeGarage;

    @Column(nullable = false)
    private boolean isUsable;

    @Column(nullable = true, columnDefinition = "bytea")
    private byte[] image;

    @Column(nullable = true)
    private String token;

    public Tool() {
    }

    public Tool(String name, ToolCategory category, String description, double remainingValue,
                Garage homeGarage, Coordinate coordinate, boolean isUsable, byte[] image) {
        this.setCoordinate(coordinate);
        this.name = name;
        this.category = category;
        this.description = description;
        this.remainingValue = remainingValue;
        this.homeGarage = homeGarage;
        this.isUsable = isUsable;
        this.image = image;
    }

    public Tool(String name, ToolCategory category, String description, double remainingValue, byte[] image, Garage garage, Farmer farmer, Coordinate coordinate, boolean isUsable) {
        super(coordinate, farmer);
        this.name = name;
        this.category = category;
        this.description = description;
        this.remainingValue = remainingValue;
        this.homeGarage = garage;
        this.image = image;
        this.isUsable = isUsable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ToolCategory getCategory() {
        return category;
    }

    public void setCategory(ToolCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRemainingValue() {
        return remainingValue;
    }

    public void setRemainingValue(double value) {
        this.remainingValue = value;
    }

    public Garage getHomeGarage() {
        return homeGarage;
    }

    public void setHomeGarage(Garage homeGarage) {
        this.homeGarage = homeGarage;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public void setUsable(boolean usable) {
        isUsable = usable;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tool tool = (Tool) o;
        return Double.compare(remainingValue, tool.remainingValue) == 0 && isUsable == tool.isUsable && Objects.equals(name, tool.name)
            && Objects.equals(category, tool.category) && Objects.equals(description, tool.description)
            && Objects.equals(homeGarage, tool.homeGarage) && Objects.deepEquals(image, tool.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, description, remainingValue, homeGarage, isUsable, Arrays.hashCode(image));
    }

    @Override
    public String toString() {
        return "Tool{"
            + "name='" + name + '\''
            + ", category=" + category
            + ", description='" + description + '\''
            + ", remainingValue=" + remainingValue
            + ", homeGarage=" + homeGarage
            + ", isUsable=" + isUsable
            + ", image=" + Arrays.toString(image)
            + '}';
    }

    public static class ToolBuilder {
        private String name;
        private String description;
        private double remainingValue;
        private ToolCategory category;
        private Garage homeGarage;
        private Coordinate coordinate;
        private boolean isUsable;
        private byte[] image;

        public ToolBuilder() {
        }

        public static ToolBuilder builder() {
            return new ToolBuilder();
        }

        public ToolBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ToolBuilder withCategory(ToolCategory category) {
            this.category = category;
            return this;
        }

        public ToolBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ToolBuilder withRemainingValue(double remainingValue) {
            this.remainingValue = remainingValue;
            return this;
        }

        public ToolBuilder withHomeGarage(Garage homeGarage) {
            this.homeGarage = homeGarage;
            return this;
        }

        public ToolBuilder withCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        public ToolBuilder withIsUsable(boolean isUsable) {
            this.isUsable = isUsable;
            return this;
        }

        public ToolBuilder withImage(byte[] image) {
            this.image = image;
            return this;
        }

        public Tool build() {
            return new Tool(name, category, description, remainingValue, homeGarage, coordinate, isUsable, image);
        }
    }
}
