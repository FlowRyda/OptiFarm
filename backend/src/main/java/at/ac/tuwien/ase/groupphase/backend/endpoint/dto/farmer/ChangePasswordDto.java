package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer;

public class ChangePasswordDto {
    private String newPassword;
    private String oldPassword;

    public ChangePasswordDto() {
    }

    public ChangePasswordDto(String newPassword, String oldPassword) {
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
