package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class CreateFarmerDto {
    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull
    private String password;

    public CreateFarmerDto() {
    }

    public CreateFarmerDto(String firstname, String lastname, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public Farmer buildFarmer(String encodedPassword) {

        return new Farmer(email, encodedPassword, firstname, lastname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreateFarmerDto createFarmerDto)) {
            return false;
        }
        return Objects.equals(email, createFarmerDto.email)
            && Objects.equals(firstname, createFarmerDto.firstname)
            && Objects.equals(lastname, createFarmerDto.lastname)
            && Objects.equals(password, createFarmerDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, email, password);
    }

    @Override
    public String toString() {
        return "{"
            + "\"firstname\":\"" + firstname + "\","
            + "\"lastname\":\"" + lastname + "\","
            + "\"email\":\"" + email + "\","
            + "\"password\":\"" + password + "\""
            + "}";
    }

    public String redactedToString() {
        return "userSignupDto{"
            + "firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", email='" + email + '\''
            + ", password='#########'"
            + '}';
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
