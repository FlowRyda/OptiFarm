package at.ac.tuwien.ase.groupphase.backend.datagenerator;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.TaskType;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.GarageRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.PlannedTaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ScheduleRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.SeedRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolCategoryRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Profile("dev")
@Component
public class DevDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final VehicleRepository vehicleRepository;
    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final GarageRepository garageRepository;
    private final FieldRepository fieldRepository;
    private final ActiveFieldRepository activeFieldRepository;
    private final CropTypeRepository cropTypeRepository;
    private final CropRepository cropRepository;
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlannedTaskRepository plannedTaskRepository;
    private final SeedRepository seedRepository;


    @Autowired
    public DevDataGenerator(FarmerRepository farmerRepository,
                            PasswordEncoder passwordEncoder,
                            VehicleRepository vehicleRepository,
                            ToolRepository toolRepository,
                            ToolCategoryRepository toolCategoryRepository,
                            GarageRepository garageRepository,
                            FieldRepository fieldRepository, ActiveFieldRepository activeFieldRepository,
                            CropTypeRepository cropTypeRepository,
                            TaskRepository taskRepository,
                            CropRepository cropRepository, ScheduleRepository scheduleRepository, PlannedTaskRepository plannedTaskRepository, SeedRepository seedRepository) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.vehicleRepository = vehicleRepository;
        this.toolRepository = toolRepository;
        this.toolCategoryRepository = toolCategoryRepository;
        this.garageRepository = garageRepository;
        this.fieldRepository = fieldRepository;
        this.activeFieldRepository = activeFieldRepository;
        this.cropTypeRepository = cropTypeRepository;
        this.cropRepository = cropRepository;
        this.scheduleRepository = scheduleRepository;
        this.plannedTaskRepository = plannedTaskRepository;
        this.seedRepository = seedRepository;
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    private void generateData() {
        clearData();
        createFarmers();
        createToolCategory();
        createCropTypes();
        createGarages();
        createTools();
        createVehicles();
        createFields();
        createSeeds();
        createActiveFields();
        createCrops();
        createFieldVisualizationData();
        createTasks();
        createComplexTasks();
        createJoeyFullPlanTasks();
    }

    private void clearData() {
        plannedTaskRepository.deleteAll();
        taskRepository.deleteAll();
        scheduleRepository.deleteAll();
        cropRepository.deleteAll();
        activeFieldRepository.deleteAll();
        fieldRepository.deleteAll();
        seedRepository.deleteAll();
        vehicleRepository.deleteAll();
        toolRepository.deleteAll();
        garageRepository.deleteAll();
        toolCategoryRepository.deleteAll();
        cropTypeRepository.deleteAll();
        farmerRepository.deleteAll();
    }

    private Farmer JOHN_DOE;
    private Farmer JANE_DOE;
    private Farmer JOEY_DOE;

    private void createFarmers() {
        var john = new Farmer("john@doe.com", passwordEncoder.encode("password"), "John", "Doe");
        var jane = new Farmer("jane@doe.com", passwordEncoder.encode("password"), "Jane", "Doe");
        var joey = new Farmer("joey@doe.com", passwordEncoder.encode("password"), "Joey", "Doe");

        JOHN_DOE = farmerRepository.save(john);
        JANE_DOE = farmerRepository.save(jane);
        JOEY_DOE = farmerRepository.save(joey);
    }


    private ToolCategory TOOL_CATEGORY_TRACTOR_JANE;
    private ToolCategory TOOL_CATEGORY_LAWN_MOWER_JANE;
    private ToolCategory TOOL_CATEGORY_CHAINSAW_JANE;
    private ToolCategory TOOL_CATEGORY_GENERATOR_JANE;
    private ToolCategory TOOL_CATEGORY_POWER_WASHER_JANE;
    private ToolCategory TOOL_CATEGORY_DRILL_JANE;

    private ToolCategory TOOL_CATEGORY_TRACTOR_JOHN;
    private ToolCategory TOOL_CATEGORY_LAWN_MOWER_JOHN;
    private ToolCategory TOOL_CATEGORY_CHAINSAW_JOHN;
    private ToolCategory TOOL_CATEGORY_GENERATOR_JOHN;
    private ToolCategory TOOL_CATEGORY_POWER_WASHER_JOHN;
    private ToolCategory TOOL_CATEGORY_DRILL_JOHN;

    private ToolCategory TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOHN;
    private ToolCategory TOOL_CATEGORY_COMBINE_JOHN;
    private ToolCategory TOOL_CATEGORY_SPRAYER_JOHN;
    private ToolCategory TOOL_CATEGORY_FORAGE_HARVESTER_JOHN;
    private ToolCategory TOOL_CATEGORY_TELEHANDLER_JOHN;

    private Garage GARAGE_JOHN_1;
    private Garage GARAGE_JOHN_2;
    private Garage GARAGE_JOHN_3;
    private Garage GARAGE_JOHN_4;
    private Garage GARAGE_JOHN_5;

    private List<Garage> JOHN_GARAGES = new ArrayList<>();

    private ToolCategory TOOL_CATEGORY_TRACTOR_JOEY;
    private ToolCategory TOOL_CATEGORY_LAWN_MOWER_JOEY;
    private ToolCategory TOOL_CATEGORY_CHAINSAW_JOEY;
    private ToolCategory TOOL_CATEGORY_GENERATOR_JOEY;
    private ToolCategory TOOL_CATEGORY_POWER_WASHER_JOEY;
    private ToolCategory TOOL_CATEGORY_DRILL_JOEY;

    private ToolCategory TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOEY;
    private ToolCategory TOOL_CATEGORY_COMBINE_JOEY;
    private ToolCategory TOOL_CATEGORY_SPRAYER_JOEY;
    private ToolCategory TOOL_CATEGORY_FORAGE_HARVESTER_JOEY;
    private ToolCategory TOOL_CATEGORY_TELEHANDLER_JOEY;

    private Task TASK_1_JANE;
    private Task TASK_2_JANE;
    private Task TASK_1_JOHN;
    private Task TASK_2_JOHN;
    private Task TASK_1_JOEY;
    private Task TASK_2_JOEY;


    private void createToolCategory() {
        TOOL_CATEGORY_TRACTOR_JANE = createToolCategory("Tractor", JANE_DOE);
        TOOL_CATEGORY_LAWN_MOWER_JANE = createToolCategory("Lawn Mower", JANE_DOE);
        TOOL_CATEGORY_CHAINSAW_JANE = createToolCategory("Chainsaw", JANE_DOE);
        TOOL_CATEGORY_GENERATOR_JANE = createToolCategory("Generator", JANE_DOE);
        TOOL_CATEGORY_POWER_WASHER_JANE = createToolCategory("Power Washer", JANE_DOE);
        TOOL_CATEGORY_DRILL_JANE = createToolCategory("Drill", JANE_DOE);

        TOOL_CATEGORY_TRACTOR_JOHN = createToolCategory("Tractor", JOHN_DOE);
        TOOL_CATEGORY_LAWN_MOWER_JOHN = createToolCategory("Lawn Mower", JOHN_DOE);
        TOOL_CATEGORY_CHAINSAW_JOHN = createToolCategory("Chainsaw", JOHN_DOE);
        TOOL_CATEGORY_GENERATOR_JOHN = createToolCategory("Generator", JOHN_DOE);
        TOOL_CATEGORY_POWER_WASHER_JOHN = createToolCategory("Power Washer", JOHN_DOE);
        TOOL_CATEGORY_DRILL_JOHN = createToolCategory("Drill", JOHN_DOE);

        TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOHN = createToolCategory("Tractor Implement", JOHN_DOE);
        TOOL_CATEGORY_COMBINE_JOHN = createToolCategory("Combine Harvester", JOHN_DOE);
        TOOL_CATEGORY_SPRAYER_JOHN = createToolCategory("Self-Propelled Sprayer", JOHN_DOE);
        TOOL_CATEGORY_FORAGE_HARVESTER_JOHN = createToolCategory("Forage Harvester", JOHN_DOE);
        TOOL_CATEGORY_TELEHANDLER_JOHN = createToolCategory("Telehandler", JOHN_DOE);

        TOOL_CATEGORY_TRACTOR_JOEY = createToolCategory("Tractor", JOEY_DOE);
        TOOL_CATEGORY_LAWN_MOWER_JOEY = createToolCategory("Lawn Mower", JOEY_DOE);
        TOOL_CATEGORY_CHAINSAW_JOEY = createToolCategory("Chainsaw", JOEY_DOE);
        TOOL_CATEGORY_GENERATOR_JOEY = createToolCategory("Generator", JOEY_DOE);
        TOOL_CATEGORY_POWER_WASHER_JOEY = createToolCategory("Power Washer", JOEY_DOE);
        TOOL_CATEGORY_DRILL_JOEY = createToolCategory("Drill", JOEY_DOE);

        TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOEY = createToolCategory("Tractor Implement", JOEY_DOE);
        TOOL_CATEGORY_COMBINE_JOEY = createToolCategory("Combine Harvester", JOEY_DOE);
        TOOL_CATEGORY_SPRAYER_JOEY = createToolCategory("Self-Propelled Sprayer", JOEY_DOE);
        TOOL_CATEGORY_FORAGE_HARVESTER_JOEY = createToolCategory("Forage Harvester", JOEY_DOE);
        TOOL_CATEGORY_TELEHANDLER_JOEY = createToolCategory("Telehandler", JOEY_DOE);
    }

    private ToolCategory createToolCategory(String name, Farmer farmer) {
        return toolCategoryRepository.save(new ToolCategory(name, farmer));
    }

    private CropType CROP_TYPE_CORN_JANE;
    private CropType CROP_TYPE_WHEAT_JANE;
    private CropType CROP_TYPE_SOYBEAN_JANE;
    private CropType CROP_TYPE_RICE_JANE;
    private CropType CROP_TYPE_BARLEY_JANE;
    private CropType CROP_TYPE_OATS_JANE;
    private CropType CROP_TYPE_COTTON_JANE;
    private CropType CROP_TYPE_POTATO_JANE;
    private CropType CROP_TYPE_TOMATO_JANE;

    private CropType CROP_TYPE_CORN_JOHN;
    private CropType CROP_TYPE_WHEAT_JOHN;
    private CropType CROP_TYPE_SOYBEAN_JOHN;
    private CropType CROP_TYPE_RICE_JOHN;
    private CropType CROP_TYPE_BARLEY_JOHN;
    private CropType CROP_TYPE_OATS_JOHN;
    private CropType CROP_TYPE_COTTON_JOHN;
    private CropType CROP_TYPE_POTATO_JOHN;
    private CropType CROP_TYPE_TOMATO_JOHN;

    private CropType CROP_TYPE_CORN_JOEY;
    private CropType CROP_TYPE_WHEAT_JOEY;
    private CropType CROP_TYPE_SOYBEAN_JOEY;
    private CropType CROP_TYPE_RICE_JOEY;
    private CropType CROP_TYPE_BARLEY_JOEY;
    private CropType CROP_TYPE_OATS_JOEY;
    private CropType CROP_TYPE_COTTON_JOEY;
    private CropType CROP_TYPE_POTATO_JOEY;
    private CropType CROP_TYPE_TOMATO_JOEY;

    private void createCropTypes() {
        CROP_TYPE_CORN_JANE = createCropType("Corn", 80, JANE_DOE);
        CROP_TYPE_WHEAT_JANE = createCropType("Wheat", 100, JANE_DOE);
        CROP_TYPE_SOYBEAN_JANE = createCropType("Soybean", 75, JANE_DOE);
        CROP_TYPE_RICE_JANE = createCropType("Rice", 120, JANE_DOE);
        CROP_TYPE_BARLEY_JANE = createCropType("Barley", 85, JANE_DOE);
        CROP_TYPE_OATS_JANE = createCropType("Oats", 110, JANE_DOE);
        CROP_TYPE_COTTON_JANE = createCropType("Cotton", 150, JANE_DOE);
        CROP_TYPE_POTATO_JANE = createCropType("Potato", 100, JANE_DOE);
        CROP_TYPE_TOMATO_JANE = createCropType("Tomato", 70, JANE_DOE);

        CROP_TYPE_CORN_JOHN = createCropType("Corn", 80, JOHN_DOE);
        CROP_TYPE_WHEAT_JOHN = createCropType("Wheat", 100, JOHN_DOE);
        CROP_TYPE_SOYBEAN_JOHN = createCropType("Soybean", 75, JOHN_DOE);
        CROP_TYPE_RICE_JOHN = createCropType("Rice", 120, JOHN_DOE);
        CROP_TYPE_BARLEY_JOHN = createCropType("Barley", 85, JOHN_DOE);
        CROP_TYPE_OATS_JOHN = createCropType("Oats", 110, JOHN_DOE);
        CROP_TYPE_COTTON_JOHN = createCropType("Cotton", 150, JOHN_DOE);
        CROP_TYPE_POTATO_JOHN = createCropType("Potato", 100, JOHN_DOE);
        CROP_TYPE_TOMATO_JOHN = createCropType("Tomato", 70, JOHN_DOE);

        CROP_TYPE_CORN_JOEY = createCropType("Corn", 80, JOEY_DOE);
        CROP_TYPE_WHEAT_JOEY = createCropType("Wheat", 100, JOEY_DOE);
        CROP_TYPE_SOYBEAN_JOEY = createCropType("Soybean", 75, JOEY_DOE);
        CROP_TYPE_RICE_JOEY = createCropType("Rice", 120, JOEY_DOE);
        CROP_TYPE_BARLEY_JOEY = createCropType("Barley", 85, JOEY_DOE);
        CROP_TYPE_OATS_JOEY = createCropType("Oats", 110, JOEY_DOE);
        CROP_TYPE_COTTON_JOEY = createCropType("Cotton", 150, JOEY_DOE);
        CROP_TYPE_POTATO_JOEY = createCropType("Potato", 100, JOEY_DOE);
        CROP_TYPE_TOMATO_JOEY = createCropType("Tomato", 70, JOEY_DOE);
    }

    private CropType createCropType(String name, int growthDuration, Farmer farmer) {
        return cropTypeRepository.save(new CropType(name, growthDuration, new ArrayList<>(), farmer));
    }

    private Garage GARAGE_JANE;
    private Garage GARAGE_JOHN;
    private Garage GARAGE_JOEY;

    double baseLatJane = 48.299611;
    double baseLatJohn = 48.296500;
    double baseLon = 15.970889;

    private void createGarages() {


        GARAGE_JANE = createGarage(
            "Jane's Garage",
            new Coordinate(baseLatJane + 0.0004, baseLon - 0.0006), // x = lat, y = lon
            JANE_DOE,
            new ArrayList<>()
        );

        GARAGE_JOHN = createGarage(
            "John's Garage",
            new Coordinate(baseLatJohn + 0.0004, baseLon - 0.0006),
            JOHN_DOE,
            new ArrayList<>()
        );

        // --- John's 5 garages ---
        GARAGE_JOHN_1 = createGarage(
            "John Garage 1 (Main)",
            new Coordinate(baseLatJohn + 0.0004, baseLon - 0.0006),
            JOHN_DOE,
            new ArrayList<>()
        );
        GARAGE_JOHN_2 = createGarage(
            "John Garage 2 (North)",
            new Coordinate(baseLatJohn + 0.0022, baseLon - 0.0002),
            JOHN_DOE,
            new ArrayList<>()
        );
        GARAGE_JOHN_3 = createGarage(
            "John Garage 3 (East)",
            new Coordinate(baseLatJohn + 0.0008, baseLon + 0.0021),
            JOHN_DOE,
            new ArrayList<>()
        );
        GARAGE_JOHN_4 = createGarage(
            "John Garage 4 (South)",
            new Coordinate(baseLatJohn - 0.0018, baseLon - 0.0003),
            JOHN_DOE,
            new ArrayList<>()
        );
        GARAGE_JOHN_5 = createGarage(
            "John Garage 5 (West)",
            new Coordinate(baseLatJohn + 0.0006, baseLon - 0.0023),
            JOHN_DOE,
            new ArrayList<>()
        );
        GARAGE_JOEY = createGarage(
            "Joey's Garage",
            new Coordinate(baseLatJohn + 0.0004, baseLon - 0.0006),
            JOEY_DOE,
            new ArrayList<>()
        );

        JOHN_GARAGES = List.of(GARAGE_JOHN, GARAGE_JOHN_1, GARAGE_JOHN_2, GARAGE_JOHN_3, GARAGE_JOHN_4, GARAGE_JOHN_5);

        JOEY_GARAGES = List.of(GARAGE_JOEY);
    }

    private Coordinate nearGarage(Garage g, int idx) {
        double lat = g.getCoordinate().getX();
        double lon = g.getCoordinate().getY();

        double dlat = ((idx % 5) - 2) * 0.00007;
        double dlon = (((idx / 5) % 5) - 2) * 0.00007;

        return new Coordinate(lat + dlat, lon + dlon);
    }

    private Garage johnGarageForIndex(int idx) {
        return JOHN_GARAGES.get(idx % JOHN_GARAGES.size());
    }

    private Garage createGarage(String name, Coordinate coordinate, Farmer farmer, List<Tool> tools) {
        return garageRepository.save(new Garage(name, coordinate, farmer, tools));
    }

    private Tool HAMMER_JANE, SAW_JANE, SHOVEL_JANE, RAKE_JANE, WRENCH_JANE;
    private Tool PITCHFORK_JANE, HOE_JANE, TROWEL_JANE, PLIERS_JANE, AXE_JANE;

    private Tool HAMMER_JOHN, SAW_JOHN, SHOVEL_JOHN, RAKE_JOHN, WRENCH_JOHN;
    private Tool PITCHFORK_JOHN, HOE_JOHN, TROWEL_JOHN, PLIERS_JOHN, AXE_JOHN;

    private Tool HAMMER_JOEY, SAW_JOEY, SHOVEL_JOEY, RAKE_JOEY, WRENCH_JOEY;
    private Tool PITCHFORK_JOEY, HOE_JOEY, TROWEL_JOEY, PLIERS_JOEY, AXE_JOEY;

    private void createTools() {
        double janeLat = GARAGE_JANE.getCoordinate().getX(); // lat
        double janeLon = GARAGE_JANE.getCoordinate().getY(); // lon

        HAMMER_JANE = createTool("Hammer", TOOL_CATEGORY_DRILL_JANE,
            "A heavy-duty hammer for farming tasks.", 100.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat, janeLon), true);

        SAW_JANE = createTool("Saw", TOOL_CATEGORY_CHAINSAW_JANE,
            "A hand saw for cutting wood.", 90.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat + 0.00010, janeLon + 0.00015), true);

        SHOVEL_JANE = createTool("Shovel", TOOL_CATEGORY_LAWN_MOWER_JANE,
            "A garden shovel for digging soil.", 80.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat - 0.00010, janeLon + 0.00015), true);

        RAKE_JANE = createTool("Rake", TOOL_CATEGORY_LAWN_MOWER_JANE,
            "A rake for gathering leaves and debris.", 70.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat + 0.00015, janeLon - 0.00010), true);

        WRENCH_JANE = createTool("Wrench", TOOL_CATEGORY_DRILL_JANE,
            "A wrench for tightening bolts.", 60.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat - 0.00015, janeLon - 0.00010), true);

        PITCHFORK_JANE = createTool("Pitchfork", TOOL_CATEGORY_CHAINSAW_JANE,
            "A pitchfork for handling hay or manure.", 50.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat + 0.00005, janeLon + 0.00025), true);

        HOE_JANE = createTool("Hoe", TOOL_CATEGORY_POWER_WASHER_JANE,
            "A hoe for cultivating the soil.", 40.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat - 0.00005, janeLon + 0.00025), true);

        TROWEL_JANE = createTool("Trowel", TOOL_CATEGORY_DRILL_JANE,
            "A small hand trowel for planting.", 30.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat + 0.00008, janeLon - 0.00022), true);

        PLIERS_JANE = createTool("Pliers", TOOL_CATEGORY_DRILL_JANE,
            "Pliers for gripping and manipulating objects.", 20.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat - 0.00008, janeLon - 0.00022), true);

        AXE_JANE = createTool("Axe", TOOL_CATEGORY_CHAINSAW_JANE,
            "An axe for chopping wood.", 10.0, null,
            GARAGE_JANE, JANE_DOE, new Coordinate(janeLat, janeLon + 0.00035), true);


        // JOHN base = his garage position
        double johnLat = GARAGE_JOHN.getCoordinate().getX(); // lat
        double johnLon = GARAGE_JOHN.getCoordinate().getY(); // lon

        HAMMER_JOHN = createTool("Hammer", TOOL_CATEGORY_DRILL_JOHN,
            "A heavy-duty hammer for farming tasks.", 100.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat, johnLon), true);

        SAW_JOHN = createTool("Saw", TOOL_CATEGORY_CHAINSAW_JOHN,
            "A hand saw for cutting wood.", 90.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat + 0.00010, johnLon + 0.00015), true);

        SHOVEL_JOHN = createTool("Shovel", TOOL_CATEGORY_LAWN_MOWER_JOHN,
            "A garden shovel for digging soil.", 80.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat - 0.00010, johnLon + 0.00015), true);

        RAKE_JOHN = createTool("Rake", TOOL_CATEGORY_LAWN_MOWER_JOHN,
            "A rake for gathering leaves and debris.", 70.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat + 0.00015, johnLon - 0.00010), true);

        WRENCH_JOHN = createTool("Wrench", TOOL_CATEGORY_DRILL_JOHN,
            "A wrench for tightening bolts.", 60.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat - 0.00015, johnLon - 0.00010), true);

        PITCHFORK_JOHN = createTool("Pitchfork", TOOL_CATEGORY_CHAINSAW_JOHN,
            "A pitchfork for handling hay or manure.", 50.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat + 0.00005, johnLon + 0.00025), true);

        HOE_JOHN = createTool("Hoe", TOOL_CATEGORY_POWER_WASHER_JOHN,
            "A hoe for cultivating the soil.", 40.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat - 0.00005, johnLon + 0.00025), true);

        TROWEL_JOHN = createTool("Trowel", TOOL_CATEGORY_DRILL_JOHN,
            "A small hand trowel for planting.", 30.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat + 0.00008, johnLon - 0.00022), true);

        PLIERS_JOHN = createTool("Pliers", TOOL_CATEGORY_DRILL_JOHN,
            "Pliers for gripping and manipulating objects.", 20.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat - 0.00008, johnLon - 0.00022), true);

        AXE_JOHN = createTool("Axe", TOOL_CATEGORY_CHAINSAW_JOHN,
            "An axe for chopping wood.", 10.0, null,
            GARAGE_JOHN, JOHN_DOE, new Coordinate(johnLat, johnLon + 0.00035), true);

        // JOEY base = his garage position
        double joeyLat = GARAGE_JOEY.getCoordinate().getX(); // lat
        double joeyLon = GARAGE_JOEY.getCoordinate().getY(); // lon

        HAMMER_JOEY = createTool("Hammer", TOOL_CATEGORY_DRILL_JOEY,
            "A heavy-duty hammer for farming tasks.", 100.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat, joeyLon), true);

        SAW_JOEY = createTool("Saw", TOOL_CATEGORY_CHAINSAW_JOEY,
            "A hand saw for cutting wood.", 90.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat + 0.00010, joeyLon + 0.00015), true);

        SHOVEL_JOEY = createTool("Shovel", TOOL_CATEGORY_LAWN_MOWER_JOEY,
            "A garden shovel for digging soil.", 80.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat - 0.00010, joeyLon + 0.00015), true);

        RAKE_JOEY = createTool("Rake", TOOL_CATEGORY_LAWN_MOWER_JOEY,
            "A rake for gathering leaves and debris.", 70.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat + 0.00015, joeyLon - 0.00010), true);

        WRENCH_JOEY = createTool("Wrench", TOOL_CATEGORY_DRILL_JOEY,
            "A wrench for tightening bolts.", 60.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat - 0.00015, joeyLon - 0.00010), true);

        PITCHFORK_JOEY = createTool("Pitchfork", TOOL_CATEGORY_CHAINSAW_JOEY,
            "A pitchfork for handling hay or manure.", 50.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat + 0.00005, joeyLon + 0.00025), true);

        HOE_JOEY = createTool("Hoe", TOOL_CATEGORY_POWER_WASHER_JOEY,
            "A hoe for cultivating the soil.", 40.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat - 0.00005, joeyLon + 0.00025), true);

        TROWEL_JOEY = createTool("Trowel", TOOL_CATEGORY_DRILL_JOEY,
            "A small hand trowel for planting.", 30.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat + 0.00008, joeyLon - 0.00022), true);

        PLIERS_JOEY = createTool("Pliers", TOOL_CATEGORY_DRILL_JOEY,
            "Pliers for gripping and manipulating objects.", 20.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat - 0.00008, joeyLon - 0.00022), true);

        AXE_JOEY = createTool("Axe", TOOL_CATEGORY_CHAINSAW_JOEY,
            "An axe for chopping wood.", 10.0, null,
            GARAGE_JOEY, JOEY_DOE, new Coordinate(joeyLat, joeyLon + 0.00035), true);

        createJohnTractorImplements();
        createJoeyTractorImplements();
    }

    private Tool createTool(String name, ToolCategory category, String description, double remainingValue, byte[] image, Garage garage, Farmer farmer, Coordinate coordinate, boolean isUsable) {
        return toolRepository.save(new Tool(name, category, description, remainingValue, image, garage, farmer, coordinate, isUsable));
    }

    private void createJohnTractorImplements() {
        record ImplementSpec(String name, String desc, double value) {
        }

        List<ImplementSpec> implementsList = List.of(
            new ImplementSpec("Moldboard Plow", "Classic moldboard plow for primary tillage.", 9000),
            new ImplementSpec("Chisel Plow", "Deep tillage implement for breaking hardpan.", 11000),
            new ImplementSpec("Disc Harrow", "Disc harrow for residue management and soil prep.", 8500),
            new ImplementSpec("Power Harrow", "Rotary harrow for fine seedbed preparation.", 14000),
            new ImplementSpec("Field Cultivator", "Cultivator for shallow tillage and weed control.", 7000),
            new ImplementSpec("Subsoiler", "Subsoiler for deep ripping and aeration.", 12000),
            new ImplementSpec("Seed Drill", "Seed drill for precise cereal seeding.", 18000),
            new ImplementSpec("Row Planter", "Planter for row crops with adjustable spacing.", 22000),
            new ImplementSpec("Fertilizer Spreader", "Broadcast spreader for granular fertilizer.", 9500),
            new ImplementSpec("Manure Spreader", "Trailer-mounted manure spreader.", 16000),
            new ImplementSpec("Boom Sprayer (Towed)", "Towed boom sprayer for pesticides/herbicides.", 20000),
            new ImplementSpec("Rotary Mower", "Rotary cutter for grass and light brush.", 6000),
            new ImplementSpec("Mower Conditioner", "Cuts and conditions hay for faster drying.", 17000),
            new ImplementSpec("Hay Rake", "Rake for windrowing hay.", 5000),
            new ImplementSpec("Tedder", "Tedder for spreading hay to dry evenly.", 5200),
            new ImplementSpec("Round Baler", "Produces round bales.", 26000),
            new ImplementSpec("Square Baler", "Produces square bales.", 28000),
            new ImplementSpec("Bale Wrapper", "Wraps bales for silage.", 21000),
            new ImplementSpec("Grain Cart", "High-capacity grain cart for harvest logistics.", 24000),
            new ImplementSpec("Flatbed Trailer (8t)", "General purpose flatbed trailer.", 9000),
            new ImplementSpec("Tipping Trailer (12t)", "Dump trailer for grain/soil transport.", 14000),
            new ImplementSpec("Front Loader Attachment", "Front loader for tractor (bucket).", 8000),
            new ImplementSpec("Pallet Fork Attachment", "Forks for pallets/bales.", 3500),
            new ImplementSpec("Stone Picker", "Collects stones from fields.", 19000),
            new ImplementSpec("Land Roller", "Roller for leveling seedbeds and pressing stones.", 12000)
        );

        for (int i = 0; i < implementsList.size(); i++) {
            Garage g = johnGarageForIndex(i);
            ImplementSpec s = implementsList.get(i);

            createTool(
                s.name(),
                TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOHN,
                s.desc(),
                s.value(),
                null,
                g,
                JOHN_DOE,
                nearGarage(g, i),
                true
            );
        }
    }

    private Vehicle TRACTOR_JANE, LAWN_MOWER_JANE, GENERATOR_JANE;
    private Vehicle TRACTOR_JOHN, LAWN_MOWER_JOHN, GENERATOR_JOHN;
    private Vehicle TRACTOR_JOEY, LAWN_MOWER_JOEY, GENERATOR_JOEY;

    private void createVehicles() {
        double janeLat = GARAGE_JANE.getCoordinate().getX();
        double janeLon = GARAGE_JANE.getCoordinate().getY();

        double johnLat = GARAGE_JOHN.getCoordinate().getX();
        double johnLon = GARAGE_JOHN.getCoordinate().getY();

        double joeyLat = GARAGE_JOEY.getCoordinate().getX();
        double joeyLon = GARAGE_JOEY.getCoordinate().getY();

        // ----- JANE'S VEHICLES -----
        TRACTOR_JANE = createVehicle(
            "Tractor", TOOL_CATEGORY_TRACTOR_JANE,
            "A powerful tractor for farm work.",
            10000.0, null,
            GARAGE_JANE, JANE_DOE,
            new Coordinate(janeLat + 0.00030, janeLon + 0.00010),
            true, 100.0, 0.0
        );

        LAWN_MOWER_JANE = createVehicle(
            "Lawn Mower", TOOL_CATEGORY_LAWN_MOWER_JANE,
            "A mower for cutting grass in large areas.",
            500.0, null,
            GARAGE_JANE, JANE_DOE,
            new Coordinate(janeLat - 0.00030, janeLon + 0.00005),
            true, 10.0, 0.0
        );

        GENERATOR_JANE = createVehicle(
            "Generator", TOOL_CATEGORY_GENERATOR_JANE,
            "A diesel-powered generator for backup power.",
            3000.0, null,
            GARAGE_JANE, JANE_DOE,
            new Coordinate(janeLat, janeLon - 0.00030),
            true, 75.0, 0.0
        );

        // ----- JOHN'S VEHICLES -----
        TRACTOR_JOHN = createVehicle(
            "Tractor", TOOL_CATEGORY_TRACTOR_JOHN,
            "A powerful tractor for farm work.",
            10000.0, null,
            GARAGE_JOHN, JOHN_DOE,
            new Coordinate(johnLat + 0.00033, johnLon + 0.00030),
            true, 100.0, 0.0
        );

        LAWN_MOWER_JOHN = createVehicle(
            "Lawn Mower", TOOL_CATEGORY_LAWN_MOWER_JOHN,
            "A mower for cutting grass in large areas.",
            500.0, null,
            GARAGE_JOHN, JOHN_DOE,
            new Coordinate(johnLat - 0.00031, johnLon + 0.00045),
            true, 10.0, 0.0
        );

        GENERATOR_JOHN = createVehicle(
            "Generator", TOOL_CATEGORY_GENERATOR_JOHN,
            "A diesel-powered generator for backup power.",
            3000.0, null,
            GARAGE_JOHN, JOHN_DOE,
            new Coordinate(johnLat, johnLon - 0.00035),
            true, 15.0, 0.0
        );

        // ----- JOEY'S VEHICLES -----
        TRACTOR_JOEY = createVehicle(
            "Tractor", TOOL_CATEGORY_TRACTOR_JOEY,
            "A powerful tractor for farm work.",
            10000.0, null,
            GARAGE_JOEY, JOEY_DOE,
            new Coordinate(joeyLat + 0.00033, joeyLon + 0.00030),
            true, 100.0, 0.0
        );

        LAWN_MOWER_JOEY = createVehicle(
            "Lawn Mower", TOOL_CATEGORY_LAWN_MOWER_JOEY,
            "A mower for cutting grass in large areas.",
            500.0, null,
            GARAGE_JOEY, JOEY_DOE,
            new Coordinate(joeyLat - 0.00031, joeyLon + 0.00045),
            true, 10.0, 0.0
        );

        GENERATOR_JOEY = createVehicle(
            "Generator", TOOL_CATEGORY_GENERATOR_JOEY,
            "A diesel-powered generator for backup power.",
            3000.0, null,
            GARAGE_JOEY, JOEY_DOE,
            new Coordinate(joeyLat, joeyLon - 0.00035),
            true, 15.0, 0.0
        );


        createJohnFleetVehicles();
        createJoeyFleetVehicles();
    }

    private List<Garage> JOEY_GARAGES = new ArrayList<>();

    private Garage joeyGarageForIndex(int idx) {
        return JOEY_GARAGES.get(idx % JOEY_GARAGES.size());
    }

    private Vehicle JOEY_TRACTOR_150HP;
    private Vehicle JOEY_SPRAYER;
    private Vehicle JOEY_COMBINE;
    private Vehicle JOEY_TELEHANDLER;

    private Tool JOEY_SEED_DRILL;
    private Tool JOEY_FERTILIZER_SPREADER;
    private Tool JOEY_DISC_HARROW;
    private Tool JOEY_PLOW;

    private void createJoeyTractorImplements() {
        record ImplementSpec(String name, String desc, double value) {
        }

        List<ImplementSpec> implementsList = List.of(
            new ImplementSpec("Moldboard Plow", "Classic moldboard plow for primary tillage.", 9000),
            new ImplementSpec("Chisel Plow", "Deep tillage implement for breaking hardpan.", 11000),
            new ImplementSpec("Disc Harrow", "Disc harrow for residue management and soil prep.", 8500),
            new ImplementSpec("Power Harrow", "Rotary harrow for fine seedbed preparation.", 14000),
            new ImplementSpec("Field Cultivator", "Cultivator for shallow tillage and weed control.", 7000),
            new ImplementSpec("Subsoiler", "Subsoiler for deep ripping and aeration.", 12000),
            new ImplementSpec("Seed Drill", "Seed drill for precise cereal seeding.", 18000),
            new ImplementSpec("Row Planter", "Planter for row crops with adjustable spacing.", 22000),
            new ImplementSpec("Fertilizer Spreader", "Broadcast spreader for granular fertilizer.", 9500),
            new ImplementSpec("Manure Spreader", "Trailer-mounted manure spreader.", 16000),
            new ImplementSpec("Boom Sprayer (Towed)", "Towed boom sprayer for pesticides/herbicides.", 20000),
            new ImplementSpec("Rotary Mower", "Rotary cutter for grass and light brush.", 6000),
            new ImplementSpec("Mower Conditioner", "Cuts and conditions hay for faster drying.", 17000),
            new ImplementSpec("Hay Rake", "Rake for windrowing hay.", 5000),
            new ImplementSpec("Tedder", "Tedder for spreading hay to dry evenly.", 5200),
            new ImplementSpec("Round Baler", "Produces round bales.", 26000),
            new ImplementSpec("Square Baler", "Produces square bales.", 28000),
            new ImplementSpec("Bale Wrapper", "Wraps bales for silage.", 21000),
            new ImplementSpec("Grain Cart", "High-capacity grain cart for harvest logistics.", 24000),
            new ImplementSpec("Flatbed Trailer (8t)", "General purpose flatbed trailer.", 9000),
            new ImplementSpec("Tipping Trailer (12t)", "Dump trailer for grain/soil transport.", 14000),
            new ImplementSpec("Front Loader Attachment", "Front loader for tractor (bucket).", 8000),
            new ImplementSpec("Pallet Fork Attachment", "Forks for pallets/bales.", 3500),
            new ImplementSpec("Stone Picker", "Collects stones from fields.", 19000),
            new ImplementSpec("Land Roller", "Roller for leveling seedbeds and pressing stones.", 12000)
        );

        for (int i = 0; i < implementsList.size(); i++) {
            ImplementSpec s = implementsList.get(i);
            Garage g = joeyGarageForIndex(i);

            Tool created = createTool(
                s.name(),
                TOOL_CATEGORY_TRACTOR_IMPLEMENT_JOEY,
                s.desc(),
                s.value(),
                null,
                g,
                JOEY_DOE,
                nearGarage(g, 200 + i),
                true
            );

            // keep references for scheduling bottlenecks
            if ("Seed Drill".equals(s.name())) {
                JOEY_SEED_DRILL = created;
            }
            if ("Fertilizer Spreader".equals(s.name())) {
                JOEY_FERTILIZER_SPREADER = created;
            }
            if ("Disc Harrow".equals(s.name())) {
                JOEY_DISC_HARROW = created;
            }
            if ("Moldboard Plow".equals(s.name())) {
                JOEY_PLOW = created;
            }
        }
    }

    private void createJoeyFleetVehicles() {
        record VehicleSpec(String name, ToolCategory category, String desc, double value, double fuel, double km) {
        }

        List<VehicleSpec> fleet = List.of(
            new VehicleSpec("Tractor 90HP", TOOL_CATEGORY_TRACTOR_JOEY, "Compact utility tractor (90HP).", 45000, 82, 1200),
            new VehicleSpec("Tractor 110HP", TOOL_CATEGORY_TRACTOR_JOEY, "General-purpose field tractor (110HP).", 52000, 74, 2400),
            new VehicleSpec("Tractor 130HP", TOOL_CATEGORY_TRACTOR_JOEY, "Mid-size tractor for tillage and hauling (130HP).", 64000, 68, 3800),
            new VehicleSpec("Tractor 150HP", TOOL_CATEGORY_TRACTOR_JOEY, "Row-crop tractor (150HP).", 78000, 61, 5100),
            new VehicleSpec("Tractor 170HP", TOOL_CATEGORY_TRACTOR_JOEY, "Heavy-duty tractor (170HP).", 91000, 56, 6600),
            new VehicleSpec("Tractor 190HP", TOOL_CATEGORY_TRACTOR_JOEY, "Heavy tillage tractor (190HP).", 105000, 48, 8200),
            new VehicleSpec("Tractor 210HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-power tractor (210HP).", 118000, 44, 9400),
            new VehicleSpec("Tractor 230HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-power tractor for big implements (230HP).", 132000, 51, 10300),
            new VehicleSpec("Tractor 250HP", TOOL_CATEGORY_TRACTOR_JOEY, "Premium tractor (250HP).", 148000, 59, 11000),
            new VehicleSpec("Tractor 270HP", TOOL_CATEGORY_TRACTOR_JOEY, "Premium tractor (270HP).", 162000, 63, 12500),
            new VehicleSpec("Tractor 290HP", TOOL_CATEGORY_TRACTOR_JOEY, "Articulated tractor (290HP).", 175000, 67, 14000),
            new VehicleSpec("Tractor 310HP", TOOL_CATEGORY_TRACTOR_JOEY, "Articulated tractor (310HP).", 189000, 53, 15500),
            new VehicleSpec("Tractor 330HP", TOOL_CATEGORY_TRACTOR_JOEY, "Articulated tractor (330HP).", 205000, 49, 17100),
            new VehicleSpec("Tractor 350HP", TOOL_CATEGORY_TRACTOR_JOEY, "Large-frame tractor (350HP).", 220000, 46, 18600),
            new VehicleSpec("Tractor 370HP", TOOL_CATEGORY_TRACTOR_JOEY, "Large-frame tractor (370HP).", 235000, 41, 20000),
            new VehicleSpec("Tractor 390HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-horsepower tractor (390HP).", 250000, 38, 21800),
            new VehicleSpec("Tractor 410HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-horsepower tractor (410HP).", 265000, 35, 23300),
            new VehicleSpec("Tractor 430HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-horsepower tractor (430HP).", 280000, 33, 24700),
            new VehicleSpec("Tractor 450HP", TOOL_CATEGORY_TRACTOR_JOEY, "High-horsepower tractor (450HP).", 295000, 30, 26500),
            new VehicleSpec("Tractor 480HP", TOOL_CATEGORY_TRACTOR_JOEY, "旗舰 high-horsepower tractor (480HP).", 320000, 28, 28700),

            new VehicleSpec("Combine Harvester", TOOL_CATEGORY_COMBINE_JOEY, "Self-propelled combine for grain harvesting.", 420000, 62, 3100),
            new VehicleSpec("Forage Harvester", TOOL_CATEGORY_FORAGE_HARVESTER_JOEY, "Self-propelled forage harvester for silage.", 380000, 58, 2800),
            new VehicleSpec("Self-Propelled Sprayer", TOOL_CATEGORY_SPRAYER_JOEY, "High-clearance sprayer for field application.", 290000, 66, 5200),
            new VehicleSpec("Telehandler", TOOL_CATEGORY_TELEHANDLER_JOEY, "Telehandler for loading bales/pallets.", 115000, 71, 8900),
            new VehicleSpec("Wheel Loader", TOOL_CATEGORY_TELEHANDLER_JOEY, "Wheel loader for yard work and material handling.", 140000, 64, 7600)
        );

        for (int i = 0; i < fleet.size(); i++) {
            VehicleSpec v = fleet.get(i);
            Garage g = joeyGarageForIndex(i);

            Vehicle created = createVehicle(
                v.name(), v.category(), v.desc(), v.value(),
                null, g, JOEY_DOE, nearGarage(g, 300 + i),
                true, v.fuel(), v.km()
            );

            if ("Tractor 150HP".equals(v.name())) {
                JOEY_TRACTOR_150HP = created;
            }
            if ("Self-Propelled Sprayer".equals(v.name())) {
                JOEY_SPRAYER = created;
            }
            if ("Combine Harvester".equals(v.name())) {
                JOEY_COMBINE = created;
            }
            if ("Telehandler".equals(v.name())) {
                JOEY_TELEHANDLER = created;
            }
        }
    }

    private void createJohnFleetVehicles() {
        record VehicleSpec(String name, ToolCategory category, String desc, double value, double fuel, double km) {
        }

        List<VehicleSpec> fleet = List.of(
            new VehicleSpec("Tractor 90HP", TOOL_CATEGORY_TRACTOR_JOHN, "Compact utility tractor (90HP).", 45000, 82, 1200),
            new VehicleSpec("Tractor 110HP", TOOL_CATEGORY_TRACTOR_JOHN, "General-purpose field tractor (110HP).", 52000, 74, 2400),
            new VehicleSpec("Tractor 130HP", TOOL_CATEGORY_TRACTOR_JOHN, "Mid-size tractor for tillage and hauling (130HP).", 64000, 68, 3800),
            new VehicleSpec("Tractor 150HP", TOOL_CATEGORY_TRACTOR_JOHN, "Row-crop tractor (150HP).", 78000, 61, 5100),
            new VehicleSpec("Tractor 170HP", TOOL_CATEGORY_TRACTOR_JOHN, "Heavy-duty tractor (170HP).", 91000, 56, 6600),
            new VehicleSpec("Tractor 190HP", TOOL_CATEGORY_TRACTOR_JOHN, "Heavy tillage tractor (190HP).", 105000, 48, 8200),
            new VehicleSpec("Tractor 210HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-power tractor (210HP).", 118000, 44, 9400),
            new VehicleSpec("Tractor 230HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-power tractor for big implements (230HP).", 132000, 51, 10300),
            new VehicleSpec("Tractor 250HP", TOOL_CATEGORY_TRACTOR_JOHN, "Premium tractor (250HP).", 148000, 59, 11000),
            new VehicleSpec("Tractor 270HP", TOOL_CATEGORY_TRACTOR_JOHN, "Premium tractor (270HP).", 162000, 63, 12500),
            new VehicleSpec("Tractor 290HP", TOOL_CATEGORY_TRACTOR_JOHN, "Articulated tractor (290HP).", 175000, 67, 14000),
            new VehicleSpec("Tractor 310HP", TOOL_CATEGORY_TRACTOR_JOHN, "Articulated tractor (310HP).", 189000, 53, 15500),
            new VehicleSpec("Tractor 330HP", TOOL_CATEGORY_TRACTOR_JOHN, "Articulated tractor (330HP).", 205000, 49, 17100),
            new VehicleSpec("Tractor 350HP", TOOL_CATEGORY_TRACTOR_JOHN, "Large-frame tractor (350HP).", 220000, 46, 18600),
            new VehicleSpec("Tractor 370HP", TOOL_CATEGORY_TRACTOR_JOHN, "Large-frame tractor (370HP).", 235000, 41, 20000),
            new VehicleSpec("Tractor 390HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-horsepower tractor (390HP).", 250000, 38, 21800),
            new VehicleSpec("Tractor 410HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-horsepower tractor (410HP).", 265000, 35, 23300),
            new VehicleSpec("Tractor 430HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-horsepower tractor (430HP).", 280000, 33, 24700),
            new VehicleSpec("Tractor 450HP", TOOL_CATEGORY_TRACTOR_JOHN, "High-horsepower tractor (450HP).", 295000, 30, 26500),
            new VehicleSpec("Tractor 480HP", TOOL_CATEGORY_TRACTOR_JOHN, "旗舰 high-horsepower tractor (480HP).", 320000, 28, 28700),

            new VehicleSpec("Combine Harvester", TOOL_CATEGORY_COMBINE_JOHN, "Self-propelled combine for grain harvesting.", 420000, 62, 3100),
            new VehicleSpec("Forage Harvester", TOOL_CATEGORY_FORAGE_HARVESTER_JOHN, "Self-propelled forage harvester for silage.", 380000, 58, 2800),
            new VehicleSpec("Self-Propelled Sprayer", TOOL_CATEGORY_SPRAYER_JOHN, "High-clearance sprayer for field application.", 290000, 66, 5200),
            new VehicleSpec("Telehandler", TOOL_CATEGORY_TELEHANDLER_JOHN, "Telehandler for loading bales/pallets.", 115000, 71, 8900),
            new VehicleSpec("Wheel Loader", TOOL_CATEGORY_TELEHANDLER_JOHN, "Wheel loader for yard work and material handling.", 140000, 64, 7600)
        );

        for (int i = 0; i < fleet.size(); i++) {
            Garage g = johnGarageForIndex(i);
            VehicleSpec v = fleet.get(i);

            createVehicle(
                v.name(),
                v.category(),
                v.desc(),
                v.value(),
                null,
                g,
                JOHN_DOE,
                nearGarage(g, 100 + i),
                true,
                v.fuel(),
                v.km()
            );
        }
    }

    private Vehicle createVehicle(String name, ToolCategory category, String description, double remainingValue, byte[] image, Garage garage, Farmer farmer, Coordinate coordinate, boolean isUsable, double fuelLevel, double drivenKm) {
        return vehicleRepository.save(new Vehicle(name, category, description, remainingValue, image, garage, farmer, coordinate, isUsable, fuelLevel, drivenKm));
    }


    private Field FIELD_1_JANE, FIELD_2_JANE, FIELD_3_JANE, FIELD_4_JANE, FIELD_5_JANE, FIELD_6_JANE;
    private Field FIELD_1_JOHN, FIELD_2_JOHN, FIELD_3_JOHN, FIELD_4_JOHN, FIELD_5_JOHN, FIELD_6_JOHN,
        FIELD_7_JOHN, FIELD_8_JOHN, FIELD_9_JOHN, FIELD_10_JOHN, FIELD_11_JOHN, FIELD_12_JOHN, FIELD_13_JOHN;
    private Field FIELD_1_JOEY, FIELD_2_JOEY, FIELD_3_JOEY, FIELD_4_JOEY, FIELD_5_JOEY, FIELD_6_JOEY,
        FIELD_7_JOEY, FIELD_8_JOEY, FIELD_9_JOEY, FIELD_10_JOEY, FIELD_11_JOEY, FIELD_12_JOEY, FIELD_13_JOEY;

    private void createFields() {
        double baseLatJane = 48.312000;
        double baseLatJohn = 48.30100;
        double baseLon = 15.970889;


        double halfHeightLat = 0.0010;
        double halfWidthLon = 0.0015;


        double lonStep = 0.0040;

        FIELD_1_JANE = createField(
            JANE_DOE,
            "Jane Field 1",
            parseWktPolygon(
                "POLYGON ((15.967726487949967 48.31075819088513, 15.971878698595322 48.31145747912546, 15.96966826473873 48.3149323714104,"
                    + " 15.965655863926486 48.31343398692032, 15.966429557397374 48.31224951851757, 15.967726487949967 48.31075819088513))")
        );

        FIELD_2_JANE = createField(
            JANE_DOE,
            "Jane Field 2",
            parseWktPolygon(
                "POLYGON ((15.97194313005225 48.31146818244245, 15.976395659733685 48.31222454449021, 15.975515877359571 48.313976264483216,"
                    + " 15.975097448477108 48.31430448231355, 15.974689756982986 48.31466123843011, 15.974228404204005 48.31496091164029, 15.973691953848746 48.315342635680466,"
                    + " 15.973004844379705 48.31628087982933, 15.96971688587952 48.31496091164029, 15.97194313005225 48.31146818244245))")
        );

        FIELD_3_JANE = createField(
            JANE_DOE,
            "Jane Field 3",
            parseWktPolygon(
                "POLYGON ((15.969829562176933 48.308046574434215, 15.980118508831163 48.308524685692056, 15.978616550178714 48.31040854317134, "
                    + "15.977307558059694 48.31035859330239, 15.976524355878105 48.31217102867614, 15.967833987266815 48.31070110570774, 15.969829562176933 48.308046574434215))")
        );

        // ---- JOHN: 3 adjacent fields in a southern row, parallel to Jane's ----
        FIELD_1_JOHN = createField(
            JOHN_DOE,
            "John Field 1",
            parseWktPolygon("POLYGON ((15.976197123527529 48.30039618513173, 15.977007150650026 48.29713799864962, 15.975950360298159 48.29712729232718, "
                + "15.974872112274172 48.29700952263213, 15.97368657588959 48.29668476236862, 15.972779989242555 48.29649918414768, 15.972281098365785 48.298686822973075, 15.976197123527529 48.30039618513173))"
            ));

        FIELD_2_JOHN = createField(
            JOHN_DOE,
            "John Field 2",
            parseWktPolygon("POLYGON ((15.976202487945558 48.300446144751454, 15.972281098365785 48.29874392158922, 15.971771478652956 48.30096714930069, "
                + "15.975794792175295 48.30272282410871, 15.976202487945558 48.300446144751454))")
        );

        FIELD_3_JOHN = createField(
            JOHN_DOE,
            "John Field 3",
            parseWktPolygon("POLYGON ((15.972699522972109 48.29649561532912, 15.971953868865969 48.299721725479756, 15.971739292144777 48.3006067289123,"
                + " 15.97166419029236 48.30094216975186, 15.968263149261476 48.29945408029104, 15.96890151500702 48.29710587967555, 15.968751311302187 48.29700595384925, "
                + "15.968826413154604 48.29662766144939, 15.970258712768556 48.29654914758122, 15.971862673759462 48.296492046510316, 15.972699522972109 48.29649561532912))"
            )
        );

        FIELD_4_JOHN = createField(
            JOHN_DOE,
            "John Field 4",
            parseWktPolygon("POLYGON ((15.976572632789614 48.300078874991996, 15.977554321289062 48.300128819289306,"
                + " 15.978240966796877 48.297267645765146, 15.977768898010256 48.29725337517113, 15.977221727371218"
                + " 48.29727004310847, 15.976572632789614 48.300078874991996))"
            )
        );

        FIELD_5_JOHN = createField(
            JOHN_DOE,
            "John Field 5",
            parseWktPolygon("POLYGON ((15.979624986648561 48.29678183583834, 15.980113148689272 48.296917407572685,"
                + " 15.980595946311952 48.29708865556462, 15.98088562488556 48.296078997655286, 15.979844927787783"
                + " 48.29609947045225, 15.979624986648561 48.29678183583834))"
            )
        );

        FIELD_6_JOHN = createField(
            JOHN_DOE,
            "John Field 6",
            parseWktPolygon("POLYGON ((15.977575778961183 48.30012835253364, 15.979211926460266 48.300206836331434,"
                + " 15.979861021041872 48.297512724104095, 15.979533791542055 48.297413402091955, 15.97914218902588"
                + " 48.29734918457077, 15.978804230690004 48.29729566990809, 15.978536009788515 48.29727069637961,"
                + " 15.978257060050966 48.29726712873171, 15.977575778961183 48.30012835253364),"
                + " (15.977876186370851 48.30001078517403, 15.979088544845583 48.300096861876554, 15.979442596435549"
                + " 48.29838358377162, 15.978283882141115 48.29830507930384, 15.977876186370851 48.30001078517403))"
            )
        );

        FIELD_7_JOHN = createField(
            JOHN_DOE,
            "John Field 7",
            parseWktPolygon("POLYGON ((15.979238748550417 48.30020729303659, 15.9796679019928 48.30023226512854,"
                + " 15.980290174484255 48.29770186960878, 15.980113148689272 48.297609080197795, 15.979882478713991"
                + " 48.29751629396826, 15.979238748550417 48.30020729303659))")
        );

        FIELD_8_JOHN = createField(
            JOHN_DOE,
            "John Field 8",
            parseWktPolygon("POLYGON ((15.975987911224367 48.302690732096586, 15.97992539405823 48.30289415088601,"
                + " 15.98039746284485 48.301106924939674, 15.97639560699463 48.30090358389127, 15.975987911224367"
                + " 48.302690732096586))")
        );

        FIELD_9_JOHN = createField(
            JOHN_DOE,
            "John Field 9",
            parseWktPolygon("POLYGON ((15.97640097141266 48.300878144262285, 15.98040282726288 48.30107791803042,"
                + " 15.980579853057863 48.30031092519511, 15.976567268371584 48.300139688011726, 15.97640097141266"
                + " 48.300878144262285))")
        );

        FIELD_10_JOHN = createField(
            JOHN_DOE,
            "John Field 10",
            parseWktPolygon("POLYGON ((15.979689359664919 48.30022502639903, 15.980982184410097 48.30027853799108,"
                + " 15.981470346450807 48.298191544335545, 15.980316996574404 48.29769116452129, 15.979689359664919"
                + " 48.30022502639903))")
        );

        FIELD_11_JOHN = createField(
            JOHN_DOE,
            "John Field 11",
            parseWktPolygon("POLYGON ((15.961085557937624 48.297720685917064, 15.968070030212404 48.299354628056896,"
                + " 15.968617200851442 48.29706663317462, 15.965242981910707 48.29656699723636, 15.963327884674074"
                + " 48.29657413374651, 15.961385965347292 48.29662053276377, 15.961149930953981 48.29663840249483,"
                + " 15.961123108863832 48.296888172707206, 15.961284041404726 48.29697729302303, 15.961085557937624"
                + " 48.297720685917064))")
        );

        FIELD_12_JOHN = createField(
            JOHN_DOE,
            "John Field 12",
            parseWktPolygon("POLYGON ((15.963231325149538 48.30183072047195, 15.96717953681946 48.30274781060222,"
                + " 15.967968106269838 48.29948262024479, 15.964014530181887 48.29854407056988, 15.963231325149538"
                + " 48.30183072047195))")
        );

        FIELD_13_JOHN = createField(
            JOHN_DOE,
            "John Field 13",
            parseWktPolygon("POLYGON ((15.9801721572876 48.302972654996175, 15.983809232711794 48.30314031543891,"
                + " 15.984833836555483 48.300518328459916, 15.980821357555143 48.30033237008281, 15.9801721572876"
                + " 48.302972654996175))")
        );


        FIELD_1_JOEY = createField(
            JOEY_DOE,
            "Joey Field 1",
            parseWktPolygon("POLYGON ((15.976197123527529 48.30039618513173, 15.977007150650026 48.29713799864962, 15.975950360298159 48.29712729232718, "
                + "15.974872112274172 48.29700952263213, 15.97368657588959 48.29668476236862, 15.972779989242555 48.29649918414768, 15.972281098365785 48.298686822973075, 15.976197123527529 48.30039618513173))"
            ));

        FIELD_2_JOEY = createField(
            JOEY_DOE,
            "Joey Field 2",
            parseWktPolygon("POLYGON ((15.976202487945558 48.300446144751454, 15.972281098365785 48.29874392158922, 15.971771478652956 48.30096714930069, "
                + "15.975794792175295 48.30272282410871, 15.976202487945558 48.300446144751454))")
        );

        FIELD_3_JOEY = createField(
            JOEY_DOE,
            "Joey Field 3",
            parseWktPolygon("POLYGON ((15.972699522972109 48.29649561532912, 15.971953868865969 48.299721725479756, 15.971739292144777 48.3006067289123,"
                + " 15.97166419029236 48.30094216975186, 15.968263149261476 48.29945408029104, 15.96890151500702 48.29710587967555, 15.968751311302187 48.29700595384925, "
                + "15.968826413154604 48.29662766144939, 15.970258712768556 48.29654914758122, 15.971862673759462 48.296492046510316, 15.972699522972109 48.29649561532912))"
            )
        );

        FIELD_4_JOEY = createField(
            JOEY_DOE,
            "Joey Field 4",
            parseWktPolygon("POLYGON ((15.976572632789614 48.300078874991996, 15.977554321289062 48.300128819289306,"
                + " 15.978240966796877 48.297267645765146, 15.977768898010256 48.29725337517113, 15.977221727371218"
                + " 48.29727004310847, 15.976572632789614 48.300078874991996))"
            )
        );

        FIELD_5_JOEY = createField(
            JOEY_DOE,
            "Joey Field 5",
            parseWktPolygon("POLYGON ((15.979624986648561 48.29678183583834, 15.980113148689272 48.296917407572685,"
                + " 15.980595946311952 48.29708865556462, 15.98088562488556 48.296078997655286, 15.979844927787783"
                + " 48.29609947045225, 15.979624986648561 48.29678183583834))"
            )
        );

        FIELD_6_JOEY = createField(
            JOEY_DOE,
            "Joey Field 6",
            parseWktPolygon("POLYGON ((15.977575778961183 48.30012835253364, 15.979211926460266 48.300206836331434,"
                + " 15.979861021041872 48.297512724104095, 15.979533791542055 48.297413402091955, 15.97914218902588"
                + " 48.29734918457077, 15.978804230690004 48.29729566990809, 15.978536009788515 48.29727069637961,"
                + " 15.978257060050966 48.29726712873171, 15.977575778961183 48.30012835253364),"
                + " (15.977876186370851 48.30001078517403, 15.979088544845583 48.300096861876554, 15.979442596435549"
                + " 48.29838358377162, 15.978283882141115 48.29830507930384, 15.977876186370851 48.30001078517403))"
            )
        );

        FIELD_7_JOEY = createField(
            JOEY_DOE,
            "Joey Field 7",
            parseWktPolygon("POLYGON ((15.979238748550417 48.30020729303659, 15.9796679019928 48.30023226512854,"
                + " 15.980290174484255 48.29770186960878, 15.980113148689272 48.297609080197795, 15.979882478713991"
                + " 48.29751629396826, 15.979238748550417 48.30020729303659))")
        );

        FIELD_8_JOEY = createField(
            JOEY_DOE,
            "Joey Field 8",
            parseWktPolygon("POLYGON ((15.975987911224367 48.302690732096586, 15.97992539405823 48.30289415088601,"
                + " 15.98039746284485 48.301106924939674, 15.97639560699463 48.30090358389127, 15.975987911224367"
                + " 48.302690732096586))")
        );

        FIELD_9_JOEY = createField(
            JOEY_DOE,
            "Joey Field 9",
            parseWktPolygon("POLYGON ((15.97640097141266 48.300878144262285, 15.98040282726288 48.30107791803042,"
                + " 15.980579853057863 48.30031092519511, 15.976567268371584 48.300139688011726, 15.97640097141266"
                + " 48.300878144262285))")
        );

        FIELD_10_JOEY = createField(
            JOEY_DOE,
            "Joey Field 10",
            parseWktPolygon("POLYGON ((15.979689359664919 48.30022502639903, 15.980982184410097 48.30027853799108,"
                + " 15.981470346450807 48.298191544335545, 15.980316996574404 48.29769116452129, 15.979689359664919"
                + " 48.30022502639903))")
        );

        FIELD_11_JOEY = createField(
            JOEY_DOE,
            "Joey Field 11",
            parseWktPolygon("POLYGON ((15.961085557937624 48.297720685917064, 15.968070030212404 48.299354628056896,"
                + " 15.968617200851442 48.29706663317462, 15.965242981910707 48.29656699723636, 15.963327884674074"
                + " 48.29657413374651, 15.961385965347292 48.29662053276377, 15.961149930953981 48.29663840249483,"
                + " 15.961123108863832 48.296888172707206, 15.961284041404726 48.29697729302303, 15.961085557937624"
                + " 48.297720685917064))")
        );

        FIELD_12_JOEY = createField(
            JOEY_DOE,
            "Joey Field 12",
            parseWktPolygon("POLYGON ((15.963231325149538 48.30183072047195, 15.96717953681946 48.30274781060222,"
                + " 15.967968106269838 48.29948262024479, 15.964014530181887 48.29854407056988, 15.963231325149538"
                + " 48.30183072047195))")
        );

        FIELD_13_JOEY = createField(
            JOEY_DOE,
            "Joey Field 13",
            parseWktPolygon("POLYGON ((15.9801721572876 48.302972654996175, 15.983809232711794 48.30314031543891,"
                + " 15.984833836555483 48.300518328459916, 15.980821357555143 48.30033237008281, 15.9801721572876"
                + " 48.302972654996175))")
        );

    }

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326); // WGS84

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

    private Polygon parseWktPolygon(String wkt) {
        try {
            WKTReader reader = new WKTReader(GEOMETRY_FACTORY);
            return (Polygon) reader.read(wkt);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse WKT: {}", wkt, e);
            throw new RuntimeException("Invalid WKT format", e);
        }
    }

    private Polygon parseWktPolygonLatLon(String wkt) {
        try {
            WKTReader reader = new WKTReader(GEOMETRY_FACTORY);
            Polygon polygon = (Polygon) reader.read(wkt);

            Coordinate[] coords = polygon.getCoordinates();
            Coordinate[] swapped = new Coordinate[coords.length];

            for (int i = 0; i < coords.length; i++) {
                swapped[i] = new Coordinate(coords[i].y, coords[i].x);
            }

            return GEOMETRY_FACTORY.createPolygon(swapped);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse WKT: {}", wkt, e);
            throw new RuntimeException("Invalid WKT format", e);
        }
    }

    private Field createField(Farmer owner, String name, Polygon boundaries) {
        return fieldRepository.save(new Field(owner, name, boundaries));
    }

    private Seed SEED_CORN_JANE, SEED_WHEAT_JANE, SEED_SOYBEAN_JANE, SEED_RICE_JANE;
    private Seed SEED_BARLEY_JANE, SEED_OATS_JANE, SEED_COTTON_JANE, SEED_POTATO_JANE, SEED_TOMATO_JANE;

    private Seed SEED_CORN_JOHN, SEED_WHEAT_JOHN, SEED_SOYBEAN_JOHN, SEED_RICE_JOHN;
    private Seed SEED_BARLEY_JOHN, SEED_OATS_JOHN, SEED_COTTON_JOHN, SEED_POTATO_JOHN, SEED_TOMATO_JOHN;

    private Seed SEED_CORN_JOEY, SEED_WHEAT_JOEY, SEED_SOYBEAN_JOEY, SEED_RICE_JOEY;
    private Seed SEED_BARLEY_JOEY, SEED_OATS_JOEY, SEED_COTTON_JOEY, SEED_POTATO_JOEY, SEED_TOMATO_JOEY;


    private void createSeeds() {
        LocalDateTime now = LocalDateTime.now();

        SEED_CORN_JANE = createSeed(CROP_TYPE_CORN_JANE, "Corn Seed", 15.0, now.minusDays(10), JANE_DOE);
        SEED_WHEAT_JANE = createSeed(CROP_TYPE_WHEAT_JANE, "Wheat Seed", 12.0, now.minusDays(12), JANE_DOE);
        SEED_SOYBEAN_JANE = createSeed(CROP_TYPE_SOYBEAN_JANE, "Soybean Seed", 18.0, now.minusDays(8), JANE_DOE);
        SEED_RICE_JANE = createSeed(CROP_TYPE_RICE_JANE, "Rice Seed", 20.0, now.minusDays(20), JANE_DOE);
        SEED_BARLEY_JANE = createSeed(CROP_TYPE_BARLEY_JANE, "Barley Seed", 14.0, now.minusDays(15), JANE_DOE);
        SEED_OATS_JANE = createSeed(CROP_TYPE_OATS_JANE, "Oats Seed", 13.0, now.minusDays(13), JANE_DOE);
        SEED_COTTON_JANE = createSeed(CROP_TYPE_COTTON_JANE, "Cotton Seed", 25.0, now.minusDays(6), JANE_DOE);
        SEED_POTATO_JANE = createSeed(CROP_TYPE_POTATO_JANE, "Potato Seed", 10.0, now.minusDays(5), JANE_DOE);
        SEED_TOMATO_JANE = createSeed(CROP_TYPE_TOMATO_JANE, "Tomato Seed", 8.0, now.minusDays(4), JANE_DOE);

        SEED_CORN_JOHN = createSeed(CROP_TYPE_CORN_JOHN, "Corn Seed", 15.0, now.minusDays(11), JOHN_DOE);
        SEED_WHEAT_JOHN = createSeed(CROP_TYPE_WHEAT_JOHN, "Wheat Seed", 12.0, now.minusDays(9), JOHN_DOE);
        SEED_SOYBEAN_JOHN = createSeed(CROP_TYPE_SOYBEAN_JOHN, "Soybean Seed", 18.0, now.minusDays(7), JOHN_DOE);
        SEED_RICE_JOHN = createSeed(CROP_TYPE_RICE_JOHN, "Rice Seed", 20.0, now.minusDays(19), JOHN_DOE);
        SEED_BARLEY_JOHN = createSeed(CROP_TYPE_BARLEY_JOHN, "Barley Seed", 14.0, now.minusDays(17), JOHN_DOE);
        SEED_OATS_JOHN = createSeed(CROP_TYPE_OATS_JOHN, "Oats Seed", 13.0, now.minusDays(16), JOHN_DOE);
        SEED_COTTON_JOHN = createSeed(CROP_TYPE_COTTON_JOHN, "Cotton Seed", 25.0, now.minusDays(6), JOHN_DOE);
        SEED_POTATO_JOHN = createSeed(CROP_TYPE_POTATO_JOHN, "Potato Seed", 10.0, now.minusDays(5), JOHN_DOE);
        SEED_TOMATO_JOHN = createSeed(CROP_TYPE_TOMATO_JOHN, "Tomato Seed", 8.0, now.minusDays(3), JOHN_DOE);

        SEED_CORN_JOEY = createSeed(CROP_TYPE_CORN_JOEY, "Corn Seed", 15.0, now.minusDays(11), JOEY_DOE);
        SEED_WHEAT_JOEY = createSeed(CROP_TYPE_WHEAT_JOEY, "Wheat Seed", 12.0, now.minusDays(9), JOEY_DOE);
        SEED_SOYBEAN_JOEY = createSeed(CROP_TYPE_SOYBEAN_JOEY, "Soybean Seed", 18.0, now.minusDays(7), JOEY_DOE);
        SEED_RICE_JOEY = createSeed(CROP_TYPE_RICE_JOEY, "Rice Seed", 20.0, now.minusDays(19), JOEY_DOE);
        SEED_BARLEY_JOEY = createSeed(CROP_TYPE_BARLEY_JOEY, "Barley Seed", 14.0, now.minusDays(17), JOEY_DOE);
        SEED_OATS_JOEY = createSeed(CROP_TYPE_OATS_JOEY, "Oats Seed", 13.0, now.minusDays(16), JOEY_DOE);
        SEED_COTTON_JOEY = createSeed(CROP_TYPE_COTTON_JOEY, "Cotton Seed", 25.0, now.minusDays(6), JOEY_DOE);
        SEED_POTATO_JOEY = createSeed(CROP_TYPE_POTATO_JOEY, "Potato Seed", 10.0, now.minusDays(5), JOEY_DOE);
        SEED_TOMATO_JOEY = createSeed(CROP_TYPE_TOMATO_JOEY, "Tomato Seed", 8.0, now.minusDays(3), JOEY_DOE);
    }


    private Seed createSeed(CropType type, String name, double buyingPrice, LocalDateTime buyingTime, Farmer owner) {
        return seedRepository.save(new Seed(type, name, buyingPrice, buyingTime, owner));
    }

    private ActiveField ACTIVE_FIELD_1_JANE;
    private ActiveField ACTIVE_FIELD_2_JANE;
    private ActiveField ACTIVE_FIELD_3_JANE;
    private ActiveField ACTIVE_FIELD_4_JANE;
    private ActiveField ACTIVE_FIELD_5_JANE_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_6_JANE_IN_PROGRESS;

    private ActiveField ACTIVE_FIELD_1_JOHN;
    private ActiveField ACTIVE_FIELD_2_JOHN;
    private ActiveField ACTIVE_FIELD_3_JOHN;
    private ActiveField ACTIVE_FIELD_4_JOHN;
    private ActiveField ACTIVE_FIELD_5_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_6_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_7_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_8_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_9_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_10_JOHN_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_11_JOHN_IN_PROGRESS;

    private ActiveField ACTIVE_FIELD_1_JOEY;
    private ActiveField ACTIVE_FIELD_2_JOEY;
    private ActiveField ACTIVE_FIELD_3_JOEY;
    private ActiveField ACTIVE_FIELD_4_JOEY;
    private ActiveField ACTIVE_FIELD_5_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_6_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_7_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_8_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_9_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_10_JOEY_IN_PROGRESS;
    private ActiveField ACTIVE_FIELD_11_JOEY_IN_PROGRESS;


    private void createActiveFields() {
        LocalDateTime now = LocalDateTime.now();


        ACTIVE_FIELD_1_JANE = createActiveField(
            JANE_DOE,
            FIELD_1_JANE,
            SEED_CORN_JANE,
            now.minusDays(150),
            now.minusDays(60),
            null,
            25
        );

        ACTIVE_FIELD_2_JANE = createActiveField(
            JANE_DOE,
            FIELD_2_JANE,
            SEED_WHEAT_JANE,
            now.minusDays(130),
            now.minusDays(40),
            null,
            30
        );

        ACTIVE_FIELD_3_JANE = createActiveField(
            JANE_DOE,
            FIELD_3_JANE,
            SEED_SOYBEAN_JANE,
            now.minusDays(110),
            now.minusDays(20),
            null,
            50
        );

        ACTIVE_FIELD_4_JANE = createActiveField(
            JANE_DOE,
            FIELD_1_JANE,
            SEED_BARLEY_JANE,
            now.minusDays(90),
            now.minusDays(10),
            null,
            35
        );


        ACTIVE_FIELD_5_JANE_IN_PROGRESS = createActiveField(
            JANE_DOE,
            FIELD_2_JANE,
            SEED_POTATO_JANE,
            now.minusDays(20),
            null,
            null,
            12
        );

        ACTIVE_FIELD_6_JANE_IN_PROGRESS = createActiveField(
            JANE_DOE,
            FIELD_3_JANE,
            SEED_TOMATO_JANE,
            now.minusDays(5),
            null,
            null,
            45
        );

        // --- JOHN: 4 completed ---
        ACTIVE_FIELD_1_JOHN = createActiveField(
            JOHN_DOE,
            FIELD_1_JOHN,
            SEED_CORN_JOHN,
            now.minusDays(160),
            now.minusDays(70),
            null,
            30
        );

        ACTIVE_FIELD_2_JOHN = createActiveField(
            JOHN_DOE,
            FIELD_2_JOHN,
            SEED_WHEAT_JOHN,
            now.minusDays(140),
            now.minusDays(50),
            null,
            12
        );

        ACTIVE_FIELD_4_JOHN = createActiveField(
            JOHN_DOE,
            FIELD_1_JOHN,
            SEED_OATS_JOHN,
            now.minusDays(100),
            now.minusDays(15),
            null,
            76
        );

        // --- JOHN: 2 in progress ---
        ACTIVE_FIELD_5_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_2_JOHN,
            SEED_COTTON_JOHN,
            now.minusDays(25),
            null,
            null,
            60
        );

        ACTIVE_FIELD_6_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_3_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(7),
            null,
            now.plusDays(23),
            75
        );

        ACTIVE_FIELD_7_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_4_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(2),
            null,
            null,
            75
        );

        ACTIVE_FIELD_8_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_5_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(5),
            null,
            null,
            75
        );

        ACTIVE_FIELD_9_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_8_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(3),
            null,
            null,
            75
        );

        ACTIVE_FIELD_10_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_11_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(15),
            null,
            now.plusDays(15),
            75
        );

        ACTIVE_FIELD_11_JOHN_IN_PROGRESS = createActiveField(
            JOHN_DOE,
            FIELD_12_JOHN,
            SEED_SOYBEAN_JOHN,
            now.minusDays(25),
            null,
            now.plusDays(5),
            75
        );

        // --- JOEY: 4 completed ---
        ACTIVE_FIELD_1_JOEY = createActiveField(
            JOEY_DOE,
            FIELD_1_JOEY,
            SEED_CORN_JOEY,
            now.minusDays(160),
            now.minusDays(70),
            null,
            30
        );

        ACTIVE_FIELD_2_JOEY = createActiveField(
            JOEY_DOE,
            FIELD_2_JOEY,
            SEED_WHEAT_JOEY,
            now.minusDays(140),
            now.minusDays(50),
            null,
            12
        );

        ACTIVE_FIELD_4_JOEY = createActiveField(
            JOEY_DOE,
            FIELD_1_JOEY,
            SEED_OATS_JOEY,
            now.minusDays(100),
            now.minusDays(15),
            null,
            76
        );

        // --- JOEY: 2 in progress ---
        ACTIVE_FIELD_5_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_2_JOEY,
            SEED_COTTON_JOEY,
            now.minusDays(25),
            null,
            null,
            60
        );

        ACTIVE_FIELD_6_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_3_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(7),
            null,
            now.plusDays(23),
            75
        );

        ACTIVE_FIELD_7_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_4_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(2),
            null,
            null,
            75
        );

        ACTIVE_FIELD_8_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_5_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(5),
            null,
            null,
            75
        );

        ACTIVE_FIELD_9_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_8_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(3),
            null,
            null,
            75
        );

        ACTIVE_FIELD_10_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_11_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(15),
            null,
            now.plusDays(15),
            75
        );

        ACTIVE_FIELD_11_JOEY_IN_PROGRESS = createActiveField(
            JOEY_DOE,
            FIELD_12_JOEY,
            SEED_SOYBEAN_JOEY,
            now.minusDays(25),
            null,
            now.plusDays(5),
            75
        );

    }


    private ActiveField createActiveField(Farmer owner, Field field, Seed seed, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime expectedHarvestDate, double sellingPrice) {
        if (expectedHarvestDate == null) {
            expectedHarvestDate = startDate.plusDays((long) seed.getType().getGrowthDuration());
        }
        return activeFieldRepository.save(new ActiveField(owner, field, seed, startDate, endDate, expectedHarvestDate, sellingPrice));
    }

    private Crop CROP_1_JANE;
    private Crop CROP_2_JANE;
    private Crop CROP_3_JANE;
    private Crop CROP_4_JANE;

    private Crop CROP_1_JOHN;
    private Crop CROP_2_JOHN;
    private Crop CROP_3_JOHN;
    private Crop CROP_4_JOHN;

    private Crop CROP_1_JOEY;
    private Crop CROP_2_JOEY;
    private Crop CROP_3_JOEY;
    private Crop CROP_4_JOEY;

    private void createCrops() {

        CROP_1_JANE = createCropForActiveField(
            ACTIVE_FIELD_1_JANE,
            180.0,
            12.0
        );

        CROP_2_JANE = createCropForActiveField(
            ACTIVE_FIELD_2_JANE,
            150.0,
            15.0
        );

        CROP_3_JANE = createCropForActiveField(
            ACTIVE_FIELD_3_JANE,
            200.0,
            10.0
        );

        CROP_4_JANE = createCropForActiveField(
            ACTIVE_FIELD_4_JANE,
            160.0,
            11.5
        );


        CROP_1_JOHN = createCropForActiveField(
            ACTIVE_FIELD_1_JOHN,
            175.0,
            13.0
        );

        CROP_2_JOHN = createCropForActiveField(
            ACTIVE_FIELD_2_JOHN,
            155.0,
            14.0
        );

        CROP_4_JOHN = createCropForActiveField(
            ACTIVE_FIELD_4_JOHN,
            165.0,
            12.0
        );

        CROP_1_JOEY = createCropForActiveField(
            ACTIVE_FIELD_1_JOEY,
            175.0,
            13.0
        );

        CROP_2_JOEY = createCropForActiveField(
            ACTIVE_FIELD_2_JOEY,
            155.0,
            14.0
        );

        CROP_4_JOEY = createCropForActiveField(
            ACTIVE_FIELD_4_JOEY,
            165.0,
            12.0
        );
    }

    private Crop createCropForActiveField(ActiveField activeField, double currentPrice, double quantity) {
        return createCrop(
            currentPrice,
            activeField.getSeed().getType(),
            quantity,
            activeField.getEndDate(),
            activeField.getOwner(),
            activeField.getField()
        );
    }

    private Crop createCrop(double currentPrice, CropType type, double quantity, LocalDateTime harvestDate, Farmer owner, Field field) {
        return cropRepository.save(new Crop(currentPrice, type, quantity, harvestDate, owner, field));
    }

    private void createFieldVisualizationData() {
        LocalDateTime now = LocalDateTime.now();
        List<Crop> crops = new ArrayList<>();

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOHN, 8.0, now.minusDays(365 + 10), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_SOYBEAN_JOHN, 11.0, now.minusDays(365 + 180), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_RICE_JOHN, 9.5, now.minusDays(730 + 30), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_BARLEY_JOHN, 14.0, now.minusDays(730 + 200), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_OATS_JOHN, 10.0, now.minusDays(1095 + 50), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_COTTON_JOHN, 16.5, now.minusDays(1095 + 230), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_POTATO_JOHN, 12.0, now.minusDays(1460 + 70), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_TOMATO_JOHN, 9.0, now.minusDays(1460 + 250), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_CORN_JOHN, 13.0, now.minusDays(1825 + 90), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            155.0, CROP_TYPE_WHEAT_JOHN, 10.5, now.minusDays(1825 + 270), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_SOYBEAN_JOHN, 15.0, now.minusDays(2190 + 110), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            225.0, CROP_TYPE_RICE_JOHN, 8.5, now.minusDays(2190 + 290), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_BARLEY_JOHN, 11.5, now.minusDays(2555 + 130), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_OATS_JOHN, 14.5, now.minusDays(2555 + 310), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_COTTON_JOHN, 10.0, now.minusDays(2920 + 150), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            215.0, CROP_TYPE_POTATO_JOHN, 17.0, now.minusDays(2920 + 330), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            200.0, CROP_TYPE_TOMATO_JOHN, 9.5, now.minusDays(3285 + 10), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_CORN_JOHN, 12.5, now.minusDays(3285 + 190), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_WHEAT_JOHN, 11.0, now.minusDays(3650 + 40), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_SOYBEAN_JOHN, 16.0, now.minusDays(3650 + 220), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_RICE_JOHN, 10.0, now.minusDays(4015 + 60), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_BARLEY_JOHN, 13.5, now.minusDays(4015 + 240), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_OATS_JOHN, 12.0, now.minusDays(4380 + 80), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_COTTON_JOHN, 15.5, now.minusDays(4380 + 260), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_POTATO_JOHN, 11.5, now.minusDays(4745 + 100), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_TOMATO_JOHN, 14.0, now.minusDays(4745 + 280), JOHN_DOE, FIELD_1_JOHN
        ));

        crops.add(new Crop(
            230.0, CROP_TYPE_RICE_JOHN, 24.5, now.minusDays(10), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_RICE_JOHN, 22.5, now.minusDays(365 + 310), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_SOYBEAN_JOHN, 22.0, now.minusDays(365 + 130), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_WHEAT_JOHN, 21.5, now.minusDays(730 + 290), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            222.0, CROP_TYPE_CORN_JOHN, 21.0, now.minusDays(730 + 110), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            219.0, CROP_TYPE_TOMATO_JOHN, 20.5, now.minusDays(1095 + 270), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            225.0, CROP_TYPE_POTATO_JOHN, 20.0, now.minusDays(1095 + 90), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_COTTON_JOHN, 19.5, now.minusDays(1460 + 250), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            215.0, CROP_TYPE_OATS_JOHN, 19.0, now.minusDays(1460 + 70), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_BARLEY_JOHN, 18.5, now.minusDays(1825 + 230), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_RICE_JOHN, 18.0, now.minusDays(1825 + 50), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            200.0, CROP_TYPE_SOYBEAN_JOHN, 17.5, now.minusDays(2190 + 210), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOHN, 17.0, now.minusDays(2190 + 30), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_CORN_JOHN, 16.5, now.minusDays(2555 + 190), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_TOMATO_JOHN, 16.0, now.minusDays(2555 + 10), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_POTATO_JOHN, 15.5, now.minusDays(2920 + 330), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_COTTON_JOHN, 15.0, now.minusDays(2920 + 150), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_OATS_JOHN, 14.5, now.minusDays(3285 + 310), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_BARLEY_JOHN, 14.0, now.minusDays(3285 + 130), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_RICE_JOHN, 13.5, now.minusDays(3650 + 290), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            155.0, CROP_TYPE_SOYBEAN_JOHN, 13.0, now.minusDays(3650 + 110), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            150.0, CROP_TYPE_WHEAT_JOHN, 12.5, now.minusDays(4015 + 270), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            145.0, CROP_TYPE_CORN_JOHN, 12.0, now.minusDays(4015 + 90), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            140.0, CROP_TYPE_TOMATO_JOHN, 11.5, now.minusDays(4380 + 250), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            135.0, CROP_TYPE_POTATO_JOHN, 11.0, now.minusDays(4380 + 70), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            130.0, CROP_TYPE_COTTON_JOHN, 10.5, now.minusDays(4745 + 230), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            125.0, CROP_TYPE_OATS_JOHN, 10.0, now.minusDays(4745 + 50), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            120.0, CROP_TYPE_BARLEY_JOHN, 9.5, now.minusDays(5110 + 210), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            115.0, CROP_TYPE_RICE_JOHN, 9.0, now.minusDays(5110 + 30), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            110.0, CROP_TYPE_SOYBEAN_JOHN, 8.5, now.minusDays(5475 + 190), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            105.0, CROP_TYPE_WHEAT_JOHN, 8.0, now.minusDays(5475 + 10), JOHN_DOE, FIELD_2_JOHN
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOHN, 24.0, now.minusDays(365 + 30), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOHN, 23.0, now.minusDays(365 + 200), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOHN, 22.0, now.minusDays(730 + 50), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOHN, 21.0, now.minusDays(730 + 220), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOHN, 20.0, now.minusDays(1095 + 70), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOHN, 19.0, now.minusDays(1095 + 240), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOHN, 17.0, now.minusDays(1460 + 90), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOHN, 16.0, now.minusDays(1460 + 260), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOHN, 15.0, now.minusDays(1825 + 110), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOHN, 14.0, now.minusDays(1825 + 280), JOHN_DOE, FIELD_3_JOHN
        ));

        crops.add(new Crop(
            92.0, CROP_TYPE_CORN_JOHN, 8.2, now.minusDays(0 + 30), JOHN_DOE, FIELD_4_JOHN
        ));
        crops.add(new Crop(
            88.0, CROP_TYPE_RICE_JOHN, 7.8, now.minusDays(0 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            105.0, CROP_TYPE_SOYBEAN_JOHN, 9.5, now.minusDays(365 + 30), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            101.0, CROP_TYPE_BARLEY_JOHN, 9.0, now.minusDays(365 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            76.0, CROP_TYPE_OATS_JOHN, 6.8, now.minusDays(730 + 30), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            72.0, CROP_TYPE_COTTON_JOHN, 6.4, now.minusDays(730 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            90.0, CROP_TYPE_POTATO_JOHN, 8.0, now.minusDays(1095 + 30), JOHN_DOE, FIELD_4_JOHN
        ));
        crops.add(new Crop(
            85.0, CROP_TYPE_TOMATO_JOHN, 7.6, now.minusDays(1095 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            108.0, CROP_TYPE_WHEAT_JOHN, 9.8, now.minusDays(1460 + 30), JOHN_DOE, FIELD_4_JOHN
        ));
        crops.add(new Crop(
            103.0, CROP_TYPE_CORN_JOHN, 9.2, now.minusDays(1460 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            74.0, CROP_TYPE_RICE_JOHN, 6.6, now.minusDays(1825 + 30), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            70.0, CROP_TYPE_SOYBEAN_JOHN, 6.2, now.minusDays(1825 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            93.0, CROP_TYPE_BARLEY_JOHN, 8.3, now.minusDays(2190 + 30), JOHN_DOE, FIELD_4_JOHN
        ));
        crops.add(new Crop(
            87.0, CROP_TYPE_OATS_JOHN, 7.7, now.minusDays(2190 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            106.0, CROP_TYPE_COTTON_JOHN, 9.6, now.minusDays(2555 + 30), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            100.0, CROP_TYPE_POTATO_JOHN, 9.1, now.minusDays(2555 + 200), JOHN_DOE, FIELD_4_JOHN
        ));

        crops.add(new Crop(
            28.0, CROP_TYPE_CORN_JOHN, 2.6, now.minusDays(0 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            34.0, CROP_TYPE_RICE_JOHN, 3.2, now.minusDays(365 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            22.0, CROP_TYPE_SOYBEAN_JOHN, 2.0, now.minusDays(730 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            27.0, CROP_TYPE_BARLEY_JOHN, 2.5, now.minusDays(1095 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            35.0, CROP_TYPE_OATS_JOHN, 3.3, now.minusDays(1460 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            21.0, CROP_TYPE_COTTON_JOHN, 1.9, now.minusDays(1825 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            26.0, CROP_TYPE_POTATO_JOHN, 2.4, now.minusDays(2190 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            33.0, CROP_TYPE_TOMATO_JOHN, 3.1, now.minusDays(2555 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            20.0, CROP_TYPE_WHEAT_JOHN, 1.8, now.minusDays(2920 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            27.5, CROP_TYPE_CORN_JOHN, 2.6, now.minusDays(3285 + 120), JOHN_DOE, FIELD_5_JOHN
        ));

        crops.add(new Crop(
            27.0, CROP_TYPE_CORN_JOHN, 2.6, now.minusDays(0 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            31.0, CROP_TYPE_RICE_JOHN, 3.0, now.minusDays(365 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            20.0, CROP_TYPE_SOYBEAN_JOHN, 1.9, now.minusDays(730 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            29.0, CROP_TYPE_BARLEY_JOHN, 2.7, now.minusDays(1095 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            23.0, CROP_TYPE_OATS_JOHN, 2.2, now.minusDays(1460 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            35.0, CROP_TYPE_COTTON_JOHN, 3.4, now.minusDays(1825 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            26.0, CROP_TYPE_POTATO_JOHN, 2.5, now.minusDays(2190 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            33.0, CROP_TYPE_TOMATO_JOHN, 3.1, now.minusDays(2555 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            21.0, CROP_TYPE_WHEAT_JOHN, 2.0, now.minusDays(2920 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            28.0, CROP_TYPE_CORN_JOHN, 2.7, now.minusDays(3285 + 120), JOHN_DOE, FIELD_7_JOHN
        ));

        crops.add(new Crop(
            54.0, CROP_TYPE_CORN_JOHN, 5.1, now.minusDays(0 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            63.0, CROP_TYPE_RICE_JOHN, 6.2, now.minusDays(365 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            38.0, CROP_TYPE_SOYBEAN_JOHN, 3.8, now.minusDays(730 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            61.0, CROP_TYPE_BARLEY_JOHN, 5.6, now.minusDays(1095 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            46.0, CROP_TYPE_OATS_JOHN, 4.4, now.minusDays(1460 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            72.0, CROP_TYPE_COTTON_JOHN, 6.9, now.minusDays(1825 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            52.0, CROP_TYPE_POTATO_JOHN, 5.0, now.minusDays(2190 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            67.0, CROP_TYPE_TOMATO_JOHN, 6.3, now.minusDays(2555 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            41.0, CROP_TYPE_WHEAT_JOHN, 3.9, now.minusDays(2920 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            58.0, CROP_TYPE_CORN_JOHN, 5.4, now.minusDays(3285 + 120), JOHN_DOE, FIELD_8_JOHN
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOHN, 24.0, now.minusDays(365 + 30), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOHN, 23.0, now.minusDays(365 + 200), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOHN, 22.0, now.minusDays(730 + 50), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOHN, 21.0, now.minusDays(730 + 220), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOHN, 20.0, now.minusDays(1095 + 70), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOHN, 19.0, now.minusDays(1095 + 240), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOHN, 17.0, now.minusDays(1460 + 90), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOHN, 16.0, now.minusDays(1460 + 260), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOHN, 15.0, now.minusDays(1825 + 110), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOHN, 14.0, now.minusDays(1825 + 280), JOHN_DOE, FIELD_11_JOHN
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOHN, 24.0, now.minusDays(365 + 30), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOHN, 23.0, now.minusDays(365 + 200), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOHN, 22.0, now.minusDays(730 + 50), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOHN, 21.0, now.minusDays(730 + 220), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOHN, 20.0, now.minusDays(1095 + 70), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOHN, 19.0, now.minusDays(1095 + 240), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOHN, 17.0, now.minusDays(1460 + 90), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOHN, 16.0, now.minusDays(1460 + 260), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOHN, 15.0, now.minusDays(1825 + 110), JOHN_DOE, FIELD_12_JOHN
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOHN, 14.0, now.minusDays(1825 + 280), JOHN_DOE, FIELD_12_JOHN
        ));


        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOEY, 8.0, now.minusDays(365 + 10), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_SOYBEAN_JOEY, 11.0, now.minusDays(365 + 180), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_RICE_JOEY, 9.5, now.minusDays(730 + 30), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_BARLEY_JOEY, 14.0, now.minusDays(730 + 200), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_OATS_JOEY, 10.0, now.minusDays(1095 + 50), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_COTTON_JOEY, 16.5, now.minusDays(1095 + 230), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_POTATO_JOEY, 12.0, now.minusDays(1460 + 70), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_TOMATO_JOEY, 9.0, now.minusDays(1460 + 250), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_CORN_JOEY, 13.0, now.minusDays(1825 + 90), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            155.0, CROP_TYPE_WHEAT_JOEY, 10.5, now.minusDays(1825 + 270), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_SOYBEAN_JOEY, 15.0, now.minusDays(2190 + 110), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            225.0, CROP_TYPE_RICE_JOEY, 8.5, now.minusDays(2190 + 290), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_BARLEY_JOEY, 11.5, now.minusDays(2555 + 130), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_OATS_JOEY, 14.5, now.minusDays(2555 + 310), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_COTTON_JOEY, 10.0, now.minusDays(2920 + 150), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            215.0, CROP_TYPE_POTATO_JOEY, 17.0, now.minusDays(2920 + 330), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            200.0, CROP_TYPE_TOMATO_JOEY, 9.5, now.minusDays(3285 + 10), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_CORN_JOEY, 12.5, now.minusDays(3285 + 190), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_WHEAT_JOEY, 11.0, now.minusDays(3650 + 40), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_SOYBEAN_JOEY, 16.0, now.minusDays(3650 + 220), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_RICE_JOEY, 10.0, now.minusDays(4015 + 60), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_BARLEY_JOEY, 13.5, now.minusDays(4015 + 240), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_OATS_JOEY, 12.0, now.minusDays(4380 + 80), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_COTTON_JOEY, 15.5, now.minusDays(4380 + 260), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_POTATO_JOEY, 11.5, now.minusDays(4745 + 100), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_TOMATO_JOEY, 14.0, now.minusDays(4745 + 280), JOEY_DOE, FIELD_1_JOEY
        ));

        crops.add(new Crop(
            230.0, CROP_TYPE_RICE_JOEY, 24.5, now.minusDays(10), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_RICE_JOEY, 22.5, now.minusDays(365 + 310), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_SOYBEAN_JOEY, 22.0, now.minusDays(365 + 130), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_WHEAT_JOEY, 21.5, now.minusDays(730 + 290), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            222.0, CROP_TYPE_CORN_JOEY, 21.0, now.minusDays(730 + 110), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            219.0, CROP_TYPE_TOMATO_JOEY, 20.5, now.minusDays(1095 + 270), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            225.0, CROP_TYPE_POTATO_JOEY, 20.0, now.minusDays(1095 + 90), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            220.0, CROP_TYPE_COTTON_JOEY, 19.5, now.minusDays(1460 + 250), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            215.0, CROP_TYPE_OATS_JOEY, 19.0, now.minusDays(1460 + 70), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_BARLEY_JOEY, 18.5, now.minusDays(1825 + 230), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_RICE_JOEY, 18.0, now.minusDays(1825 + 50), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            200.0, CROP_TYPE_SOYBEAN_JOEY, 17.5, now.minusDays(2190 + 210), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOEY, 17.0, now.minusDays(2190 + 30), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            190.0, CROP_TYPE_CORN_JOEY, 16.5, now.minusDays(2555 + 190), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            185.0, CROP_TYPE_TOMATO_JOEY, 16.0, now.minusDays(2555 + 10), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_POTATO_JOEY, 15.5, now.minusDays(2920 + 330), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            175.0, CROP_TYPE_COTTON_JOEY, 15.0, now.minusDays(2920 + 150), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            170.0, CROP_TYPE_OATS_JOEY, 14.5, now.minusDays(3285 + 310), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            165.0, CROP_TYPE_BARLEY_JOEY, 14.0, now.minusDays(3285 + 130), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            160.0, CROP_TYPE_RICE_JOEY, 13.5, now.minusDays(3650 + 290), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            155.0, CROP_TYPE_SOYBEAN_JOEY, 13.0, now.minusDays(3650 + 110), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            150.0, CROP_TYPE_WHEAT_JOEY, 12.5, now.minusDays(4015 + 270), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            145.0, CROP_TYPE_CORN_JOEY, 12.0, now.minusDays(4015 + 90), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            140.0, CROP_TYPE_TOMATO_JOEY, 11.5, now.minusDays(4380 + 250), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            135.0, CROP_TYPE_POTATO_JOEY, 11.0, now.minusDays(4380 + 70), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            130.0, CROP_TYPE_COTTON_JOEY, 10.5, now.minusDays(4745 + 230), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            125.0, CROP_TYPE_OATS_JOEY, 10.0, now.minusDays(4745 + 50), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            120.0, CROP_TYPE_BARLEY_JOEY, 9.5, now.minusDays(5110 + 210), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            115.0, CROP_TYPE_RICE_JOEY, 9.0, now.minusDays(5110 + 30), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            110.0, CROP_TYPE_SOYBEAN_JOEY, 8.5, now.minusDays(5475 + 190), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            105.0, CROP_TYPE_WHEAT_JOEY, 8.0, now.minusDays(5475 + 10), JOEY_DOE, FIELD_2_JOEY
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOEY, 24.0, now.minusDays(365 + 30), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOEY, 23.0, now.minusDays(365 + 200), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOEY, 22.0, now.minusDays(730 + 50), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOEY, 21.0, now.minusDays(730 + 220), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOEY, 20.0, now.minusDays(1095 + 70), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOEY, 19.0, now.minusDays(1095 + 240), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOEY, 17.0, now.minusDays(1460 + 90), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOEY, 16.0, now.minusDays(1460 + 260), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOEY, 15.0, now.minusDays(1825 + 110), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOEY, 14.0, now.minusDays(1825 + 280), JOEY_DOE, FIELD_3_JOEY
        ));

        crops.add(new Crop(
            92.0, CROP_TYPE_CORN_JOEY, 8.2, now.minusDays(0 + 30), JOEY_DOE, FIELD_4_JOEY
        ));
        crops.add(new Crop(
            88.0, CROP_TYPE_RICE_JOEY, 7.8, now.minusDays(0 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            105.0, CROP_TYPE_SOYBEAN_JOEY, 9.5, now.minusDays(365 + 30), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            101.0, CROP_TYPE_BARLEY_JOEY, 9.0, now.minusDays(365 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            76.0, CROP_TYPE_OATS_JOEY, 6.8, now.minusDays(730 + 30), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            72.0, CROP_TYPE_COTTON_JOEY, 6.4, now.minusDays(730 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            90.0, CROP_TYPE_POTATO_JOEY, 8.0, now.minusDays(1095 + 30), JOEY_DOE, FIELD_4_JOEY
        ));
        crops.add(new Crop(
            85.0, CROP_TYPE_TOMATO_JOEY, 7.6, now.minusDays(1095 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            108.0, CROP_TYPE_WHEAT_JOEY, 9.8, now.minusDays(1460 + 30), JOEY_DOE, FIELD_4_JOEY
        ));
        crops.add(new Crop(
            103.0, CROP_TYPE_CORN_JOEY, 9.2, now.minusDays(1460 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            74.0, CROP_TYPE_RICE_JOEY, 6.6, now.minusDays(1825 + 30), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            70.0, CROP_TYPE_SOYBEAN_JOEY, 6.2, now.minusDays(1825 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            93.0, CROP_TYPE_BARLEY_JOEY, 8.3, now.minusDays(2190 + 30), JOEY_DOE, FIELD_4_JOEY
        ));
        crops.add(new Crop(
            87.0, CROP_TYPE_OATS_JOEY, 7.7, now.minusDays(2190 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            106.0, CROP_TYPE_COTTON_JOEY, 9.6, now.minusDays(2555 + 30), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            100.0, CROP_TYPE_POTATO_JOEY, 9.1, now.minusDays(2555 + 200), JOEY_DOE, FIELD_4_JOEY
        ));

        crops.add(new Crop(
            28.0, CROP_TYPE_CORN_JOEY, 2.6, now.minusDays(0 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            34.0, CROP_TYPE_RICE_JOEY, 3.2, now.minusDays(365 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            22.0, CROP_TYPE_SOYBEAN_JOEY, 2.0, now.minusDays(730 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            27.0, CROP_TYPE_BARLEY_JOEY, 2.5, now.minusDays(1095 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            35.0, CROP_TYPE_OATS_JOEY, 3.3, now.minusDays(1460 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            21.0, CROP_TYPE_COTTON_JOEY, 1.9, now.minusDays(1825 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            26.0, CROP_TYPE_POTATO_JOEY, 2.4, now.minusDays(2190 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            33.0, CROP_TYPE_TOMATO_JOEY, 3.1, now.minusDays(2555 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            20.0, CROP_TYPE_WHEAT_JOEY, 1.8, now.minusDays(2920 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            27.5, CROP_TYPE_CORN_JOEY, 2.6, now.minusDays(3285 + 120), JOEY_DOE, FIELD_5_JOEY
        ));

        crops.add(new Crop(
            27.0, CROP_TYPE_CORN_JOEY, 2.6, now.minusDays(0 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            31.0, CROP_TYPE_RICE_JOEY, 3.0, now.minusDays(365 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            20.0, CROP_TYPE_SOYBEAN_JOEY, 1.9, now.minusDays(730 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            29.0, CROP_TYPE_BARLEY_JOEY, 2.7, now.minusDays(1095 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            23.0, CROP_TYPE_OATS_JOEY, 2.2, now.minusDays(1460 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            35.0, CROP_TYPE_COTTON_JOEY, 3.4, now.minusDays(1825 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            26.0, CROP_TYPE_POTATO_JOEY, 2.5, now.minusDays(2190 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            33.0, CROP_TYPE_TOMATO_JOEY, 3.1, now.minusDays(2555 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            21.0, CROP_TYPE_WHEAT_JOEY, 2.0, now.minusDays(2920 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            28.0, CROP_TYPE_CORN_JOEY, 2.7, now.minusDays(3285 + 120), JOEY_DOE, FIELD_7_JOEY
        ));

        crops.add(new Crop(
            54.0, CROP_TYPE_CORN_JOEY, 5.1, now.minusDays(0 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            63.0, CROP_TYPE_RICE_JOEY, 6.2, now.minusDays(365 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            38.0, CROP_TYPE_SOYBEAN_JOEY, 3.8, now.minusDays(730 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            61.0, CROP_TYPE_BARLEY_JOEY, 5.6, now.minusDays(1095 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            46.0, CROP_TYPE_OATS_JOEY, 4.4, now.minusDays(1460 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            72.0, CROP_TYPE_COTTON_JOEY, 6.9, now.minusDays(1825 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            52.0, CROP_TYPE_POTATO_JOEY, 5.0, now.minusDays(2190 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            67.0, CROP_TYPE_TOMATO_JOEY, 6.3, now.minusDays(2555 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            41.0, CROP_TYPE_WHEAT_JOEY, 3.9, now.minusDays(2920 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            58.0, CROP_TYPE_CORN_JOEY, 5.4, now.minusDays(3285 + 120), JOEY_DOE, FIELD_8_JOEY
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOEY, 24.0, now.minusDays(365 + 30), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOEY, 23.0, now.minusDays(365 + 200), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOEY, 22.0, now.minusDays(730 + 50), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOEY, 21.0, now.minusDays(730 + 220), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOEY, 20.0, now.minusDays(1095 + 70), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOEY, 19.0, now.minusDays(1095 + 240), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOEY, 17.0, now.minusDays(1460 + 90), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOEY, 16.0, now.minusDays(1460 + 260), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOEY, 15.0, now.minusDays(1825 + 110), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOEY, 14.0, now.minusDays(1825 + 280), JOEY_DOE, FIELD_11_JOEY
        ));

        crops.add(new Crop(
            270.0, CROP_TYPE_CORN_JOEY, 24.0, now.minusDays(365 + 30), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            255.0, CROP_TYPE_RICE_JOEY, 23.0, now.minusDays(365 + 200), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            240.0, CROP_TYPE_SOYBEAN_JOEY, 22.0, now.minusDays(730 + 50), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            265.0, CROP_TYPE_BARLEY_JOEY, 21.0, now.minusDays(730 + 220), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            235.0, CROP_TYPE_OATS_JOEY, 20.0, now.minusDays(1095 + 70), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            250.0, CROP_TYPE_COTTON_JOEY, 19.0, now.minusDays(1095 + 240), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            205.0, CROP_TYPE_POTATO_JOEY, 17.0, now.minusDays(1460 + 90), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            180.0, CROP_TYPE_TOMATO_JOEY, 16.0, now.minusDays(1460 + 260), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            210.0, CROP_TYPE_CORN_JOEY, 15.0, now.minusDays(1825 + 110), JOEY_DOE, FIELD_12_JOEY
        ));

        crops.add(new Crop(
            195.0, CROP_TYPE_WHEAT_JOEY, 14.0, now.minusDays(1825 + 280), JOEY_DOE, FIELD_12_JOEY
        ));

        saveCrops(crops);
    }

    private void saveCrops(List<Crop> crops) {
        cropRepository.saveAll(crops);
    }

    private void createTasks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.plusDays(1);
        LocalDateTime windowEnd = now.plusDays(3);

        TASK_1_JANE = createTask(
            JANE_DOE,
            "Harvest Preparation",
            windowStart,
            windowEnd,
            null,
            null,
            FIELD_1_JANE,
            List.of(HAMMER_JANE, SAW_JANE),
            List.of(TOOL_CATEGORY_DRILL_JANE),
            4.5f,
            false,
            TaskType.NONE
        );

        TASK_2_JANE = createTask(
            JANE_DOE,
            "Field Maintenance",
            windowStart,
            windowEnd,
            null,
            null,
            FIELD_2_JANE,
            List.of(SHOVEL_JANE, RAKE_JANE),
            List.of(TOOL_CATEGORY_LAWN_MOWER_JANE),
            3.0f,
            false,
            TaskType.NONE
        );
    }

    private Task createTask(Farmer owner, String name, LocalDateTime windowStart, LocalDateTime windowEnd,
                            LocalDateTime assignedStart, LocalDateTime assignedEnd,
                            Field field, List<Tool> tools, List<ToolCategory> categories,
                            float workTimeEstimate, boolean fixed, TaskType taskType) {
        Task task = new Task(owner, name, windowStart, windowEnd, assignedStart, assignedEnd,
            field, tools, categories, workTimeEstimate, fixed, taskType);
        return taskRepository.save(task);
    }

    private void createComplexTasks() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        //##################################################################
        //This section compares all non-active fields with each other (based on forecast as well as area size)

        //works

        //Non-active field with lower forecast
        createTask(
            JOHN_DOE,
            "Planting Wheat Non-Active Forecast",
            tomorrow.withHour(6),
            tomorrow.withHour(14),
            null, null,
            FIELD_7_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.PLANTING
        );

        //Non-active field with higher forecast
        createTask(
            JOHN_DOE,
            "Planting Corn Non-Active Forecast",
            tomorrow.withHour(6),
            tomorrow.withHour(14),
            null, null,
            FIELD_7_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.PLANTING
        );

        //------------------------------------------------------------------------------

        //Non-active field with higher area and no forecast

        //works

        createTask(
            JOHN_DOE,
            "Planting Wheat Non-Active No Forecast",
            tomorrow.plusDays(1).withHour(10),
            tomorrow.plusDays(1).withHour(15),
            null, null,
            FIELD_13_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            5.0f,
            false,
            TaskType.PLANTING
        );

        //Non-active field with lower area no forecast
        createTask(
            JOHN_DOE,
            "Planting Corn Non-Active No Forecast",
            tomorrow.plusDays(1).withHour(10),
            tomorrow.plusDays(1).withHour(15),
            null, null,
            FIELD_9_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            5.0f,
            false,
            TaskType.PLANTING
        );

        //------------------------------------------------------------------------------

        //Non-active field with lower area and no forecast

        //works

        createTask(
            JOHN_DOE,
            "Maintenance Non-Active No Forecast with Hole",
            tomorrow.plusDays(2).withHour(10),
            tomorrow.plusDays(2).withHour(15),
            null, null,
            FIELD_6_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            5.0f,
            false,
            TaskType.MAINTENANCE
        );

        //Non-active field with higher area no forecast
        createTask(
            JOHN_DOE,
            "Maintenance Non-Active No Forecast without Hole",
            tomorrow.plusDays(2).withHour(10),
            tomorrow.plusDays(2).withHour(15),
            null, null,
            FIELD_10_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            5.0f,
            false,
            TaskType.MAINTENANCE
        );

        //------------------------------------------------------------------------------

        //works

        //Non-active field with lower forecast
        createTask(
            JOHN_DOE,
            "None Wheat Non-Active Forecast",
            tomorrow.plusDays(3).withHour(6),
            tomorrow.plusDays(3).withHour(14),
            null, null,
            FIELD_7_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.NONE
        );

        //Non-active field with higher forecast
        createTask(
            JOHN_DOE,
            "None Corn Non-Active Forecast",
            tomorrow.plusDays(3).withHour(6),
            tomorrow.plusDays(3).withHour(14),
            null, null,
            FIELD_1_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //works

        //Non-active field with forecast
        createTask(
            JOHN_DOE,
            "Maintenance Barley Non-Active Forecast",
            tomorrow.plusDays(4).withHour(6),
            tomorrow.plusDays(4).withHour(14),
            null, null,
            FIELD_1_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.MAINTENANCE
        );

        //Non-active field with only area
        createTask(
            JOHN_DOE,
            "Maintenance Rye Non-Active No Forecast",
            tomorrow.plusDays(4).withHour(6),
            tomorrow.plusDays(4).withHour(14),
            null, null,
            FIELD_9_JOHN,
            List.of(SHOVEL_JOHN),
            null,
            8.0f,
            false,
            TaskType.MAINTENANCE
        );

        //#############################################################################
        //This section compares active fields with each other

        //works

        //This is a None Task
        createTask(
            JOHN_DOE,
            "None Peaches Forecast Tie-breaker ID",
            tomorrow.withHour(12),
            tomorrow.withHour(18),
            null, null,
            FIELD_10_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.NONE
        );

        //This is a None Task
        createTask(
            JOHN_DOE,
            "None Lemon Forecast Tie-breaker ID",
            tomorrow.withHour(12),
            tomorrow.withHour(18),
            null, null,
            FIELD_10_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //Test to see what happens when two tasks share the same tool category but not the same tool

        //works

        //This is a None Task
        createTask(
            JOHN_DOE,
            "None Cucumber Same Category",
            tomorrow.plusDays(1).withHour(12),
            tomorrow.plusDays(1).withHour(18),
            null, null,
            FIELD_1_JOHN,
            null,
            List.of(TOOL_CATEGORY_CHAINSAW_JOHN),
            6.0f,
            false,
            TaskType.NONE
        );


        //This is a None Task
        createTask(
            JOHN_DOE,
            "None Kiwi Same Category",
            tomorrow.plusDays(1).withHour(12),
            tomorrow.plusDays(1).withHour(18),
            null, null,
            FIELD_2_JOHN,
            null,
            List.of(TOOL_CATEGORY_CHAINSAW_JOHN),
            6f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //Test if tasks with an earlier end of their time window are given priority

        //works

        //This is a none task, with the end window one hour earlier
        createTask(
            JOHN_DOE,
            "None Nuts end date is one hour earlier",
            tomorrow.plusDays(2).withHour(6),
            tomorrow.plusDays(2).withHour(11),
            null, null,
            FIELD_12_JOHN,
            List.of(AXE_JOHN),
            null,
            5.0f,
            false,
            TaskType.NONE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "None Paprika end date is one hour later",
            tomorrow.plusDays(2).withHour(6),
            tomorrow.plusDays(2).withHour(12),
            null, null,
            FIELD_12_JOHN,
            List.of(AXE_JOHN),
            null,
            5.0f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //This test should decide if we prioritize a maintenance task or a none task

        //works

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "Maintenance Peaches",
            tomorrow.plusDays(3).withHour(6),
            tomorrow.plusDays(3).withHour(12),
            null, null,
            FIELD_11_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.MAINTENANCE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "None Bananas",
            tomorrow.plusDays(3).withHour(6),
            tomorrow.plusDays(3).withHour(12),
            null, null,
            FIELD_11_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //This test should decide if we prioritize a planting task or a maintenance task

        //works

        //This is just a normal planting task
        createTask(
            JOHN_DOE,
            "Planting Coconuts",
            tomorrow.plusDays(4).withHour(6),
            tomorrow.plusDays(4).withHour(12),
            null, null,
            FIELD_5_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.PLANTING
        );

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "Maintenance Apples",
            tomorrow.plusDays(4).withHour(6),
            tomorrow.plusDays(4).withHour(12),
            null, null,
            FIELD_5_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.MAINTENANCE
        );

        //------------------------------------------------------------------------------

        //This test should decide if we prioritize a planting task or and emergency task

        //works

        //This is a planting task
        createTask(
            JOHN_DOE,
            "Planting Soybeans",
            tomorrow.plusDays(5).withHour(6),
            tomorrow.plusDays(5).withHour(12),
            null, null,
            FIELD_3_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.PLANTING
        );

        //This is an emergency task
        createTask(
            JOHN_DOE,
            "Emergency Kakao",
            tomorrow.plusDays(5).withHour(6),
            tomorrow.plusDays(5).withHour(12),
            null, null,
            FIELD_3_JOHN,
            List.of(AXE_JOHN),
            null,
            6.0f,
            false,
            TaskType.EMERGENCY
        );


        //------------------------------------------------------------------------------

        //This test should decide if we prioritize maintenance task with higher growth progress

        //works because maintenance more important with lower growth progress

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "Maintaining Rice first third",
            tomorrow.withHour(6),
            tomorrow.withHour(12),
            null, null,
            FIELD_3_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            6.0f,
            false,
            TaskType.MAINTENANCE
        );

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "Maintaining Lettuce second third",
            tomorrow.withHour(6),
            tomorrow.withHour(12),
            null, null,
            FIELD_11_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            6.0f,
            false,
            TaskType.MAINTENANCE
        );

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "Maintaining Cabbage third third",
            tomorrow.withHour(6),
            tomorrow.withHour(12),
            null, null,
            FIELD_12_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            6.0f,
            false,
            TaskType.MAINTENANCE
        );


        //#############################################################################
        //This section compares scores on active and non-active fields with each other

        //works comparison is valid

        //Comparison between a field with forecast and fields only evaluated based on their size
        createTask(
            JOHN_DOE,
            "Planting Potato",
            tomorrow.withHour(6),
            tomorrow.withHour(14),
            null, null,
            FIELD_1_JOHN,
            List.of(HAMMER_JOHN),
            null,
            4.0f,
            false,
            TaskType.PLANTING
        );

        //This task should be second because Field 5 without forecast is still reasonably big
        createTask(
            JOHN_DOE,
            "Planting Carrot",
            tomorrow.withHour(6),
            tomorrow.withHour(14),
            null, null,
            FIELD_10_JOHN,
            List.of(HAMMER_JOHN),
            null,
            4.0f,
            false,
            TaskType.PLANTING
        );

        //This task should be thrown out because it has no forecast and the size of the field is pretty small therefore it should be thrown
        createTask(
            JOHN_DOE,
            "Planting Onion",
            tomorrow.withHour(6),
            tomorrow.withHour(14),
            null, null,
            FIELD_6_JOHN,
            List.of(HAMMER_JOHN),
            null,
            4.0f,
            false,
            TaskType.PLANTING
        );


        //#############################################################################
        //This section includes more advanced testing logic that should be shown

        //Test to see if two smaller tasks are prioritized over one bigger task with Maintenance Tasks (the small ones together should have a higher priority)

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "BigTask Profit Maintenance",
            tomorrow.plusDays(2).withHour(18),
            tomorrow.plusDays(2).withHour(22),
            null, null,
            FIELD_3_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.MAINTENANCE
        );

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Profit Maintenance",
            tomorrow.plusDays(2).withHour(18),
            tomorrow.plusDays(2).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.MAINTENANCE
        );

        //This is a maintenance task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Profit Maintenance",
            tomorrow.plusDays(2).withHour(18),
            tomorrow.plusDays(2).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.MAINTENANCE
        );


        //------------------------------------------------------------------------------

        //Test to see if two smaller tasks are prioritized over one bigger task with Emergency Tasks (the small ones together should have a higher priority)

        //This is an emergency Task
        createTask(
            JOHN_DOE,
            "BigTask Profit Emergency",
            tomorrow.plusDays(3).withHour(18),
            tomorrow.plusDays(3).withHour(22),
            null, null,
            FIELD_3_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.EMERGENCY
        );

        //This is an emergency Task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Profit Emergency",
            tomorrow.plusDays(3).withHour(18),
            tomorrow.plusDays(3).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.EMERGENCY
        );

        //This is an emergency task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Profit Emergency",
            tomorrow.plusDays(3).withHour(18),
            tomorrow.plusDays(3).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.EMERGENCY
        );

        //------------------------------------------------------------------------------

        //Test to see if two smaller tasks are prioritized over one bigger task with Harvesting Tasks (the small ones together should have a higher priority)

        //This is a harvesting Task
        createTask(
            JOHN_DOE,
            "BigTask Profit Harvesting",
            tomorrow.plusDays(4).withHour(18),
            tomorrow.plusDays(4).withHour(22),
            null, null,
            FIELD_3_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.HARVESTING
        );

        //This is a harvesting Task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Profit Harvesting",
            tomorrow.plusDays(4).withHour(18),
            tomorrow.plusDays(4).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.HARVESTING
        );

        //This is a harvesting task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Profit Harvesting",
            tomorrow.plusDays(4).withHour(18),
            tomorrow.plusDays(4).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.HARVESTING
        );

        //------------------------------------------------------------------------------

        //Test if two smaller tasks are prioritized over one bigger task with type planting on non-active field (the small ones together should have a higher priority)

        //This is a planting task
        createTask(
            JOHN_DOE,
            "BigTask Area Planting",
            tomorrow.plusDays(5).withHour(18),
            tomorrow.plusDays(5).withHour(22),
            null, null,
            FIELD_5_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.PLANTING
        );

        //This is a planting task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Area Planting",
            tomorrow.plusDays(5).withHour(18),
            tomorrow.plusDays(5).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.PLANTING
        );

        //This is a planting task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Area Planting",
            tomorrow.plusDays(5).withHour(18),
            tomorrow.plusDays(5).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.PLANTING
        );

        //------------------------------------------------------------------------------

        //Test if two smaller tasks are prioritized over one bigger task with type none on non-active field (the small ones together should have a higher priority)

        //This is a none task
        createTask(
            JOHN_DOE,
            "BigTask Area None",
            tomorrow.plusDays(6).withHour(18),
            tomorrow.plusDays(6).withHour(22),
            null, null,
            FIELD_5_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.NONE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Area None",
            tomorrow.plusDays(6).withHour(18),
            tomorrow.plusDays(6).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.NONE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Area None",
            tomorrow.plusDays(6).withHour(18),
            tomorrow.plusDays(6).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.NONE
        );

        //------------------------------------------------------------------------------

        //Test if two smaller tasks with crops data are prioritized over one bigger task with type none on non-active field (the small ones together should have a higher priority)

        //This is a none task
        createTask(
            JOHN_DOE,
            "BigTask Forecasting None",
            tomorrow.plusDays(7).withHour(18),
            tomorrow.plusDays(7).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            4.0f,
            false,
            TaskType.NONE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "SmallTask 1 Forecasting None",
            tomorrow.plusDays(7).withHour(18),
            tomorrow.plusDays(7).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.NONE
        );

        //This is a none task
        createTask(
            JOHN_DOE,
            "SmallTask 2 Forecasting None",
            tomorrow.plusDays(7).withHour(18),
            tomorrow.plusDays(7).withHour(22),
            null, null,
            FIELD_2_JOHN,
            List.of(LAWN_MOWER_JOHN),
            null,
            2.0f,
            false,
            TaskType.NONE
        );


    }

    // ----- Joey Tasks----------
    private LocalDateTime nextFeb2Midnight() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        LocalDateTime feb2 = LocalDateTime.of(year, 2, 2, 0, 0);
        if (now.isAfter(feb2)) {
            feb2 = feb2.plusYears(1);
        }
        return feb2;
    }

    private void createJoeyFullPlanTasks() {
        LocalDateTime base = nextFeb2Midnight();

        List<Field> fields = List.of(
            FIELD_1_JOEY, FIELD_2_JOEY, FIELD_3_JOEY, FIELD_4_JOEY, FIELD_5_JOEY, FIELD_6_JOEY,
            FIELD_7_JOEY, FIELD_8_JOEY, FIELD_9_JOEY, FIELD_10_JOEY, FIELD_11_JOEY, FIELD_12_JOEY, FIELD_13_JOEY
        );

        // Safety: if you forgot to create fleet items, fail fast in dev
        if (JOEY_TRACTOR_150HP == null || JOEY_SPRAYER == null || JOEY_COMBINE == null || JOEY_TELEHANDLER == null
            || JOEY_SEED_DRILL == null || JOEY_FERTILIZER_SPREADER == null || JOEY_DISC_HARROW == null || JOEY_PLOW == null) {
            throw new IllegalStateException("Joey fleet items not initialized (call createJoeyFleetVehicles() + createJoeyTractorImplements()).");
        }

        for (int d = 0; d < 30; d++) {
            LocalDateTime day = base.plusDays(d);

            Field fa = fields.get(d % fields.size());
            Field fb = fields.get((d + 4) % fields.size());
            Field fc = fields.get((d + 8) % fields.size());

            // 1) Soil sampling
            createTask(
                JOEY_DOE,
                "Soil sampling",
                day.withHour(6), day.withHour(12),
                null, null,
                fa,
                List.of(SHOVEL_JOEY),
                List.of(),
                3.0f,
                false,
                TaskType.MAINTENANCE
            );

            // 2) Primary tillage (tractor bottleneck)
            Tool tillageImpl = (d % 3 == 0) ? JOEY_DISC_HARROW : (d % 3 == 1 ? JOEY_PLOW : JOEY_DISC_HARROW);
            createTask(
                JOEY_DOE,
                "Primary tillage",
                day.withHour(6), day.withHour(22),
                null, null,
                fa,
                List.of(JOEY_TRACTOR_150HP, tillageImpl),
                List.of(),
                6.0f,
                false,
                TaskType.MAINTENANCE
            );

            // 3) Fertilizer spreading (same tractor bottleneck)
            createTask(
                JOEY_DOE,
                "Fertilizer spreading",
                day.withHour(8), day.withHour(20),
                null, null,
                fb,
                List.of(JOEY_TRACTOR_150HP, JOEY_FERTILIZER_SPREADER),
                List.of(),
                3.5f,
                false,
                TaskType.MAINTENANCE
            );

            // 4) Herbicide spraying (every 2nd day)
            if (d % 2 == 0) {
                createTask(
                    JOEY_DOE,
                    "Herbicide spraying",
                    day.withHour(10), day.withHour(18),
                    null, null,
                    fc,
                    List.of(JOEY_SPRAYER),
                    List.of(),
                    3.0f,
                    false,
                    TaskType.MAINTENANCE
                );
            }

            // 5) Seed drilling blocks
            if (d % 3 == 1) {
                createTask(
                    JOEY_DOE,
                    "Seed drilling",
                    day.withHour(9), day.withHour(22),
                    null, null,
                    fa,
                    List.of(JOEY_TRACTOR_150HP, JOEY_SEED_DRILL),
                    List.of(),
                    6.0f,
                    false,
                    TaskType.PLANTING
                );
            }

            // 6) Fixed workshop service once per week (field must not be null)
            if (d % 7 == 1) {
                LocalDateTime s = day.withHour(13);
                LocalDateTime e = day.withHour(16);
                createTask(
                    JOEY_DOE,
                    "FIXED: Workshop service",
                    s, e,
                    s, e,
                    fa,
                    List.of(JOEY_SPRAYER),
                    List.of(),
                    3.0f,
                    true,
                    TaskType.MAINTENANCE
                );
            }

            // 7) Fixed emergency every ~10 days
            if (d % 10 == 3) {
                LocalDateTime s = day.withHour(15);
                LocalDateTime e = day.withHour(17);
                createTask(
                    JOEY_DOE,
                    "FIXED: Emergency repair",
                    s, e,
                    s, e,
                    fb,
                    List.of(WRENCH_JOEY, PLIERS_JOEY),
                    List.of(),
                    4.0f,
                    true,
                    TaskType.EMERGENCY
                );
            }

            // 8) Evening maintenance task using categories
            if (d % 2 == 1) {
                createTask(
                    JOEY_DOE,
                    "Fence maintenance",
                    day.withHour(17), day.withHour(22),
                    null, null,
                    fc,
                    List.of(),
                    List.of(TOOL_CATEGORY_DRILL_JOEY),
                    2.5f,
                    false,
                    TaskType.MAINTENANCE
                );
            }
        }

        // End-of-month logistics block: combine + telehandler overlap chain
        for (int d = 22; d < 30; d += 2) {
            LocalDateTime day = base.plusDays(d);
            Field f = fields.get((d + 2) % fields.size());

            createTask(
                JOEY_DOE,
                "Harvesting",
                day.withHour(6), day.withHour(18),
                null, null,
                f,
                List.of(JOEY_COMBINE),
                List.of(),
                6.0f,
                false,
                TaskType.HARVESTING
            );

            createTask(
                JOEY_DOE,
                "Loading & transport",
                day.withHour(8), day.withHour(20),
                null, null,
                f,
                List.of(JOEY_TELEHANDLER),
                List.of(),
                4.0f,
                false,
                TaskType.HARVESTING
            );
        }
    }
}