package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class LoginDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Password must not be null")
    private String password;

    public LoginDto() {
    }

    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoginDto loginDto)) {
            return false;
        }
        return Objects.equals(email, loginDto.email)
            && Objects.equals(password, loginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "{"
            + "\"email\":\"" + email + "\","
            + "\"password\":\"" + password + "\""
            + "}";
    }

    public static final class UserLoginDtoBuilder {
        private String email;
        private String password;

        private UserLoginDtoBuilder() {
        }

        public static UserLoginDtoBuilder anUserLoginDto() {
            return new UserLoginDtoBuilder();
        }

        public UserLoginDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserLoginDtoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginDto build() {
            LoginDto loginDto = new LoginDto();
            loginDto.setEmail(email);
            loginDto.setPassword(password);
            return loginDto;
        }
    }
}
