package at.ac.tuwien.ase.groupphase.backend.datagenerator;

import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Profile("generateData")
@Component
public class FarmerDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int NUMBER_OF_FARMERS_TO_GENERATE = 5;

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final VehicleRepository vehicleRepository;
    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final GarageRepository garageRepository;
    private final FieldRepository fieldRepository;
    private final CropTypeRepository cropTypeRepository;
    private final CropRepository cropRepository;

    @Autowired
    public FarmerDataGenerator(FarmerRepository farmerRepository,
                               PasswordEncoder passwordEncoder,
                               VehicleRepository vehicleRepository,
                               ToolRepository toolRepository,
                               ToolCategoryRepository toolCategoryRepository,
                               GarageRepository garageRepository,
                               FieldRepository fieldRepository,
                               CropTypeRepository cropTypeRepository,
                               CropRepository cropRepository) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.vehicleRepository = vehicleRepository;
        this.toolRepository = toolRepository;
        this.toolCategoryRepository = toolCategoryRepository;
        this.garageRepository = garageRepository;
        this.fieldRepository = fieldRepository;
        this.cropTypeRepository = cropTypeRepository;
        this.cropRepository = cropRepository;
    }

    @PostConstruct
    private void generateData() {
        LOGGER.info("Starting idempotent data generation for profile 'generateData'.");

        try {
            generateFarmers();
            Farmer owner = getOwner();
            generateGarages(owner);
            generateFields(owner);
            generateCropTypes(owner);
            generateCrops(owner);
            generateTools(owner);
            generateVehicles(owner);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return;
        }

        LOGGER.info("Finished idempotent data generation.");
    }

    private void generateFarmers() {
        LOGGER.debug("Ensuring {} farmer entries exist", NUMBER_OF_FARMERS_TO_GENERATE);

        String hashedPw = passwordEncoder.encode("password");

        for (int i = 0; i < NUMBER_OF_FARMERS_TO_GENERATE; i++) {
            String email = i + "farmer@mail.com";
            String firstname = "Farmer_Firstname_" + i;
            String lastname = "Farmer_Lastname_" + i;

            farmerRepository.findByEmail(email).ifPresentOrElse(
                existing -> LOGGER.debug("Farmer with email {} already exists, skipping creation", email),
                () -> {
                    Farmer farmer = Farmer.FarmerBuilder.aFarmer()
                        .withFirstname(firstname)
                        .withLastname(lastname)
                        .withEmail(email)
                        .withHashedPassword(hashedPw)
                        .build();
                    LOGGER.debug("Creating farmer {}", farmer);
                    farmerRepository.save(farmer);
                }
            );
        }
    }

    private Farmer getOwner() {
        return farmerRepository.findByEmail("1farmer@mail.com")
            .orElseGet(() -> {
                LOGGER.debug("Owner farmer 1farmer@mail.com not found, creating it.");
                Farmer owner = new Farmer(
                    "1farmer@mail.com",
                    passwordEncoder.encode("password"),
                    "Farmer_Firstname_1",
                    "Farmer_Lastname_1"
                );
                return farmerRepository.save(owner);
            });
    }

    private ToolCategory createOrGetCategory(String name, Farmer owner) {
        return toolCategoryRepository.findByNameAndOwner(name, owner)
            .orElseGet(() -> {
                LOGGER.debug("Creating tool category {} for owner {}", name, owner.getEmail());
                return toolCategoryRepository.save(new ToolCategory(name, owner));
            });
    }

    private void generateGarages(Farmer owner) {
        LOGGER.debug("Ensuring garages exist for owner {}", owner.getEmail());

        createOrGetGarage("Garage 1", owner, new Coordinate(0, 0, 0));
        createOrGetGarage("Garage 2", owner, new Coordinate(20, 20, 100));
    }

    private Garage createOrGetGarage(String name, Farmer owner, Coordinate coordinate) {
        return garageRepository.findByName(name)
            .map(existing -> {
                LOGGER.debug("Garage {} already exists, skipping creation", name);
                return existing;
            })
            .orElseGet(() -> {
                LOGGER.debug("Creating garage {}", name);
                Garage garage = new Garage();
                garage.setOwner(owner);
                garage.setOwnerId(owner.getId());
                garage.setName(name);
                garage.setCoordinate(coordinate);
                return garageRepository.save(garage);
            });
    }

    private Garage getGarage(String name) {
        return garageRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name + " not found"));
    }

    private void generateFields(Farmer owner) {
        LOGGER.debug("Ensuring fields exist for owner {}", owner.getEmail());

        double baseLatField1 = 47.45606129486939;
        double baseLatField2 = 48.44406129486939;
        double baseLon = 15.832752345517912;

        double halfHeightLat = 0.0010;
        double halfWidthLon = 0.0015;

        double lonStep = 0.0040;

        createOrGetField("Field 1", owner, createRectField(baseLatField1, baseLon + lonStep,
            halfHeightLat, halfWidthLon));
        createOrGetField("Field 2", owner, createRectField(baseLatField2, baseLon + lonStep,
            halfHeightLat, halfWidthLon));
    }

    private Field createOrGetField(String name, Farmer owner, Polygon boundaries) {
        Field existing = fieldRepository.findByNameAndOwner(name, owner);
        if (existing != null) {
            LOGGER.debug("Field {} for owner {} already exists, skipping creation", name, owner.getEmail());
            return existing;
        }

        LOGGER.debug("Creating field {} for owner {}", name, owner.getEmail());
        Field field = new Field(owner, name, boundaries);
        return fieldRepository.save(field);
    }

    private void generateCropTypes(Farmer owner) {
        LOGGER.debug("Ensuring crop types exist for owner {}", owner.getEmail());

        createOrGetCropType("Wheat", 24.5, owner);
        createOrGetCropType("Fiber", 27.5, owner);
    }

    private CropType createOrGetCropType(String name, double moisture, Farmer owner) {
        return cropTypeRepository.findByNameAndOwner(name, owner)
            .orElseGet(() -> {
                LOGGER.debug("Creating crop type {} for owner {}", name, owner.getEmail());
                CropType cropType = new CropType(name, moisture, null, owner);
                cropType.setOwner(owner);
                cropType.setOwnerId(owner.getId());
                return cropTypeRepository.save(cropType);
            });
    }

    private void generateCrops(Farmer owner) {
        if (cropRepository.count() > 0) {
            LOGGER.debug("Crops already exist, skipping crop generation");
            return;
        }

        LOGGER.debug("Generating crops for owner {}", owner.getEmail());

        Field field1 = fieldRepository.findByNameAndOwner("Field 1", owner);

        CropType cropType1 = cropTypeRepository.getCropTypeByNameAndOwner("Wheat", owner);
        CropType cropType2 = cropTypeRepository.getCropTypeByNameAndOwner("Fiber", owner);

        List<Crop> cropsField1 = new ArrayList<>();

        cropsField1.add(new Crop(47, cropType1, 483,
            LocalDateTime.of(2025, 1, 9, 12, 0), owner, field1));
        cropsField1.add(new Crop(38, cropType2, 706,
            LocalDateTime.of(2024, 11, 5, 12, 0), owner, field1));
        cropsField1.add(new Crop(17, cropType1, 497,
            LocalDateTime.of(2024, 8, 31, 12, 0), owner, field1));
        cropsField1.add(new Crop(33, cropType2, 451,
            LocalDateTime.of(2024, 6, 26, 12, 0), owner, field1));
        cropsField1.add(new Crop(26, cropType1, 332,
            LocalDateTime.of(2024, 4, 22, 12, 0), owner, field1));
        cropsField1.add(new Crop(48, cropType2, 608,
            LocalDateTime.of(2024, 2, 17, 12, 0), owner, field1));
        cropsField1.add(new Crop(15, cropType1, 539,
            LocalDateTime.of(2023, 12, 14, 12, 0), owner, field1));
        cropsField1.add(new Crop(35, cropType2, 762,
            LocalDateTime.of(2023, 10, 10, 12, 0), owner, field1));
        cropsField1.add(new Crop(28, cropType1, 584,
            LocalDateTime.of(2023, 8, 6, 12, 0), owner, field1));
        cropsField1.add(new Crop(41, cropType2, 619,
            LocalDateTime.of(2023, 6, 2, 12, 0), owner, field1));
        cropsField1.add(new Crop(22, cropType1, 747,
            LocalDateTime.of(2023, 3, 29, 12, 0), owner, field1));
        cropsField1.add(new Crop(49, cropType2, 532,
            LocalDateTime.of(2023, 1, 23, 12, 0), owner, field1));
        cropsField1.add(new Crop(12, cropType1, 395,
            LocalDateTime.of(2022, 11, 19, 12, 0), owner, field1));
        cropsField1.add(new Crop(30, cropType2, 788,
            LocalDateTime.of(2022, 9, 15, 12, 0), owner, field1));
        cropsField1.add(new Crop(45, cropType1, 401,
            LocalDateTime.of(2022, 7, 11, 12, 0), owner, field1));
        cropsField1.add(new Crop(24, cropType2, 311,
            LocalDateTime.of(2022, 5, 7, 12, 0), owner, field1));
        cropsField1.add(new Crop(37, cropType1, 650,
            LocalDateTime.of(2022, 3, 3, 12, 0), owner, field1));
        cropsField1.add(new Crop(21, cropType2, 575,
            LocalDateTime.of(2021, 12, 28, 12, 0), owner, field1));
        cropsField1.add(new Crop(43, cropType1, 370,
            LocalDateTime.of(2021, 10, 24, 12, 0), owner, field1));
        cropsField1.add(new Crop(19, cropType2, 792,
            LocalDateTime.of(2021, 8, 20, 12, 0), owner, field1));
        cropsField1.add(new Crop(32, cropType1, 621,
            LocalDateTime.of(2021, 6, 16, 12, 0), owner, field1));
        cropsField1.add(new Crop(25, cropType2, 458,
            LocalDateTime.of(2021, 4, 12, 12, 0), owner, field1));
        cropsField1.add(new Crop(46, cropType1, 513,
            LocalDateTime.of(2021, 2, 6, 12, 0), owner, field1));
        cropsField1.add(new Crop(11, cropType2, 381,
            LocalDateTime.of(2020, 12, 3, 12, 0), owner, field1));
        cropsField1.add(new Crop(31, cropType1, 715,
            LocalDateTime.of(2020, 9, 29, 12, 0), owner, field1));
        cropsField1.add(new Crop(23, cropType2, 492,
            LocalDateTime.of(2020, 7, 26, 12, 0), owner, field1));
        cropsField1.add(new Crop(44, cropType1, 680,
            LocalDateTime.of(2020, 5, 21, 12, 0), owner, field1));
        cropsField1.add(new Crop(36, cropType2, 350,
            LocalDateTime.of(2020, 3, 17, 12, 0), owner, field1));
        cropsField1.add(new Crop(14, cropType1, 555,
            LocalDateTime.of(2020, 1, 12, 12, 0), owner, field1));
        cropsField1.add(new Crop(27, cropType2, 730,
            LocalDateTime.of(2019, 11, 8, 12, 0), owner, field1));
        cropsField1.add(new Crop(39, cropType1, 420,
            LocalDateTime.of(2019, 9, 4, 12, 0), owner, field1));
        cropsField1.add(new Crop(18, cropType2, 690,
            LocalDateTime.of(2019, 7, 1, 12, 0), owner, field1));
        cropsField1.add(new Crop(42, cropType1, 345,
            LocalDateTime.of(2019, 4, 27, 12, 0), owner, field1));
        cropsField1.add(new Crop(16, cropType2, 599,
            LocalDateTime.of(2019, 2, 21, 12, 0), owner, field1));
        cropsField1.add(new Crop(20, cropType1, 750,
            LocalDateTime.of(2018, 12, 18, 12, 0), owner, field1));
        cropsField1.add(new Crop(34, cropType2, 477,
            LocalDateTime.of(2018, 10, 14, 12, 0), owner, field1));
        cropsField1.add(new Crop(29, cropType1, 634,
            LocalDateTime.of(2018, 8, 10, 12, 0), owner, field1));
        cropsField1.add(new Crop(13, cropType2, 305,
            LocalDateTime.of(2018, 6, 6, 12, 0), owner, field1));
        cropsField1.add(new Crop(40, cropType1, 777,
            LocalDateTime.of(2018, 4, 2, 12, 0), owner, field1));
        cropsField1.add(new Crop(50, cropType2, 510,
            LocalDateTime.of(2018, 1, 27, 12, 0), owner, field1));
        cropsField1.add(new Crop(47, cropType1, 483,
            LocalDateTime.of(2017, 11, 23, 12, 0), owner, field1));
        cropsField1.add(new Crop(38, cropType2, 706,
            LocalDateTime.of(2017, 9, 19, 12, 0), owner, field1));
        cropsField1.add(new Crop(17, cropType1, 497,
            LocalDateTime.of(2017, 7, 15, 12, 0), owner, field1));
        cropsField1.add(new Crop(33, cropType2, 451,
            LocalDateTime.of(2017, 5, 11, 12, 0), owner, field1));
        cropsField1.add(new Crop(26, cropType1, 332,
            LocalDateTime.of(2017, 3, 6, 12, 0), owner, field1));
        cropsField1.add(new Crop(48, cropType2, 608,
            LocalDateTime.of(2016, 12, 31, 12, 0), owner, field1));
        cropsField1.add(new Crop(15, cropType1, 539,
            LocalDateTime.of(2016, 10, 27, 12, 0), owner, field1));
        cropsField1.add(new Crop(35, cropType2, 762,
            LocalDateTime.of(2016, 8, 22, 12, 0), owner, field1));
        cropsField1.add(new Crop(28, cropType1, 584,
            LocalDateTime.of(2016, 6, 18, 12, 0), owner, field1));
        cropsField1.add(new Crop(41, cropType2, 619,
            LocalDateTime.of(2016, 4, 14, 12, 0), owner, field1));

        cropRepository.saveAll(cropsField1);

        Field field2 = fieldRepository.findByNameAndOwner("Field 2", owner);
        List<Crop> cropsField2 = new ArrayList<>();

        cropsField2.add(new Crop(68, cropType2, 1145,
            LocalDateTime.of(2024, 12, 24, 10, 0), owner, field2));
        cropsField2.add(new Crop(81, cropType1, 952,
            LocalDateTime.of(2024, 10, 19, 10, 0), owner, field2));
        cropsField2.add(new Crop(59, cropType2, 870,
            LocalDateTime.of(2024, 8, 14, 10, 0), owner, field2));
        cropsField2.add(new Crop(92, cropType1, 1101,
            LocalDateTime.of(2024, 6, 9, 10, 0), owner, field2));
        cropsField2.add(new Crop(77, cropType2, 988,
            LocalDateTime.of(2024, 4, 4, 10, 0), owner, field2));
        cropsField2.add(new Crop(61, cropType1, 1056,
            LocalDateTime.of(2024, 1, 29, 10, 0), owner, field2));
        cropsField2.add(new Crop(88, cropType2, 1190,
            LocalDateTime.of(2023, 11, 24, 10, 0), owner, field2));
        cropsField2.add(new Crop(73, cropType1, 915,
            LocalDateTime.of(2023, 9, 19, 10, 0), owner, field2));
        cropsField2.add(new Crop(65, cropType2, 1022,
            LocalDateTime.of(2023, 7, 15, 10, 0), owner, field2));
        cropsField2.add(new Crop(90, cropType1, 885,
            LocalDateTime.of(2023, 5, 10, 10, 0), owner, field2));
        cropsField2.add(new Crop(79, cropType2, 1177,
            LocalDateTime.of(2023, 3, 5, 10, 0), owner, field2));
        cropsField2.add(new Crop(56, cropType1, 940,
            LocalDateTime.of(2022, 12, 30, 10, 0), owner, field2));
        cropsField2.add(new Crop(85, cropType2, 1005,
            LocalDateTime.of(2022, 10, 25, 10, 0), owner, field2));
        cropsField2.add(new Crop(64, cropType1, 1111,
            LocalDateTime.of(2022, 8, 20, 10, 0), owner, field2));
        cropsField2.add(new Crop(94, cropType2, 970,
            LocalDateTime.of(2022, 6, 15, 10, 0), owner, field2));
        cropsField2.add(new Crop(71, cropType1, 892,
            LocalDateTime.of(2022, 4, 10, 10, 0), owner, field2));
        cropsField2.add(new Crop(62, cropType2, 1160,
            LocalDateTime.of(2022, 2, 4, 10, 0), owner, field2));
        cropsField2.add(new Crop(89, cropType1, 1035,
            LocalDateTime.of(2021, 12, 1, 10, 0), owner, field2));
        cropsField2.add(new Crop(75, cropType2, 908,
            LocalDateTime.of(2021, 9, 26, 10, 0), owner, field2));
        cropsField2.add(new Crop(58, cropType1, 1184,
            LocalDateTime.of(2021, 7, 22, 10, 0), owner, field2));
        cropsField2.add(new Crop(95, cropType2, 965,
            LocalDateTime.of(2021, 5, 17, 10, 0), owner, field2));
        cropsField2.add(new Crop(78, cropType1, 860,
            LocalDateTime.of(2021, 3, 12, 10, 0), owner, field2));
        cropsField2.add(new Crop(66, cropType2, 1125,
            LocalDateTime.of(2021, 1, 6, 10, 0), owner, field2));
        cropsField2.add(new Crop(83, cropType1, 1045,
            LocalDateTime.of(2020, 11, 1, 10, 0), owner, field2));
        cropsField2.add(new Crop(70, cropType2, 928,
            LocalDateTime.of(2020, 8, 27, 10, 0), owner, field2));
        cropsField2.add(new Crop(60, cropType1, 1150,
            LocalDateTime.of(2020, 6, 22, 10, 0), owner, field2));
        cropsField2.add(new Crop(93, cropType2, 995,
            LocalDateTime.of(2020, 4, 17, 10, 0), owner, field2));
        cropsField2.add(new Crop(76, cropType1, 875,
            LocalDateTime.of(2020, 2, 11, 10, 0), owner, field2));
        cropsField2.add(new Crop(67, cropType2, 1080,
            LocalDateTime.of(2019, 12, 7, 10, 0), owner, field2));
        cropsField2.add(new Crop(82, cropType1, 1010,
            LocalDateTime.of(2019, 10, 2, 10, 0), owner, field2));
        cropsField2.add(new Crop(63, cropType2, 1195,
            LocalDateTime.of(2019, 7, 28, 10, 0), owner, field2));
        cropsField2.add(new Crop(91, cropType1, 958,
            LocalDateTime.of(2019, 5, 23, 10, 0), owner, field2));
        cropsField2.add(new Crop(74, cropType2, 855,
            LocalDateTime.of(2019, 3, 18, 10, 0), owner, field2));
        cropsField2.add(new Crop(57, cropType1, 1130,
            LocalDateTime.of(2019, 1, 12, 10, 0), owner, field2));
        cropsField2.add(new Crop(86, cropType2, 1065,
            LocalDateTime.of(2018, 11, 7, 10, 0), owner, field2));
        cropsField2.add(new Crop(69, cropType1, 935,
            LocalDateTime.of(2018, 9, 2, 10, 0), owner, field2));
        cropsField2.add(new Crop(87, cropType2, 1105,
            LocalDateTime.of(2018, 6, 28, 10, 0), owner, field2));
        cropsField2.add(new Crop(72, cropType1, 990,
            LocalDateTime.of(2018, 4, 23, 10, 0), owner, field2));
        cropsField2.add(new Crop(84, cropType2, 880,
            LocalDateTime.of(2018, 2, 17, 10, 0), owner, field2));
        cropsField2.add(new Crop(95, cropType1, 1170,
            LocalDateTime.of(2017, 12, 13, 10, 0), owner, field2));
        cropsField2.add(new Crop(68, cropType2, 1015,
            LocalDateTime.of(2017, 10, 8, 10, 0), owner, field2));
        cropsField2.add(new Crop(81, cropType1, 905,
            LocalDateTime.of(2017, 8, 3, 10, 0), owner, field2));
        cropsField2.add(new Crop(59, cropType2, 1120,
            LocalDateTime.of(2017, 5, 29, 10, 0), owner, field2));
        cropsField2.add(new Crop(92, cropType1, 945,
            LocalDateTime.of(2017, 3, 24, 10, 0), owner, field2));
        cropsField2.add(new Crop(77, cropType2, 1075,
            LocalDateTime.of(2017, 1, 18, 10, 0), owner, field2));
        cropsField2.add(new Crop(61, cropType1, 898,
            LocalDateTime.of(2016, 11, 13, 10, 0), owner, field2));
        cropsField2.add(new Crop(88, cropType2, 1135,
            LocalDateTime.of(2016, 9, 8, 10, 0), owner, field2));
        cropsField2.add(new Crop(73, cropType1, 975,
            LocalDateTime.of(2016, 7, 4, 10, 0), owner, field2));
        cropsField2.add(new Crop(65, cropType2, 1050,
            LocalDateTime.of(2016, 4, 29, 10, 0), owner, field2));
        cropsField2.add(new Crop(90, cropType1, 918,
            LocalDateTime.of(2016, 2, 24, 10, 0), owner, field2));

        cropRepository.saveAll(cropsField2);
    }

    private void generateTools(Farmer owner) {
        LOGGER.debug("Ensuring tools exist for owner {}", owner.getEmail());

        final ToolCategory heavyMachinery = createOrGetCategory("Heavy Machinery", owner);
        final ToolCategory lightMachinery = createOrGetCategory("Light Machinery", owner);
        final ToolCategory harvestingEquipment = createOrGetCategory("Harvesting Equipment", owner);
        final ToolCategory plantingEquipment = createOrGetCategory("Planting Equipment", owner);
        final ToolCategory irrigationSystems = createOrGetCategory("Irrigation Systems", owner);

        final Garage garage1 = getGarage("Garage 1");
        final Garage garage2 = getGarage("Garage 2");

        final String image = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAIAAACRXR/mAAAAUElEQVR4nO3OMQHAIADAMEDS1CERWTOwpxc"
            + "7EgWZzz7jf9btwDetQqvQKrQKrUKr0Cq0Cq1Cq9AqtAqtQqvQKrQKrUKr0Cq0Cq1Cq9AqtIoXf4wB1ppa1sUAAAAASUVORK5CYII=";
        byte[] imageBytes = image.getBytes();

        createOrGetTool("Plow C", lightMachinery,
            "Durable plow for soil preparation.", 65.0, garage1,
            new Coordinate(0, 0, 0), true, imageBytes, owner);

        createOrGetTool("Seeder D", heavyMachinery,
            "High-precision seeder for efficient planting.", 70.0, garage2,
            new Coordinate(2, 0, 0), false, null, owner);

        createOrGetTool("Disc Harrow", lightMachinery,
            "Harrow for breaking up and smoothing soil.", 55.0, garage1,
            new Coordinate(5, 3, 0), true, null, owner);

        createOrGetTool("Rotary Tiller", lightMachinery,
            "Tiller for fine soil preparation.", 60.0, garage1,
            new Coordinate(6, 4, 0), true, null, owner);

        createOrGetTool("Grain Header", harvestingEquipment,
            "Header attachment for grain harvesting.", 80.0, garage2,
            new Coordinate(10, 2, 0), true, null, owner);

        createOrGetTool("Corn Header", harvestingEquipment,
            "Header for corn harvesting.", 75.0, garage2,
            new Coordinate(11, 3, 0), false, null, owner);

        createOrGetTool("Row Planter", plantingEquipment,
            "Planter for multi-row seeding.", 68.0, garage1,
            new Coordinate(3, 6, 0), true, null, owner);

        createOrGetTool("Sprinkler Line", irrigationSystems,
            "Movable sprinkler line for crop irrigation.", 50.0, garage2,
            new Coordinate(7, 7, 0), true, null, owner);
    }

    private Tool createOrGetTool(String name,
                                 ToolCategory category,
                                 String description,
                                 double remainingValue,
                                 Garage garage,
                                 Coordinate coordinate,
                                 boolean usable,
                                 byte[] image,
                                 Farmer owner) {

        return toolRepository.findByNameAndOwner(name, owner)
            .orElseGet(() -> {
                LOGGER.debug("Creating tool {} for owner {}", name, owner.getEmail());
                Tool tool = new Tool(name, category, description, remainingValue, garage, coordinate, usable, image);
                tool.setOwner(owner);
                tool.setOwnerId(owner.getId());
                return toolRepository.save(tool);
            });
    }

    private void generateVehicles(Farmer owner) {
        LOGGER.debug("Ensuring vehicles exist for owner {}", owner.getEmail());

        final ToolCategory heavyMachinery = createOrGetCategory("Heavy Machinery", owner);
        final ToolCategory lightMachinery = createOrGetCategory("Light Machinery", owner);
        final ToolCategory harvestingEquipment = createOrGetCategory("Harvesting Equipment", owner);
        final ToolCategory plantingEquipment = createOrGetCategory("Planting Equipment", owner);
        final ToolCategory irrigationSystems = createOrGetCategory("Irrigation Systems", owner);

        final Garage garage1 = getGarage("Garage 1");
        final Garage garage2 = getGarage("Garage 2");

        final String image = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAIAAACRXR/mAAAAT0lEQVR4nO3OMQHAIADAMEAS/gUgCwN7erEjUZB59h7/"
            + "s14HvmkVWoVWoVVoFVqFVqFVaBVahVahVWgVWoVWoVVoFVqFVqFVaBVahVahVVxwWAGQXEYE8AAAAABJRU5ErkJggg==";
        byte[] imageBytes = image.getBytes();

        createOrGetVehicle("Tractor A",
            "A powerful tractor for heavy-duty farming tasks.",
            85.0, lightMachinery, garage1,
            new Coordinate(0, 0, 0), true, imageBytes,
            99.5, 884.2, owner);

        createOrGetVehicle("Harvester B",
            "Efficient harvester for large-scale crop harvesting.",
            78.0, heavyMachinery, garage2,
            new Coordinate(0, 0, 0), true, null,
            75.0, 1200.5, owner);

        createOrGetVehicle("Sprayer Truck",
            "Truck-mounted sprayer for crop protection.",
            72.0, irrigationSystems, garage1,
            new Coordinate(4, 1, 0), true, null,
            60.0, 540.0, owner);

        createOrGetVehicle("Utility Pickup",
            "General-purpose pickup for farm logistics.",
            65.0, lightMachinery, garage1,
            new Coordinate(2, 4, 0), false, null,
            55.0, 320.0, owner);

        createOrGetVehicle("Seeder Tractor",
            "Tractor optimized for pulling seeding equipment.",
            80.0, plantingEquipment, garage2,
            new Coordinate(5, 5, 0), true, null,
            70.0, 950.0, owner);

        createOrGetVehicle("Grain Cart",
            "High-capacity grain cart for harvest logistics.",
            77.0, harvestingEquipment, garage2,
            new Coordinate(8, 2, 0), true, null,
            65.0, 670.0, owner);

        createOrGetVehicle("Field Sprayer",
            "Self-propelled sprayer for large fields.",
            82.0, irrigationSystems, garage1,
            new Coordinate(6, 3, 0), false, null,
            80.0, 430.0, owner);

        createOrGetVehicle("Compact Loader",
            "Small loader for material handling around the farm.",
            69.0, heavyMachinery, garage2,
            new Coordinate(3, 7, 0), true, null,
            50.0, 210.0, owner);
    }

    private Vehicle createOrGetVehicle(String name,
                                       String description,
                                       double remainingValue,
                                       ToolCategory category,
                                       Garage garage,
                                       Coordinate coordinate,
                                       boolean usable,
                                       byte[] image,
                                       double fuelLevel,
                                       double drivenKm,
                                       Farmer owner) {

        return vehicleRepository.findByNameAndOwner(name, owner)
            .orElseGet(() -> {
                LOGGER.debug("Creating vehicle {} for owner {}", name, owner.getEmail());
                Vehicle vehicle = new Vehicle(name, description, remainingValue, category, garage,
                    coordinate, usable, image, fuelLevel, drivenKm);
                vehicle.setOwner(owner);
                vehicle.setOwnerId(owner.getId());
                return vehicleRepository.save(vehicle);
            });
    }

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);

    private Polygon createRectField(double centerLat, double centerLon,
                                    double halfHeightLat, double halfWidthLon) {

        Coordinate[] coords = new Coordinate[]{
            new Coordinate(centerLon - halfWidthLon, centerLat - halfHeightLat),
            new Coordinate(centerLon + halfWidthLon, centerLat - halfHeightLat),
            new Coordinate(centerLon + halfWidthLon, centerLat + halfHeightLat),
            new Coordinate(centerLon - halfWidthLon, centerLat + halfHeightLat),
            new Coordinate(centerLon - halfWidthLon, centerLat - halfHeightLat) // close polygon
        };

        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(coords);
        return GEOMETRY_FACTORY.createPolygon(shell, null);
    }
}
