package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class EditFarmerDto {
    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    public EditFarmerDto() {
    }

    public EditFarmerDto(Farmer farmer) {
        this.firstname = farmer.getFirstname();
        this.lastname = farmer.getLastname();
        this.email = farmer.getEmail();
    }

    public EditFarmerDto(String firstname, String lastname, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "{"
            + "\"firstname\":\"" + firstname + "\","
            + "\"lastname\":\"" + lastname + "\","
            + "\"email\":\"" + email + "\""
            + "}";
    }
}
