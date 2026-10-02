package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.SimpleActiveFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface TestData {

    Long ID = 1L;
    String HASHED_PW = "SomeHashedPassword";
    String BASE_URI = "/api/v1";

    List<GrantedAuthority> USER_ROLES = AuthorityUtils.createAuthorityList(new ArrayList<>() {
        {
            add("ROLE_FARMER");
        }
    });

    GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);


    String DEFAULT_FARMER_MAIL = "test@mail.com";
    String DEFAULT_FARMER_FIRST_NAME = "John";
    String DEFAULT_FARMER_LAST_NAME = "Doe";

    CreateFarmerDto CREATE_FARMER_DTO = new CreateFarmerDto(
        DEFAULT_FARMER_FIRST_NAME,
        DEFAULT_FARMER_LAST_NAME,
        DEFAULT_FARMER_MAIL,
        "password"
    );

    EditFarmerDto EDIT_FARMER_DTO = new EditFarmerDto(
        DEFAULT_FARMER_FIRST_NAME + "edited",
        DEFAULT_FARMER_LAST_NAME + "edited",
        "test@mail_edited.com"
    );


    ChangePasswordDto CHANGE_PASSWORD_DTO = new ChangePasswordDto(
        "newPassword", "oldPassword"
    );

    LoginDto LOGIN_DTO = new LoginDto(
        DEFAULT_FARMER_MAIL,
        "password"
    );

    static Polygon createTestPolygon() {
        Coordinate[] coords = new Coordinate[]{
            new Coordinate(15.967726, 48.310758),
            new Coordinate(15.971878, 48.311457),
            new Coordinate(15.969668, 48.314932),
            new Coordinate(15.965655, 48.313433),
            new Coordinate(15.967726, 48.310758)
        };
        return GEOMETRY_FACTORY.createPolygon(coords);
    }

    static Farmer defaultFarmer() {
        Farmer farmer = new Farmer();
        farmer.setId(ID);
        farmer.setEmail(DEFAULT_FARMER_MAIL);
        farmer.setFirstname(DEFAULT_FARMER_FIRST_NAME);
        farmer.setLastname(DEFAULT_FARMER_LAST_NAME);
        farmer.setHashedPassword(HASHED_PW);
        return farmer;
    }

    static Farmer editedFarmer() {
        Farmer farmer = new Farmer();
        farmer.setId(ID);
        farmer.setEmail("edited@example.com");
        farmer.setFirstname(DEFAULT_FARMER_FIRST_NAME + "edited");
        farmer.setLastname(DEFAULT_FARMER_LAST_NAME + "edited");
        farmer.setLastname(HASHED_PW);
        return farmer;
    }

    JwtUser JWT_USER = new JwtUser(ID, DEFAULT_FARMER_MAIL);
    long DEFAULT_TOOL_ID = 100L;
    ToolCategoryDto UPDATED_CATEGORY_DTO = new ToolCategoryDto(20L, "Updated Category");
    GarageDto GARAGE_DTO = new GarageDto(1L, 1L, "Main Garage", new CoordinateDto(0, 0));

    static ToolCategory defaultCategory() {
        ToolCategory category = new ToolCategory();
        category.setId(10L);
        category.setName("Tractor");
        return category;
    }

    static ToolDto validToolDto() {
        ToolDto dto = new ToolDto();
        dto.setName("My Tractor");
        dto.setDescription("A powerful tractor");
        dto.setCategoryDto(new ToolCategoryDto(10L, "Tractor"));
        dto.setImage("image-data");
        dto.setGarageDto(GARAGE_DTO);
        dto.setLocationDto(new CoordinateDto(0, 0));
        dto.setUsable(true);
        dto.setRemainingValue(80);
        return dto;
    }

    static Tool defaultTool(Farmer owner) {
        Tool tool = new Tool();
        Garage garage = new Garage();
        garage.setId(1L);
        garage.setOwnerId(owner.getId());
        garage.setName("Main Garage");
        garage.setOwner(owner);
        garage.setCoordinate(new Coordinate(0, 0, 0));

        tool.setId(DEFAULT_TOOL_ID);
        tool.setName("My Tractor");
        tool.setDescription("A powerful tractor");
        tool.setHomeGarage(garage);
        tool.setCoordinate(new Coordinate(0, 0, 0));
        tool.setCategory(defaultCategory());
        tool.setImage("image-data".getBytes(StandardCharsets.UTF_8));
        tool.setUsable(true);
        tool.setRemainingValue(80);
        tool.setOwner(owner);
        return tool;
    }

    static ToolCategory updatedCategoryEntity() {
        ToolCategory cat = new ToolCategory();
        cat.setId(20L);
        cat.setName("Updated Category");
        return cat;
    }

    static ToolDto updatedToolDto() {
        ToolDto dto = new ToolDto();
        dto.setName("Updated name");
        dto.setDescription("Updated description");
        dto.setCategoryDto(UPDATED_CATEGORY_DTO);
        dto.setImage("new-image-data");
        dto.setUsable(false);
        dto.setRemainingValue(42);
        dto.setGarageDto(GARAGE_DTO);
        dto.setLocationDto(new CoordinateDto(0, 0));
        return dto;
    }

    static ToolDto updatedMappedToolDto() {
        ToolDto dto = updatedToolDto();
        dto.setId(DEFAULT_TOOL_ID);
        return dto;
    }

    static Farmer otherFarmer() {
        Farmer f = new Farmer();
        f.setId(999L);
        f.setEmail("other@mail.com");
        f.setFirstname("Other");
        f.setLastname("Owner");
        f.setHashedPassword("pw");
        return f;
    }


    static List<CoordinateDto> createTestCoordinateDtoList() {
        Polygon polygon = createTestPolygon();
        Coordinate[] coords = polygon.getCoordinates();
        List<CoordinateDto> dtoList = new ArrayList<>();

        for (Coordinate coord : coords) {
            dtoList.add(new CoordinateDto(coord.x, coord.y));
        }

        return dtoList;
    }

    static SimpleFieldDto validSimpleFieldDto() {
        return new SimpleFieldDto(1L, "Test field", createTestCoordinateDtoList(), null);
    }

    static FieldCreateDto validFieldCreateDto() {
        return new FieldCreateDto("Test field", createTestCoordinateDtoList(), null);
    }

    static SimpleFieldDto updatedSimpleFieldDto() {
        return new SimpleFieldDto(1L, "Updated field", createTestCoordinateDtoList(), null);
    }

    static FieldDetailDto validFieldDetailDto() {
        return new FieldDetailDto(1L, "Detailed field", 12.5, createTestCoordinateDtoList(), null);
    }

    static FieldUpdateDto validFieldUpdateDto() {
        return new FieldUpdateDto("Updated field", createTestCoordinateDtoList(), null);
    }

    static SimpleActiveFieldDto validSimpleActiveFieldDto() {
        return new SimpleActiveFieldDto(
            1L,
            "Test active field",
            25,
            1L);
    }

    static ActiveFieldCreateDto validActiveFieldCreateDto() {
        return new ActiveFieldCreateDto(
            LocalDateTime.of(2000, 1, 1, 0, 0),
            1,
            1,
            50,
            null);
    }

    static ActiveFieldDetailDto validActiveFieldDetailDto() {
        return new ActiveFieldDetailDto(1L, "Detailed active field", null,
            LocalDateTime.of(2000, 6, 1, 0, 0),
            LocalDateTime.of(2000, 1, 15, 0, 0),
            50,
            10,
            1,
            1,
            1,
            "Test Seed",
            25.01);
    }

    static ActiveFieldUpdateDto validActiveFieldUpdateDto() {
        return new ActiveFieldUpdateDto(
            LocalDateTime.of(2000, 1, 1, 0, 0),
            LocalDateTime.of(2000, 6, 15, 0, 0),
            LocalDateTime.of(2000, 6, 10, 0, 0),
            1,
            1,
            1);
    }

    static Field defaultField(Farmer owner) {
        Field field = new Field(owner, "Test Field", createTestPolygon());
        field.setId(1L);
        return field;
    }

    static Field otherField(Farmer owner) {
        Field field = new Field(owner, "Other Test Field", createTestPolygon());
        field.setId(2L);
        return field;
    }

    static ActiveField defaultActiveField(Farmer owner) {
        ActiveField field = new ActiveField();

        field.setId(1L);
        field.setStartDate(LocalDateTime.of(2000, 1, 1, 0, 0));
        field.setEndDate(LocalDateTime.of(2000, 6, 15, 0, 0));
        field.setOwner(owner);
        field.setField(defaultField(owner));
        field.setSeed(defaultSeed(owner));
        field.setExpectedHarvestDate(LocalDateTime.now().plusDays((long) field.getSeed().getType().getGrowthDuration()));
        field.setSellingPrice(40);
        return field;
    }

    static Seed defaultSeed(Farmer owner) {
        Seed seed = new Seed();
        seed.setId(1L);
        seed.setName("Test Seed");
        seed.setType(defaultCropType(owner));
        seed.setBuyingPrice(25);
        seed.setBuyingTime(LocalDateTime.of(2000, 1, 1, 0, 0));
        seed.setOwner(owner);
        seed.setOwnerId(owner.getId());
        return seed;
    }

    static ActiveField otherActiveField(Farmer owner) {
        ActiveField field = new ActiveField();

        field.setId(1L);
        field.setStartDate(LocalDateTime.of(2000, 1, 1, 0, 0));
        field.setEndDate(LocalDateTime.of(2000, 6, 15, 0, 0));
        field.setOwner(owner);
        field.setField(defaultField(owner));
        field.setSeed(defaultSeed(owner));
        field.setExpectedHarvestDate(LocalDateTime.now().plusDays((long) field.getSeed().getType().getGrowthDuration()));
        field.setSellingPrice(50);
        return field;
    }

    static CropType defaultCropType(Farmer owner) {
        CropType cropType = new CropType();
        cropType.setId(1L);
        cropType.setName("Corn");
        cropType.setGrowthDuration(35.5);
        cropType.setOwner(owner);
        cropType.setOwnerId(owner.getId());
        return cropType;
    }


    static CropType defaultCropType(Long id, String name, Farmer owner) {
        CropType c = new CropType();
        c.setId(id);
        c.setName(name);
        c.setOwner(owner);
        c.setOwnerId(owner.getId());
        return c;
    }

    static CropTypeDto defaultCropTypeDto(Long id, String name) {
        CropTypeDto dto = new CropTypeDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    static Seed defaultSeed(Long id, String name, Long cropTypeId, Farmer owner) {
        Seed s = new Seed();
        s.setId(id);
        s.setName(name);
        s.setOwner(owner);
        s.setOwnerId(owner.getId());

        CropType type = new CropType();
        type.setId(cropTypeId);
        s.setType(type);

        return s;
    }

    static SeedDto defaultSeedDto(Long id, String name, Long cropTypeId) {
        SeedDto dto = new SeedDto();
        dto.setId(id);
        dto.setName(name);
        dto.setCropTypeId(cropTypeId);
        return dto;
    }

    static Field fieldWithArea(long id, double areaHa) {
        Field f = new Field();
        f.setId(id);
        f.setName("Field " + id);
        f.setArea(areaHa);
        return f;
    }

    static Seed seedWithBuyingPrice(Farmer owner, double buyingPrice) {
        Seed s = defaultSeed(owner);
        s.setBuyingPrice(buyingPrice);
        return s;
    }

    /**
     * Creates an ActiveField usable for PriorityContext (profit calc) and PriorityScorer progress-from-dates.
     */
    static ActiveField activeFieldForScoring(
        Farmer owner,
        Field field,
        Seed seed,
        double sellingPrice,
        LocalDateTime startDate,
        LocalDateTime expectedHarvestDate
    ) {
        ActiveField af = new ActiveField();
        af.setField(field);
        af.setSeed(seed);
        af.setSellingPrice(sellingPrice);
        af.setStartDate(startDate);
        af.setExpectedHarvestDate(expectedHarvestDate);
        af.setEndDate(null);
        af.setOwner(owner);
        af.setOwnerId(owner.getId());
        return af;
    }

    static Task taskWithTypeAndWindow(
        Field field,
        TaskType type,
        LocalDateTime windowStart,
        LocalDateTime windowEnd
    ) {
        return new Task(
            "Task " + type,
            windowStart,
            windowEnd,
            null,
            null,
            field,
            null,
            null,
            1.0f,
            false,
            type
        );
    }
}