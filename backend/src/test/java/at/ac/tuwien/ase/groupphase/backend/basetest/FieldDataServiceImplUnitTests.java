package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.FieldDataServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldDataServiceImplUnitTests {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private FieldDataServiceImpl service;

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
    void getFieldDataByIdreturnsCorrectCalculatedDto() {
        long fieldId = 1L;
        int currentYear = LocalDate.now().getYear();

        Field field = new Field();
        field.setId(fieldId);

        CropType wheat = new CropType();
        wheat.setName("Wheat");

        CropType corn = new CropType();
        corn.setName("Corn");

        Crop crop1 = new Crop();
        crop1.setOwnerId(defaultFarmer.getId());
        crop1.setOwner(defaultFarmer);
        crop1.setType(wheat);
        crop1.setCurrentPrice(10.0);
        crop1.setQuantity(5);
        crop1.setHarvestDate(LocalDateTime.of(currentYear, 6, 1, 0, 0));

        Crop crop2 = new Crop();
        crop1.setOwnerId(defaultFarmer.getId());
        crop1.setOwner(defaultFarmer);
        crop2.setType(wheat);
        crop2.setCurrentPrice(20.0);
        crop2.setQuantity(2);
        crop2.setHarvestDate(LocalDateTime.of(currentYear - 1, 5, 1,0,0));

        Crop crop3 = new Crop();
        crop1.setOwnerId(defaultFarmer.getId());
        crop1.setOwner(defaultFarmer);
        crop3.setType(corn);
        crop3.setCurrentPrice(5.0);
        crop3.setQuantity(10);
        crop3.setHarvestDate(LocalDateTime.of(currentYear, 7, 1,0,0));

        List<Crop> crops = List.of(crop1, crop2, crop3);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, defaultFarmer)).thenReturn(crops);

        FieldDataDto result = service.getFieldDataById(fieldId);

        assertNotNull(result);

        Map<String, Integer> cropNumbers = result.getPlantedCrops();
        assertEquals(2, cropNumbers.get("Wheat"));
        assertEquals(1, cropNumbers.get("Corn"));

        assertEquals(140.0, result.getTotalEarnings());

        assertEquals(150.0, result.getDiffPrevYear());

        Map<Integer, Double> timeseries = result.getHarvestedCropsTimeSeries();
        assertEquals(100.0, timeseries.get(currentYear));
        assertEquals(40.0, timeseries.get(currentYear - 1));

        verify(fieldRepository).findById(fieldId);
        verify(cropRepository).findByFieldAndOwner(field, defaultFarmer);
    }

    @Test
    void getFieldDataById_withNonExistingField_throwsNotFoundException() {
        long fieldId = 99L;

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> service.getFieldDataById(fieldId)
        );

        verify(cropRepository, never()).findByFieldAndOwner(any(), any());
    }

    @Test
    void getFieldDataById_countsCropTypesCorrectly() {
        long fieldId = 1L;

        Farmer farmer = defaultFarmer;

        Field field = new Field();
        field.setId(fieldId);

        CropType wheat = new CropType();
        wheat.setName("Wheat");

        Crop crop1 = new Crop();
        crop1.setType(wheat);
        crop1.setCurrentPrice(1);
        crop1.setQuantity(1);
        crop1.setHarvestDate(LocalDateTime.now());

        Crop crop2 = new Crop();
        crop2.setType(wheat);
        crop2.setCurrentPrice(1);
        crop2.setQuantity(1);
        crop2.setHarvestDate(LocalDateTime.now());


        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, farmer))
            .thenReturn(List.of(crop1, crop2));

        FieldDataDto result = service.getFieldDataById(fieldId);

        assertEquals(2, result.getPlantedCrops().get("Wheat"));
    }

    @Test
    void getFieldDataById_calculatesTotalEarningsCorrectly() {
        long fieldId = 1L;

        Farmer farmer = defaultFarmer;

        Field field = new Field();
        field.setId(fieldId);

        CropType type = new CropType();
        type.setName("Corn");

        Crop crop = new Crop();
        crop.setType(type);
        crop.setCurrentPrice(10);
        crop.setQuantity(3);
        crop.setHarvestDate(LocalDateTime.now());

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, farmer))
            .thenReturn(List.of(crop));

        FieldDataDto result = service.getFieldDataById(fieldId);

        assertEquals(30.0, result.getTotalEarnings());
    }

    @Test
    void getFieldDataById_calculatesPreviousYearDifferenceCorrectly() {
        long fieldId = 1L;
        int year = LocalDate.now().getYear();

        Farmer farmer = defaultFarmer;

        Field field = new Field();
        field.setId(fieldId);

        CropType type = new CropType();
        type.setName("Wheat");

        Crop lastYearCrop = new Crop();
        lastYearCrop.setType(type);
        lastYearCrop.setCurrentPrice(20);
        lastYearCrop.setQuantity(1);
        lastYearCrop.setHarvestDate(LocalDateTime.of(year - 1, 1, 1, 0, 0));

        Crop thisYearCrop = new Crop();
        thisYearCrop.setType(type);
        thisYearCrop.setCurrentPrice(10);
        thisYearCrop.setQuantity(1);
        thisYearCrop.setHarvestDate(LocalDateTime.of(year, 1, 1, 0, 0));

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, farmer))
            .thenReturn(List.of(lastYearCrop, thisYearCrop));

        FieldDataDto result = service.getFieldDataById(fieldId);

        assertEquals(-50.0, result.getDiffPrevYear());
    }

    @Test
    void getFieldDataById_calculatesTimeseriesCorrectly() {
        long fieldId = 1L;
        int year = LocalDate.now().getYear();

        Farmer farmer = defaultFarmer;

        Field field = new Field();
        field.setId(fieldId);

        CropType type = new CropType();
        type.setName("Corn");

        Crop crop1 = new Crop();
        crop1.setType(type);
        crop1.setCurrentPrice(5);
        crop1.setQuantity(2);
        crop1.setHarvestDate(LocalDateTime.of(year, 1, 1, 0, 0));

        Crop crop2 = new Crop();
        crop2.setType(type);
        crop2.setCurrentPrice(3);
        crop2.setQuantity(4);
        crop2.setHarvestDate(LocalDateTime.of(year, 6, 1, 0, 0));


        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, farmer))
            .thenReturn(List.of(crop1, crop2));

        FieldDataDto result = service.getFieldDataById(fieldId);

        assertEquals(22.0, result.getHarvestedCropsTimeSeries().get(year));
    }

    @Test
    void getForecastByFieldId_createsCorrectForecast() {
        long fieldId = 1L;
        int startYear = 2020;

        Farmer farmer = defaultFarmer;

        Field field = new Field();
        field.setId(fieldId);

        CropType type = new CropType();
        type.setName("Wheat");

        Crop crop1 = new Crop();
        crop1.setType(type);
        crop1.setCurrentPrice(10);
        crop1.setQuantity(5);
        crop1.setHarvestDate(LocalDateTime.of(startYear, 1, 1, 0, 0));

        Crop crop2 = new Crop();
        crop2.setType(type);
        crop2.setCurrentPrice(12);
        crop2.setQuantity(6);
        crop2.setHarvestDate(LocalDateTime.of(startYear + 1, 1, 1, 0, 0));

        Crop crop3 = new Crop();
        crop3.setType(type);
        crop3.setCurrentPrice(11);
        crop3.setQuantity(7);
        crop3.setHarvestDate(LocalDateTime.of(startYear + 2, 1, 1, 0, 0));

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(cropRepository.findByFieldAndOwner(field, farmer))
            .thenReturn(List.of(crop1, crop2, crop3));


        ForecastDto forecastDto = service.getForecastByFieldId(fieldId);

        assertNotNull(forecastDto);

        assertNotNull(forecastDto.getForecastTimeSeries());
        assertNotNull(forecastDto.getLowerConfidenceIntervalTimeSeries());
        assertNotNull(forecastDto.getUpperConfidenceIntervalTimeSeries());

        int forecastLength = forecastDto.getForecastTimeSeries().size();

        assertEquals(forecastLength, forecastDto.getLowerConfidenceIntervalTimeSeries().size());
        assertEquals(forecastLength, forecastDto.getUpperConfidenceIntervalTimeSeries().size());

        assertTrue(forecastLength > 0);

        forecastDto.getForecastTimeSeries().forEach((year, value) -> {
            assertNotNull(year);
            assertNotNull(value);
        });

        verify(fieldRepository).findById(fieldId);
        verify(cropRepository).findByFieldAndOwner(field, farmer);
    }

}
