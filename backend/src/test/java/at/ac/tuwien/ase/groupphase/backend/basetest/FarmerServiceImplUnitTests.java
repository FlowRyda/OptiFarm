package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.service.impl.FarmerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static at.ac.tuwien.ase.groupphase.backend.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.context.SecurityContextHolder.*;

@ExtendWith(MockitoExtension.class)
public class FarmerServiceImplUnitTests {
    @Mock
    private JwtTokenizer jwtTokenizerMock;
    @Mock
    private PasswordEncoder passwordEncoderMock;
    @Mock
    private FarmerRepository farmerRepositoryMock;
    @InjectMocks
    private FarmerServiceImpl farmerService;

    private Farmer defaultFarmer;
    private Farmer editedFarmer;

    @BeforeEach
    void setup() {
        defaultFarmer = TestData.defaultFarmer();
        editedFarmer = TestData.editedFarmer();

        JwtUser jwtUser = new JwtUser(1L, "test@mail.com");
        Authentication auth =
            new UsernamePasswordAuthenticationToken(jwtUser, null, null);

        SecurityContext context = createEmptyContext();
        context.setAuthentication(auth);
        setContext(context);
    }

    @Test
    public void changePassword_when_input_is_valid_changes_password_successfully() {
        when(farmerRepositoryMock.findById(ID)).thenReturn(Optional.of(defaultFarmer));
        when(passwordEncoderMock.matches(any(), any())).thenReturn(true);
        when(passwordEncoderMock.encode(any())).thenReturn(HASHED_PW);

        defaultFarmer.setHashedPassword(HASHED_PW);
        when(farmerRepositoryMock.save(any())).thenReturn(defaultFarmer);

        var changedFarmer = farmerService.changePassword(CHANGE_PASSWORD_DTO);
        assertNotNull(changedFarmer);
    }

    @Test
    public void changePassword_when_input_is_not_valid_throws_BadCredentialsException() {
        when(farmerRepositoryMock.findById(ID)).thenReturn(Optional.of(defaultFarmer));
        when(passwordEncoderMock.matches(any(), any())).thenReturn(false);

        var exception = assertThrows(BadCredentialsException.class, () -> farmerService.changePassword(CHANGE_PASSWORD_DTO));
        assertEquals("Old password is incorrect.", exception.getMessage());
    }

    @Test
    public void delete_does_not_throw_when_input_is_valid() {
        when(farmerRepositoryMock.findById(ID)).thenReturn(Optional.of(defaultFarmer));
        assertDoesNotThrow(() -> {
            farmerService.delete();
        });
    }

    @Test
    public void delete_throws_when_farmer_cannot_be_found() {
        var exception = assertThrows(NotFoundException.class, () -> farmerService.delete());
        assertEquals(String.format("Could not find a farmer with the id %s", ID), exception.getMessage());
    }

    @Test
    public void create_farmer_creates_new_farmer() {
        defaultFarmer.setId(ID);
        defaultFarmer.setHashedPassword(HASHED_PW);
        when(farmerRepositoryMock.save(any())).thenReturn(defaultFarmer);
        when(passwordEncoderMock.encode(any())).thenReturn(HASHED_PW);
        var created = farmerService.create(CREATE_FARMER_DTO);

        assertAll(() -> {
            assertNotNull(created);
            assertNotNull(created.getEmail());
            assertNotNull(created.getFirstname());
            assertNotNull(created.getLastname());
            assertEquals(CREATE_FARMER_DTO.getEmail(), created.getEmail());
            assertEquals(CREATE_FARMER_DTO.getFirstname(), created.getFirstname());
            assertEquals(CREATE_FARMER_DTO.getLastname(), created.getLastname());
        });
    }

    @Test
    public void update_farmer_updates_existing_farmer() {
        defaultFarmer.setId(ID);

        when(farmerRepositoryMock.save(any())).thenReturn(editedFarmer);
        when(farmerRepositoryMock.findById(ID)).thenReturn(Optional.of(defaultFarmer));

        var edited = farmerService.update(EDIT_FARMER_DTO);

        assertAll(() -> {
            assertNotNull(edited);
            assertNotNull(edited.getEmail());
            assertNotNull(edited.getFirstname());
            assertNotNull(edited.getLastname());
            assertEquals(EDIT_FARMER_DTO.getEmail(), edited.getEmail());
            assertEquals(EDIT_FARMER_DTO.getFirstname(), edited.getFirstname());
            assertEquals(EDIT_FARMER_DTO.getLastname(), edited.getLastname());
        });
    }

    @Test
    public void findApplicationUserByEmail_returns_existing_farmer() {
        when(farmerRepositoryMock.findByEmail(defaultFarmer.getEmail())).thenReturn(Optional.of(defaultFarmer));

        var farmer = farmerService.findApplicationUserByEmail(defaultFarmer.getEmail());
        Assertions.assertNotNull(farmer);
    }

    @Test
    public void findApplicationUserByEmail_throws_when_farmer_cannot_be_found() {
        when(farmerRepositoryMock.findByEmail(defaultFarmer.getEmail())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> farmerService.findApplicationUserByEmail(defaultFarmer.getEmail()));
    }

    @Test
    public void loadUserByUsername_returns_correct_UserDetails() {
        when(farmerRepositoryMock.findByEmail(defaultFarmer.getEmail())).thenReturn(Optional.of(defaultFarmer));

        var userDetails = farmerService.loadUserByUsername(defaultFarmer.getEmail());

        assertAll(() -> {
            assertNotNull(userDetails);
            assertNotNull(userDetails.getUsername());
            assertNotNull(userDetails.getPassword());
            assertNotNull(userDetails.getAuthorities());
            assertEquals(defaultFarmer.getEmail(), userDetails.getUsername());
            assertEquals(defaultFarmer.getHashedPassword(), userDetails.getPassword());
            assertEquals(1, userDetails.getAuthorities().size());
            assertTrue(userDetails.getAuthorities().containsAll(USER_ROLES));
        });
    }

    @Test
    public void loadUserByUsername_throws_when_farmer_cannot_be_found() {
        when(farmerRepositoryMock.findByEmail(defaultFarmer.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> farmerService.loadUserByUsername(defaultFarmer.getEmail()));
    }

    @Test
    public void valid_login_returns_jwt() {
        when(farmerRepositoryMock.findByEmail(LOGIN_DTO.getEmail())).thenReturn(Optional.of(defaultFarmer));
        when(passwordEncoderMock.matches(LOGIN_DTO.getPassword(), defaultFarmer.getHashedPassword())).thenReturn(true);
        String expectedJwt = "myJwt";
        when(jwtTokenizerMock.getAuthToken(any(), any(), any())).thenReturn(expectedJwt);

        var resultJwt = farmerService.login(LOGIN_DTO);
        assertEquals(expectedJwt, resultJwt);
    }

    @Test
    public void invalid_login_throws() {
        when(farmerRepositoryMock.findByEmail(LOGIN_DTO.getEmail())).thenReturn(Optional.of(defaultFarmer));
        when(passwordEncoderMock.matches(LOGIN_DTO.getPassword(), defaultFarmer.getHashedPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> farmerService.login(LOGIN_DTO));
    }
}
