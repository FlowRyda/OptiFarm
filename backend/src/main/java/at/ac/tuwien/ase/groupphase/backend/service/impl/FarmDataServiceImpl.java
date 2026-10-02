package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FarmDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.FarmDataService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static at.ac.tuwien.ase.groupphase.backend.service.impl.ForecastServiceImpl.*;

@Service
public class FarmDataServiceImpl implements FarmDataService {

    private static final Logger logger = LoggerFactory.getLogger(FarmDataServiceImpl.class);
    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;
    private final FieldRepository fieldRepository;
    private final int forecastLength = 5;

    @Autowired
    public FarmDataServiceImpl(CropRepository cropRepository, FarmerRepository farmerRepository, FieldRepository fieldRepository) {
        this.cropRepository = cropRepository;
        this.farmerRepository = farmerRepository;
        this.fieldRepository = fieldRepository;
    }

    private Farmer getLoggedInFarmer() {
        var user = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(user.id()).orElseThrow(
            () -> new NotFoundException("Farmer not found: " + user.id())
        );
    }

    @Transactional
    @Override
    public FarmDataDto getFarmDataByOwner(Long id) {
        logger.info("Fetching farm data by id {}", id);
        Farmer owner = getLoggedInFarmer();

        int currentYear = java.time.LocalDate.now().getYear();
        if (!Objects.equals(id, owner.getId())) {
            throw new NotFoundException("Farm data not found for owner id: " + id);
        }

        Page pageContent = cropRepository.findAllByOwner(owner, Pageable.unpaged());
        List<Crop> allCrops = null;

        if (pageContent != null) {
            allCrops = pageContent.getContent();
        }

        Map<String, Integer> totalCropNumbers = allCrops.stream()
            .collect(Collectors.toMap(
                crop -> crop.getType().getName(),
                crop -> 1,
                Integer::sum
            ));

        double totalEarnings = allCrops.stream()
            .mapToDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            .sum();

        double totalQuantity = allCrops.stream()
            .mapToDouble(Crop::getQuantity)
            .sum();

        Map<Integer, Double> earningsByYear = allCrops.stream()
            .collect(Collectors.groupingBy(
                crop -> crop.getHarvestDate().getYear(),
                Collectors.summingDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            ));

        double earningsThisYear = earningsByYear.getOrDefault(currentYear, 0.0);
        double earningsPrevYear = earningsByYear.getOrDefault(currentYear - 1, 0.0);

        double diffEarningsPrevYear = earningsThisYear - earningsPrevYear;
        double diffEarningsPrevYearPercent = (earningsPrevYear == 0) ? 0 : (diffEarningsPrevYear / earningsPrevYear) * 100;

        Map<Integer, Double> totalCropYieldsTimeseries = calculateCropYieldsTimeseries(allCrops);

        List<Map<Integer, Double>> fieldTimeseries = fieldRepository.findAllByOwner(owner).stream()
            .map(field -> {
                List<Crop> fieldCrops = cropRepository.findByFieldAndOwner(field, owner);
                return calculateCropYieldsTimeseries(fieldCrops);
            })
            .collect(Collectors.toList());

        FarmDataDto farmDataDto = new FarmDataDto(id, totalCropNumbers, totalEarnings, totalQuantity,
            diffEarningsPrevYearPercent, totalCropYieldsTimeseries, fieldTimeseries);
        return farmDataDto;
    }

    @Override
    public ForecastDto getFarmForecast(Long id) {
        logger.info("Fetching farm forecast data by id {}", id);
        Farmer owner = getLoggedInFarmer();

        if (!Objects.equals(id, owner.getId())) {
            throw new NotFoundException("Farm data not found for owner id: " + id);
        }

        List<Crop> allCrops = cropRepository.findAllByOwner(owner, Pageable.unpaged()).getContent();

        return calculateForecast(allCrops, forecastLength, id);
    }
}
