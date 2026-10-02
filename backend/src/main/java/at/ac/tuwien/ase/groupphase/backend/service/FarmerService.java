package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface FarmerService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;


    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return an application user
     */
    ApplicationUser findApplicationUserByEmail(String email) throws UsernameNotFoundException;

    /**
     * Log in a user.
     *
     * @param loginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(LoginDto loginDto) throws BadCredentialsException;

    /**
     * Get the data of the logged in farmer.
     *
     * @return the logged in farmer
     * @throws NotFoundException if the logged in farmer does not exist anymore
     */
    EditFarmerDto getLoggedInFarmer() throws NotFoundException;

    /**
     * creates a new Farmer.
     *
     * @param createFarmerDto the data for the new farmer
     * @return the created farmer dto - excluding the hashed pw
     */
    EditFarmerDto create(CreateFarmerDto createFarmerDto);

    /**
     * Updates the logged in existing farmer.
     *
     * @param createFarmerDto the data to update the farmer with
     * @return the update farmer
     * @throws NotFoundException if the logged in farmer does not exist anymore
     */
    EditFarmerDto update(EditFarmerDto createFarmerDto) throws NotFoundException;

    /**
     * Deletes the logged in farmer.
     *
     * @throws NotFoundException if the logged in farmer does not exist anymore
     */
    void delete() throws NotFoundException;

    /**
     * changes the password of the logged in farmer.
     *
     * @param changePasswordDto the new password and the old password to verify
     * @return the updated farmer without the hashed pw
     * @throws NotFoundException if the logged in farmer does not exist anymore
     */
    EditFarmerDto changePassword(ChangePasswordDto changePasswordDto) throws NotFoundException;
}
