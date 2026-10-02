package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.CropTypeMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.CropTypeServiceImpl;
import at.ac.tuwien.ase.groupphase.backend.service.validation.CropTypeValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CropTypeServiceImplUnitTests {

    @Mock
    private CropTypeRepository repo;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private CropTypeMapper mapper;

    @Mock
    private CropTypeValidator validator;

    @InjectMocks
    private CropTypeServiceImpl service;

    private Farmer defaultFarmer;
    private MockedStatic<JwtUtil> jwtUtilMock;

    @BeforeEach
    void setup() {
        defaultFarmer = TestData.defaultFarmer();
        JwtUser jwtUser = TestData.JWT_USER;

        jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(JwtUtil::getActiveJwtUser).thenReturn(jwtUser);

        when(farmerRepository.findById(defaultFarmer.getId()))
            .thenReturn(Optional.of(defaultFarmer));
    }

    @AfterEach
    void cleanup() {
        jwtUtilMock.close();
    }

    @Test
    void findAll_withExistingCropTypes_returnsMappedDtos() {
        CropType t1 = TestData.defaultCropType(1L, "Wheat", defaultFarmer);
        CropType t2 = TestData.defaultCropType(2L, "Corn", defaultFarmer);

        CropTypeDto dto1 = TestData.defaultCropTypeDto(1L, "Wheat");
        CropTypeDto dto2 = TestData.defaultCropTypeDto(2L, "Corn");

        when(repo.findAllByOwner(defaultFarmer)).thenReturn(List.of(t1, t2));
        when(mapper.toDto(t1)).thenReturn(dto1);
        when(mapper.toDto(t2)).thenReturn(dto2);

        List<CropTypeDto> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Wheat", result.get(0).getName());
        assertEquals("Corn", result.get(1).getName());

        verify(repo).findAllByOwner(defaultFarmer);
        verify(mapper).toDto(t1);
        verify(mapper).toDto(t2);
    }

    @Test
    void findById_withExistingId_returnsMappedDto() {
        CropType entity = TestData.defaultCropType(1L, "Wheat", defaultFarmer);
        CropTypeDto dto = TestData.defaultCropTypeDto(1L, "Wheat");

        when(repo.findByIdAndOwnerId(1L, defaultFarmer.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        CropTypeDto result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Wheat", result.getName());

        verify(repo).findByIdAndOwnerId(1L, defaultFarmer.getId());
        verify(mapper).toDto(entity);
    }

    @Test
    void findById_withNonExistingId_throwsNotFoundException() {
        when(repo.findByIdAndOwnerId(999L, defaultFarmer.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(999L));
        verify(mapper, never()).toDto(any());
    }

    @Test
    void create_withValidDto_savesAndReturnsMappedDto() throws ValidationException {
        CropTypeDto input = TestData.defaultCropTypeDto(null, "Barley");
        CropType saved = TestData.defaultCropType(10L, "Barley", defaultFarmer);
        CropTypeDto mappedResult = TestData.defaultCropTypeDto(10L, "Barley");

        when(repo.findAllByOwner(defaultFarmer)).thenReturn(List.of());
        doNothing().when(validator).validateForCreate(input, List.of());

        when(mapper.toEntity(input)).thenReturn(saved);
        when(repo.save(any(CropType.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mappedResult);

        CropTypeDto result = service.create(input);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Barley", result.getName());

        ArgumentCaptor<CropType> captor = ArgumentCaptor.forClass(CropType.class);
        verify(repo).save(captor.capture());
        assertNull(captor.getValue().getId());

        verify(mapper).toEntity(input);
        verify(mapper).toDto(saved);
        verify(validator).validateForCreate(input, List.of());
    }

    @Test
    void create_withInvalidDto_throwsValidationException() throws ValidationException {
        CropTypeDto input = TestData.defaultCropTypeDto(null, "Invalid");
        when(repo.findAllByOwner(defaultFarmer)).thenReturn(List.of());
        doThrow(new ValidationException("Validation Error", List.of("Error"))).when(validator).validateForCreate(input, List.of());

        assertThrows(ValidationException.class, () -> service.create(input));
        verify(repo, never()).save(any());
    }

    @Test
    void update_withValidId_updatesAndReturnsMappedDto() throws ValidationException {
        CropType existing = TestData.defaultCropType(5L, "Wheat", defaultFarmer);
        CropTypeDto updateDto = TestData.defaultCropTypeDto(null, "Updated Wheat");

        when(repo.findAllByOwner(defaultFarmer)).thenReturn(List.of(existing));
        doNothing().when(validator).validateForUpdate(updateDto, List.of(existing), 5L);

        when(repo.findByIdAndOwnerId(5L, defaultFarmer.getId())).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        CropTypeDto mapped = TestData.defaultCropTypeDto(5L, "Updated Wheat");
        when(mapper.toDto(existing)).thenReturn(mapped);

        CropTypeDto result = service.update(5L, updateDto);

        assertEquals("Updated Wheat", result.getName());
        verify(mapper).updateEntityFromDto(updateDto, existing);
        verify(repo).save(existing);
        verify(mapper).toDto(existing);
        verify(validator).validateForUpdate(updateDto, List.of(existing), 5L);
    }

    @Test
    void update_withNonExistingId_throwsNotFoundException() throws ValidationException {
        when(repo.findAllByOwner(defaultFarmer)).thenReturn(List.of());
        doNothing().when(validator).validateForUpdate(any(), anyList(), anyLong());

        when(repo.findByIdAndOwnerId(999L, defaultFarmer.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> service.update(999L, TestData.defaultCropTypeDto(null, "New"))
        );

        verify(repo, never()).save(any());
    }

    @Test
    void delete_withExistingId_deletesSuccessfully() {
        CropType existing = TestData.defaultCropType(3L, "Wheat", defaultFarmer);

        when(repo.findByIdAndOwnerId(3L, defaultFarmer.getId())).thenReturn(Optional.of(existing));

        service.delete(3L);

        verify(repo).deleteByIdAndOwnerId(3L, defaultFarmer.getId());
    }

    @Test
    void delete_withNonExistingId_throwsNotFoundException() {
        when(repo.findByIdAndOwnerId(123L, defaultFarmer.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> service.delete(123L)
        );

        verify(repo, never()).deleteByIdAndOwnerId(anyLong(), anyLong());
    }
}
