package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;

import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.FieldDataService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static at.ac.tuwien.ase.groupphase.backend.service.impl.ForecastServiceImpl.calculateForecast;

@Service
public class FieldDataServiceImpl implements FieldDataService {

    private static final Logger logger = LoggerFactory.getLogger(FieldDataServiceImpl.class);
    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;
    private final FieldRepository fieldRepository;
    private final int forecastLength = 5;

    @Autowired
    public FieldDataServiceImpl(CropRepository cropRepository, FarmerRepository farmerRepository, FieldRepository fieldRepository) {
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
    public FieldDataDto getFieldDataById(Long id) {
        logger.info("Fetching field data by id {}", id);
        Farmer owner = getLoggedInFarmer();

        int currentYear = java.time.LocalDate.now().getYear();
        Field existingField = fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Field not found"));

        List<Crop> crops = cropRepository.findByFieldAndOwner(existingField, owner);

        Map<String, Integer> cropNumbers = crops.stream()
            .collect(Collectors.toMap(
                crop -> crop.getType().getName(),
                crop -> 1,
                Integer::sum
            ));

        double totalEarnings = crops.stream()
            .mapToDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            .sum();

        double totalQuantity = crops.stream()
            .mapToDouble(Crop::getQuantity)
            .sum();

        double earningsPrevYear = crops.stream()
            .filter(crop -> crop.getHarvestDate().getYear() == currentYear - 1)
            .mapToDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            .sum();

        double earningsThisYear = crops.stream()
            .filter(crop -> crop.getHarvestDate().getYear() == currentYear)
            .mapToDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            .sum();

        double diffEarningsPrevYear = earningsThisYear - earningsPrevYear;
        double diffEarningsPrevYearPercent = (earningsPrevYear == 0) ? 0 : (diffEarningsPrevYear / earningsPrevYear) * 100;


        Map<Integer, Double> cropYieldsTimeseries = calculateCropYieldsTimeseries(crops);

        FieldDataDto fieldDataDto = new FieldDataDto(id, existingField.getName(),
            cropNumbers, totalEarnings, diffEarningsPrevYearPercent, cropYieldsTimeseries);
        return fieldDataDto;
    }

    private Map<Integer, Double> calculateCropYieldsTimeseries(List<Crop> crops) {
        return crops.stream()
            .collect(Collectors.groupingBy(
                crop -> crop.getHarvestDate().getYear(),
                Collectors.summingDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            ));
    }

    public ForecastDto getForecastByFieldId(Long id) {
        logger.info("Fetching field forecast data by id {}", id);
        Farmer owner = getLoggedInFarmer();

        Field existingField = fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Field not found"));
        List<Crop> crops = cropRepository.findByFieldAndOwner(existingField, owner);

        return calculateForecast(crops, forecastLength, id);
    }

    public List<FieldDataDetailDto> getFieldDataByOwner(Long id) {
        Farmer owner = getLoggedInFarmer();
        logger.info("Fetching field data by owner id {}", owner.getId());

        if (!Objects.equals(id, owner.getId())) {
            throw new NotFoundException("Field data not found for owner id: " + id);
        }

        List<Field> fields = fieldRepository.findAllByOwner(owner);

        List<FieldDataDetailDto> fieldDataList = fields.stream().map(field -> {
            List<Crop> crops = cropRepository.findByFieldAndOwner(field, owner);
            double totalEarnings = crops.stream()
                .mapToDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
                .sum();
            double totalQuantity = crops.stream()
                .mapToDouble(Crop::getQuantity)
                .sum();

            return new FieldDataDetailDto(field.getId(), field.getName(), totalEarnings, totalQuantity, crops.size());
        }).collect(Collectors.toList());

        return fieldDataList;
    }
}
