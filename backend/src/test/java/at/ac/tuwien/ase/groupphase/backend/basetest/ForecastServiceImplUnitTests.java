package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.FieldServiceImpl;
import at.ac.tuwien.ase.groupphase.backend.service.impl.ForecastServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ForecastServiceImplUnitTests {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private CropRepository cropRepository;

    @InjectMocks
    private FieldServiceImpl fieldService;

    @InjectMocks
    private ForecastServiceImpl forecastService;

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
    void calculateCropYieldsTimeseries_CorrectKeysAndSize() {
        List<Crop> crops = List.of(
            createCrop(2020, 10.0, 5),
            createCrop(2020, 5.0, 2),
            createCrop(2021, 20.0, 3)
        );

        Map<Integer, Double> result = ForecastServiceImpl.calculateCropYieldsTimeseries(crops);

        assertEquals(2, result.size(), "Timeseries should contain exactly 2 years");
        assertTrue(result.containsKey(2020), "Timeseries should contain key 2020");
        assertTrue(result.containsKey(2021), "Timeseries should contain key 2021");
        assertNotNull(result.get(2020), "Timeseries for 2020 should not be null");
    }

    @Test
    void calculateForecast_CorrectLengthAndKeys() {
        int startYear = 2020;
        int forecastLength = 5;
        Long fieldId = 123L;

        List<Crop> crops = List.of(
            createCrop(startYear, 100.0, 1),
            createCrop(startYear + 1, 110.0, 1),
            createCrop(startYear + 2, 120.0, 1)
        );

        ForecastDto result = ForecastServiceImpl.calculateForecast(crops, forecastLength, fieldId);

        assertNotNull(result);
        assertEquals(fieldId, result.getId());

        assertEquals(forecastLength, result.getForecastTimeSeries().size(), "Forecast length does not match");
        assertEquals(forecastLength, result.getLowerConfidenceIntervalTimeSeries().size(), "Lower interval length does not match");
        assertEquals(forecastLength, result.getUpperConfidenceIntervalTimeSeries().size(), "Upper interval length does not match");

        int expectedStartYear = startYear + 3;
        for (int i = 0; i < forecastLength; i++) {
            int targetYear = expectedStartYear + i;

            assertTrue(result.getForecastTimeSeries().containsKey(targetYear),
                "Missing year " + targetYear + " in forecast");
            assertNotNull(result.getForecastTimeSeries().get(targetYear),
                "Value for year " + targetYear + " should not be null");
            assertTrue(result.getLowerConfidenceIntervalTimeSeries().containsKey(targetYear),
                "Missing year " + targetYear + " in lower interval");
            assertTrue(result.getUpperConfidenceIntervalTimeSeries().containsKey(targetYear),
                "Missing year " + targetYear + " in upper interval");
        }
    }

    @Test
    void calculateForecast_allValuesPresent() {
        List<Crop> crops = List.of(
            createCrop(2018, 50, 1),
            createCrop(2019, 55, 1),
            createCrop(2020, 60, 1)
        );

        ForecastDto result = ForecastServiceImpl.calculateForecast(crops, 3, 1L);

        result.getForecastTimeSeries().forEach((year, val) -> assertNotNull(val));
        result.getLowerConfidenceIntervalTimeSeries().forEach((year, val) -> assertNotNull(val));
        result.getUpperConfidenceIntervalTimeSeries().forEach((year, val) -> assertNotNull(val));
    }

    private Crop createCrop(int year, double price, double quantity) {
        Crop crop = new Crop();
        crop.setHarvestDate(LocalDateTime.of(year, 1, 1, 0, 0));
        crop.setCurrentPrice(price);
        crop.setQuantity(quantity);
        return crop;
    }

    @Test
    void calculateNormalizedForecast_withMultipleFields() {
        Field f1 = fieldWithId(1L, defaultFarmer);
        Field f2 = fieldWithId(2L, defaultFarmer);
        Field f3 = fieldWithId(3L, defaultFarmer);

        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(List.of(f1, f2, f3));

        when(cropRepository.findByFieldAndOwner(eq(f1), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2022)));
        when(cropRepository.findByFieldAndOwner(eq(f2), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2023)));
        when(cropRepository.findByFieldAndOwner(eq(f3), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2024)));

        try (MockedStatic<ForecastServiceImpl> mocked = mockStatic(ForecastServiceImpl.class)) {

            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(1L)))
                .thenReturn(forecastWithSingleValue(10.0));
            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(2L)))
                .thenReturn(forecastWithSingleValue(20.0));
            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(3L)))
                .thenReturn(forecastWithSingleValue(30.0));

            double normalized = forecastService.calculateNormalizedForecast(2L);

            assertEquals(0.5, normalized);
        }
    }

    @Test
    void calculateNormalizedForecast_withNoCropData() {
        Field f1 = fieldWithId(1L, defaultFarmer);
        Field f2 = fieldWithId(2L, defaultFarmer);

        when(fieldRepository.findAllByOwner(eq(defaultFarmer))).thenReturn(List.of(f1, f2));
        when(cropRepository.findByFieldAndOwner(eq(f1), eq(defaultFarmer))).thenReturn(List.of());
        when(cropRepository.findByFieldAndOwner(eq(f2), eq(defaultFarmer))).thenReturn(List.of());

        double normalized = forecastService.calculateNormalizedForecast(1L);

        assertEquals(-1.0, normalized);
    }

    @Test
    void calculateNormalizedForecast_targetHighestField() {
        Field f1 = fieldWithId(10L, defaultFarmer);
        Field f2 = fieldWithId(11L, defaultFarmer);
        Field f3 = fieldWithId(12L, defaultFarmer);
        Field target = fieldWithId(99L, defaultFarmer);

        when(fieldRepository.findAllByOwner(eq(defaultFarmer))).thenReturn(List.of(f1, f2, f3, target));

        when(cropRepository.findByFieldAndOwner(eq(f1), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2020)));
        when(cropRepository.findByFieldAndOwner(eq(f2), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2020)));
        when(cropRepository.findByFieldAndOwner(eq(f3), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2020)));
        when(cropRepository.findByFieldAndOwner(eq(target), eq(defaultFarmer))).thenReturn(List.of(cropInPastYear(2020)));

        try (MockedStatic<ForecastServiceImpl> mocked = mockStatic(ForecastServiceImpl.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(10L)))
                .thenReturn(forecastWithSingleValue(5.0));

            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(11L)))
                .thenReturn(forecastWithSingleValue(15.0));

            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(12L)))
                .thenReturn(forecastWithSingleValue(25.0));

            mocked.when(() -> ForecastServiceImpl.calculateForecast(anyList(), eq(1), eq(99L)))
                .thenReturn(forecastWithSingleValue(25.0));

            double normalized = forecastService.calculateNormalizedForecast(99L);

            assertEquals(1.0, normalized);
        }
    }

    private static Field fieldWithId(Long id, Farmer owner) {
        Field f = new Field();
        f.setOwner(owner);
        f.setId(id);
        return f;
    }

    private static Crop cropInPastYear(int year) {
        Crop c = new Crop();
        c.setHarvestDate(LocalDateTime.of(year, 1, 1, 0, 0));
        c.setCurrentPrice(10.0);
        c.setQuantity(1.0);
        return c;
    }

    private static ForecastDto forecastWithSingleValue(double value) {
        ForecastDto dto = new ForecastDto();
        Map<Integer, Double> ts = new LinkedHashMap<>();
        ts.put(2030, value);
        dto.setForecastTimeSeries(ts);
        return dto;
    }
}