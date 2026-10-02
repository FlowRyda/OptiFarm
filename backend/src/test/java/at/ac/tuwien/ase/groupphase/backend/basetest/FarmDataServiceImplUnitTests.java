package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FarmDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.FarmDataServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmDataServiceImplUnitTests {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private FarmDataServiceImpl service;

    private Farmer defaultFarmer;
    private MockedStatic<JwtUtil> jwtUtilMock;

    @BeforeEach
    void setup() {
        defaultFarmer = TestData.defaultFarmer();
        JwtUser jwtUser = TestData.JWT_USER;

        jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(JwtUtil::getActiveJwtUser).thenReturn(jwtUser);

        lenient().when(farmerRepository.findById(defaultFarmer.getId()))
            .thenReturn(Optional.of(defaultFarmer));
    }

    @AfterEach
    void cleanup() {
        jwtUtilMock.close();
    }

    @Test
    void getFarmDataByOwner_returnsCorrectCalculation() {
        long farmerId = defaultFarmer.getId();
        int currentYear = LocalDateTime.now().getYear();

        Field field1 = new Field();
        field1.setId(1L);
        field1.setOwner(defaultFarmer);

        CropType wheat = new CropType();
        wheat.setName("Wheat");

        Crop crop1 = new Crop();
        crop1.setType(wheat);
        crop1.setCurrentPrice(10.0);
        crop1.setQuantity(5);
        crop1.setHarvestDate(LocalDateTime.of(currentYear, 6, 1, 0, 0));
        crop1.setFieldId(field1);

        Crop crop2 = new Crop();
        crop2.setType(wheat);
        crop2.setCurrentPrice(20.0);
        crop2.setQuantity(2);
        crop2.setHarvestDate(LocalDateTime.of(currentYear - 1, 5, 1, 0, 0));
        crop2.setFieldId(field1);

        List<Crop> crops = List.of(crop1, crop2);
        List<Field> fields = List.of(field1);

        when(cropRepository.findAllByOwner(eq(defaultFarmer), any(Pageable.class)))
            .thenReturn(new PageImpl<>(crops));
        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(fields);
        when(cropRepository.findByFieldAndOwner(field1, defaultFarmer)).thenReturn(crops);

        FarmDataDto result = service.getFarmDataByOwner(farmerId);

        assertNotNull(result);
        assertEquals(farmerId, result.getFarmerId());

        assertEquals(2, result.getPlantedCrops().get("Wheat"));

        assertEquals(90.0, result.getTotalEarnings());

        assertEquals(7.0, result.getTotalQuantity());

        assertEquals(25.0, result.getDiffPrevYear());

        assertFalse(result.getFieldTimeSeries().isEmpty());
        assertEquals(50.0, result.getFieldTimeSeries().getLast().get(currentYear));
        assertEquals(40.0, result.getFieldTimeSeries().getLast().get(currentYear - 1));

        verify(cropRepository).findAllByOwner(eq(defaultFarmer), any(Pageable.class));
    }

    @Test
    void getFarmDataByOwner_whenFarmerNotFoundInDb_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> service.getFarmDataByOwner(999L));
    }

    @Test
    void getFarmForecast_returnsForecastDto() {
        long farmerId = defaultFarmer.getId();

        Crop crop = new Crop();
        crop.setType(new CropType());
        crop.getType().setName("Wheat");
        crop.setCurrentPrice(10.0);
        crop.setQuantity(5);
        crop.setHarvestDate(LocalDateTime.of(2020, 1, 1, 0, 0));

        when(cropRepository.findAllByOwner(eq(defaultFarmer), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(crop)));

        ForecastDto result = service.getFarmForecast(farmerId);

        assertNotNull(result);
        assertNotNull(result.getForecastTimeSeries());
        assertFalse(result.getForecastTimeSeries().isEmpty());

        verify(cropRepository).findAllByOwner(eq(defaultFarmer), any(Pageable.class));
    }

    @Test
    void getFarmForecast_withWrongIdthrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> service.getFarmForecast(999L));
    }

    @Test
    void getFarmDataByOwner_calculatesZeroPercentPrevYear() {
        long farmerId = defaultFarmer.getId();
        int currentYear = LocalDateTime.now().getYear();

        Crop crop = new Crop();
        crop.setType(new CropType());
        crop.getType().setName("Corn");
        crop.setCurrentPrice(100.0);
        crop.setQuantity(1);
        crop.setHarvestDate(LocalDateTime.of(currentYear, 1, 1, 0, 0));

        when(cropRepository.findAllByOwner(eq(defaultFarmer), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(crop)));
        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(List.of());

        FarmDataDto result = service.getFarmDataByOwner(farmerId);

        assertEquals(0.0, result.getDiffPrevYear());
    }

    @Test
    void getFarmDataByOwner_withMultipleFieldsCalculatesCorrectData() {
        long farmerId = defaultFarmer.getId();
        int currentYear = LocalDateTime.now().getYear();

        Field field1 = new Field();
        field1.setId(1L);
        field1.setOwner(defaultFarmer);
        Field field2 = new Field();
        field2.setId(2L);
        field2.setOwner(defaultFarmer);
        Field field3 = new Field();
        field3.setId(3L);
        field3.setOwner(defaultFarmer);
        List<Field> fields = List.of(field1, field2, field3);

        CropType wheat = new CropType();
        wheat.setName("Wheat");
        CropType corn = new CropType();
        corn.setName("Corn");

        java.util.ArrayList<Crop> allCrops = new java.util.ArrayList<>();
        java.util.List<Crop> cropsF1 = new java.util.ArrayList<>();
        java.util.List<Crop> cropsF2 = new java.util.ArrayList<>();
        java.util.List<Crop> cropsF3 = new java.util.ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int year = currentYear - i;
            double quantity = 5.0 + i;
            double price = 10.0;

            Crop c1 = new Crop();
            c1.setType(wheat); c1.setCurrentPrice(price); c1.setQuantity(quantity);
            c1.setHarvestDate(LocalDateTime.of(year, 6, 1, 0, 0));
            c1.setFieldId(field1);
            cropsF1.add(c1); allCrops.add(c1);

            Crop c2 = new Crop();
            c2.setType(corn); c2.setCurrentPrice(price); c2.setQuantity(quantity);
            c2.setHarvestDate(LocalDateTime.of(year, 7, 1, 0, 0));
            c2.setFieldId(field2);
            cropsF2.add(c2); allCrops.add(c2);

            Crop c3 = new Crop();
            c3.setType(wheat); c3.setCurrentPrice(price); c3.setQuantity(quantity);
            c3.setHarvestDate(LocalDateTime.of(year, 8, 1, 0, 0));
            c3.setFieldId(field3);
            cropsF3.add(c3); allCrops.add(c3);
        }

        when(cropRepository.findAllByOwner(eq(defaultFarmer), any(Pageable.class)))
            .thenReturn(new PageImpl<>(allCrops));
        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(fields);
        when(cropRepository.findByFieldAndOwner(field1, defaultFarmer)).thenReturn(cropsF1);
        when(cropRepository.findByFieldAndOwner(field2, defaultFarmer)).thenReturn(cropsF2);
        when(cropRepository.findByFieldAndOwner(field3, defaultFarmer)).thenReturn(cropsF3);

        FarmDataDto result = service.getFarmDataByOwner(farmerId);

        assertNotNull(result);

        assertEquals(10, result.getPlantedCrops().get("Wheat"));
        assertEquals(5, result.getPlantedCrops().get("Corn"));

        assertEquals(1050.0, result.getTotalEarnings());
        assertEquals(105.0, result.getTotalQuantity());

        assertEquals(-16.666, result.getDiffPrevYear(), 0.001);

        assertEquals(3, result.getFieldTimeSeries().size());

        assertEquals(50.0, result.getFieldTimeSeries().getLast().get(currentYear));
        assertEquals(70.0, result.getFieldTimeSeries().getLast().get(currentYear - 2));

        verify(fieldRepository).findAllByOwner(defaultFarmer);
        verify(cropRepository, times(3)).findByFieldAndOwner(any(Field.class), eq(defaultFarmer));
    }
}