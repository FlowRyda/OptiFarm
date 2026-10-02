package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Farmer extends ApplicationUser {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<Field> fields;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<ActiveField> activeFields;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<Garage> garages;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<Crop> crops;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<Tool> tools;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<Vehicle> vehicles;

    public Farmer() {
        fields = new ArrayList<>();
        activeFields = new ArrayList<>();
        garages = new ArrayList<>();
        crops = new ArrayList<>();
        tools = new ArrayList<>();
        vehicles = new ArrayList<>();
    }

    public Farmer(String email, String hashedPassword, String firstname, String lastname) {
        super(email, hashedPassword, firstname, lastname);
        fields = new ArrayList<>();
        activeFields = new ArrayList<>();
        garages = new ArrayList<>();
        crops = new ArrayList<>();
        tools = new ArrayList<>();
        vehicles = new ArrayList<>();
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<ActiveField> getActiveFields() {
        return activeFields;
    }

    public void setActiveFields(List<ActiveField> activeFields) {
        this.activeFields = activeFields;
    }

    public List<Garage> getGarages() {
        return garages;
    }

    public void setGarages(List<Garage> garages) {
        this.garages = garages;
    }

    public List<Crop> getCrops() {
        return crops;
    }

    public void setCrops(List<Crop> crops) {
        this.crops = crops;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public static final class FarmerBuilder {
        private Long id;


        private FarmerBuilder() {
        }

        public static FarmerBuilder aFarmer() {
            return new FarmerBuilder();
        }

        private String firstname;
        private String lastname;
        private String email;
        private String hashedPassword;

        public FarmerBuilder withFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public FarmerBuilder withLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public FarmerBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public FarmerBuilder withHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
            return this;
        }

        public Farmer build() {
            return new Farmer(this.email, this.hashedPassword, this.firstname, this.lastname);
        }
    }
}
