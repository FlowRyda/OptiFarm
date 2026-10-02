package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.FarmerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class FarmerServiceImpl implements FarmerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public FarmerServiceImpl(FarmerRepository farmerRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.trace("Load user by username (email)");
        ApplicationUser applicationUser = findApplicationUserByEmail(email);
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_FARMER");
        LOGGER.info("Load user by username (email)");
        return new User(applicationUser.getEmail(), applicationUser.getHashedPassword(), grantedAuthorities);
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) throws UsernameNotFoundException {
        LOGGER.trace("Find application user by email");
        return farmerRepository.findByEmail(email).orElseThrow(() -> {
            LOGGER.error("No user found with email {}", email);
            return new UsernameNotFoundException("Could not find user by username");
        });
    }

    @Override
    public String login(LoginDto loginDto) throws BadCredentialsException {
        LOGGER.trace("Login farmer.");
        UserDetails userDetails = loadUserByUsername(loginDto.getEmail());
        if (userDetails != null && userDetails.isAccountNonExpired() && userDetails.isAccountNonLocked() && userDetails.isCredentialsNonExpired() && passwordEncoder.matches(loginDto.getPassword(), userDetails.getPassword())) {
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

            var farmer = farmerRepository.findByEmail(loginDto.getEmail()).orElseThrow(() -> {
                LOGGER.error("Could not find Farmer by email {}", loginDto.getEmail());
                return new UsernameNotFoundException("Could not find user by username");
            });
            return jwtTokenizer.getAuthToken(userDetails.getUsername(), farmer.getId(), roles);
        }
        LOGGER.error("Login failed");
        throw new BadCredentialsException("Username or password is incorrect or account is locked");
    }

    @Override
    public EditFarmerDto getLoggedInFarmer() throws NotFoundException {
        LOGGER.trace("Get logged in farmer");
        var activeUser = JwtUtil.getActiveJwtUser();
        LOGGER.info("Logged in farmer id {}", activeUser.id());
        var farmer = getFarmerById(activeUser.id());
        return new EditFarmerDto(farmer);
    }

    @Override
    public EditFarmerDto create(CreateFarmerDto createFarmerDto) {
        LOGGER.trace("Create new farmer");
        var encodedPassword = passwordEncoder.encode(createFarmerDto.getPassword());
        Farmer farmer = createFarmerDto.buildFarmer(encodedPassword);
        var saved = farmerRepository.save(farmer);
        LOGGER.info("Created Farmer with email {}", farmer.getEmail());
        return new EditFarmerDto(saved);
    }

    @Override
    public EditFarmerDto update(EditFarmerDto editFarmerDto) throws NotFoundException {
        LOGGER.trace("Update logged in farmer");
        var loggedInUser = JwtUtil.getActiveJwtUser();
        var farmer = getFarmerById(loggedInUser.id());

        farmer.setFirstname(editFarmerDto.getFirstname());
        farmer.setLastname(editFarmerDto.getLastname());
        farmer.setEmail(editFarmerDto.getEmail());
        farmerRepository.save(farmer);

        LOGGER.info("Updated Farmer with id {}", farmer.getId());
        return new EditFarmerDto(farmer);
    }

    @Override
    public void delete() throws NotFoundException {
        LOGGER.trace("Delete logged in farmer");
        var loggedInUser = JwtUtil.getActiveJwtUser();
        var farmer = getFarmerById(loggedInUser.id());
        farmerRepository.delete(farmer);
        LOGGER.info("Deleted Farmer with id {}", loggedInUser.id());
    }

    @Override
    public EditFarmerDto changePassword(ChangePasswordDto changePasswordDto) throws NotFoundException {
        LOGGER.trace("Change password of logged in farmer");
        var loggedInUser = JwtUtil.getActiveJwtUser();
        var farmer = getFarmerById(loggedInUser.id());
        var oldPasswordMatches = passwordEncoder.matches(changePasswordDto.getOldPassword(), farmer.getHashedPassword());

        if (oldPasswordMatches) {
            var newEncodedPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
            farmer.setHashedPassword(newEncodedPassword);
            farmerRepository.save(farmer);
            LOGGER.info("Changed password of logged in farmer");
            return new EditFarmerDto(farmer);
        }
        LOGGER.error("Change password of logged in farmer failed - old password does not match.");
        throw new BadCredentialsException("Old password is incorrect.");
    }

    private Farmer getFarmerById(Long id) throws NotFoundException {
        LOGGER.trace("Get logged in farmer by id {}", id);
        return farmerRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("Could not find farmer by id {}", id);
            return new NotFoundException(String.format("Could not find a farmer with the id %s", id));
        });
    }
}
