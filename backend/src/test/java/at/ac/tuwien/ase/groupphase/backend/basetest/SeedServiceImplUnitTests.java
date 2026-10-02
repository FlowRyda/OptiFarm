package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.SeedMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.SeedRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.SeedServiceImpl;
import at.ac.tuwien.ase.groupphase.backend.service.validation.SeedValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SeedServiceImplUnitTests {

    @Mock
    private SeedRepository seedRepository;

    @Mock
    private CropTypeRepository cropTypeRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private SeedValidator seedValidator;

    @InjectMocks
    private SeedServiceImpl service;

    private Farmer defaultFarmer;
    private MockedStatic<JwtUtil> jwtUtilMock;

    @BeforeEach
    void setup() {
        defaultFarmer = TestData.defaultFarmer();
        jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(JwtUtil::getActiveJwtUser).thenReturn(TestData.JWT_USER);

        when(farmerRepository.findById(defaultFarmer.getId()))
            .thenReturn(Optional.of(defaultFarmer));
    }

    @AfterEach
    void cleanup() {
        jwtUtilMock.close();
    }

    @Test
    void saveSeed_withValidSeed_savesAndReturnsEntity() throws ValidationException {
        Seed seed = TestData.defaultSeed(null, "Seed A", 1L, defaultFarmer);
        Seed saved = TestData.defaultSeed(10L, "Seed A", 1L, defaultFarmer);

        doNothing().when(seedValidator).validateForCreate(any(SeedDto.class), anyList());
        when(seedRepository.save(seed)).thenReturn(saved);

        Seed result = service.saveSeed(seed);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Seed A", result.getName());

        verify(seedValidator).validateForCreate(any(SeedDto.class), anyList());
        verify(seedRepository).save(seed);
    }

    @Test
    void getSeedById_withExistingId_returnsSeed() {
        Seed entity = TestData.defaultSeed(5L, "Seed B", 2L, defaultFarmer);
        when(seedRepository.findById(5L)).thenReturn(Optional.of(entity));

        Seed result = service.getSeedById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Seed B", result.getName());

        verify(seedRepository).findById(5L);
    }

    @Test
    void getSeedById_withNonExistingId_throwsRuntimeException() {
        when(seedRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getSeedById(999L));

        verify(seedRepository).findById(999L);
    }

    @Test
    void getAllSeeds_returnsMappedDtos() {
        Seed s1 = TestData.defaultSeed(1L, "Seed A", 10L, defaultFarmer);
        Seed s2 = TestData.defaultSeed(2L, "Seed B", 20L, defaultFarmer);

        SeedDto dto1 = TestData.defaultSeedDto(1L, "Seed A", 10L);
        SeedDto dto2 = TestData.defaultSeedDto(2L, "Seed B", 20L);

        when(seedRepository.findAllByOwner(defaultFarmer)).thenReturn(List.of(s1, s2));

        try (MockedStatic<SeedMapper> seedMapperMock = mockStatic(SeedMapper.class)) {
            seedMapperMock.when(() -> SeedMapper.toDto(s1)).thenReturn(dto1);
            seedMapperMock.when(() -> SeedMapper.toDto(s2)).thenReturn(dto2);

            List<SeedDto> result = service.getAllSeeds();

            assertEquals(2, result.size());
            assertEquals("Seed A", result.get(0).getName());
            assertEquals("Seed B", result.get(1).getName());

            verify(seedRepository).findAllByOwner(defaultFarmer);
            seedMapperMock.verify(() -> SeedMapper.toDto(s1));
            seedMapperMock.verify(() -> SeedMapper.toDto(s2));
        }
    }

    @Test
    void update_withValidData_updatesAndReturnsMappedDto() throws ValidationException {
        Seed existing = TestData.defaultSeed(5L, "Old Seed", 10L, defaultFarmer);
        SeedDto updateDto = TestData.defaultSeedDto(null, "Updated Seed", 20L);
        CropType updatedCropType = TestData.defaultCropType(20L, "Corn", defaultFarmer);

        when(seedRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(cropTypeRepository.findById(updateDto.getCropTypeId()))
            .thenReturn(Optional.of(updatedCropType));

        doNothing().when(seedValidator).validateForUpdate(any(SeedDto.class), anyList(), anyLong());

        Seed updatedSaved = TestData.defaultSeed(5L, "Updated Seed", 20L, defaultFarmer);
        when(seedRepository.save(existing)).thenReturn(updatedSaved);

        SeedDto mappedResult = TestData.defaultSeedDto(5L, "Updated Seed", 20L);

        try (MockedStatic<SeedMapper> seedMapperMock = mockStatic(SeedMapper.class)) {
            seedMapperMock.when(() -> SeedMapper.toDto(updatedSaved)).thenReturn(mappedResult);

            SeedDto result = service.update(5L, updateDto);

            assertNotNull(result);
            assertEquals(5L, result.getId());
            assertEquals("Updated Seed", result.getName());
            assertEquals(20L, result.getCropTypeId());

            verify(seedValidator).validateForUpdate(eq(updateDto), anyList(), eq(5L));
            verify(seedRepository).findById(5L);
            verify(cropTypeRepository).findById(updateDto.getCropTypeId());
            verify(seedRepository).save(existing);

            seedMapperMock.verify(() -> SeedMapper.updateEntityFromDto(updateDto, existing, updatedCropType));
            seedMapperMock.verify(() -> SeedMapper.toDto(updatedSaved));
        }
    }

    @Test
    void update_withNonExistingSeed_throwsRuntimeException() {
        SeedDto dto = TestData.defaultSeedDto(null, "X", 1L);
        when(seedRepository.findById(99L)).thenReturn(Optional.empty());

        try (MockedStatic<SeedMapper> seedMapperMock = mockStatic(SeedMapper.class)) {
            assertThrows(RuntimeException.class, () -> service.update(99L, dto));

            verify(seedRepository).findById(99L);
            verify(cropTypeRepository, never()).findById(any());
            seedMapperMock.verifyNoInteractions();
        }
    }

    @Test
    void update_withNonExistingCropType_throwsRuntimeException() throws ValidationException {
        Seed existing = TestData.defaultSeed(3L, "Seed X", 10L, defaultFarmer);
        SeedDto dto = TestData.defaultSeedDto(null, "Updated Seed", 999L);

        when(seedRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(cropTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update(3L, dto));

        verify(seedRepository).findById(3L);
        verify(cropTypeRepository).findById(999L);
        verify(seedValidator).validateForUpdate(eq(dto), anyList(), eq(3L));
    }

    @Test
    void getSeedsByCropType_returnsSeedList() {
        CropType c = TestData.defaultCropType(5L, "Wheat", defaultFarmer);

        when(cropTypeRepository.findById(5L)).thenReturn(Optional.of(c));

        Seed s1 = TestData.defaultSeed(1L, "Seed A", 5L, defaultFarmer);
        Seed s2 = TestData.defaultSeed(2L, "Seed B", 5L, defaultFarmer);

        when(seedRepository.findByTypeId(5L)).thenReturn(List.of(s1, s2));

        List<Seed> result = service.getSeedsByCropType(5L);

        assertEquals(2, result.size());
        assertEquals("Seed A", result.get(0).getName());

        verify(cropTypeRepository).findById(5L);
        verify(seedRepository).findByTypeId(5L);
    }
}
