package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ToolMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.GarageRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ToolCategoryService;
import at.ac.tuwien.ase.groupphase.backend.service.impl.ToolServiceImpl;
import at.ac.tuwien.ase.groupphase.backend.service.validation.ToolValidator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolServiceImplUnitTests {
    @Mock
    private ToolRepository toolRepository;
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private GarageRepository garageRepository;
    @Mock
    private ToolCategoryService toolCategoryService;
    @Mock
    private ToolValidator validator;
    @Mock
    private ToolMapper mapper;
    @InjectMocks
    private ToolServiceImpl toolService;

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
    void createTool_withValidDto_persistsToolAndReturnsMappedDto() throws ValidationException {
        ToolDto inputDto = TestData.validToolDto();
        ToolCategory categoryEntity = TestData.defaultCategory();
        Garage garage = new Garage();
        garage.setId(1L);
        garage.setOwner(defaultFarmer);
        garage.setOwnerId(defaultFarmer.getId());
        garage.setCoordinate(new Coordinate(0,0,0));

        when(garageRepository.getGarageByIdAndOwnerId(1L, defaultFarmer.getId()))
            .thenReturn(Optional.of(garage));

        when(toolCategoryService.getOrCreateToolCategory("Tractor"))
            .thenReturn(categoryEntity);

        when(toolRepository.save(any(Tool.class))).thenAnswer(invocation -> {
            Tool tool = invocation.getArgument(0);
            tool.setId(100L);
            return tool;
        });

        ToolDto mappedDto = TestData.validToolDto();
        mappedDto.setId(100L);

        when(mapper.mapToolEntityToToolDto(any(Tool.class)))
            .thenReturn(mappedDto);

        ToolDto result = toolService.createTool(inputDto);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals("My Tractor", result.getName());
            assertEquals("A powerful tractor", result.getDescription());
            assertNotNull(result.getCategoryDto());
            assertEquals("Tractor", result.getCategoryDto().getName());
            assertTrue(result.isUsable());
            assertEquals(80, result.getRemainingValue());
        });

        ArgumentCaptor<Tool> toolCaptor = ArgumentCaptor.forClass(Tool.class);
        verify(toolRepository).save(toolCaptor.capture());
        Tool savedTool = toolCaptor.getValue();

        assertAll(() -> {
            assertEquals("My Tractor", savedTool.getName());
            assertEquals("A powerful tractor", savedTool.getDescription());
            assertEquals(categoryEntity, savedTool.getCategory());
            assertEquals(defaultFarmer, savedTool.getOwner());
            assertTrue(savedTool.isUsable());
            assertEquals(80, savedTool.getRemainingValue());
            assertArrayEquals("image-data".getBytes(StandardCharsets.UTF_8), savedTool.getImage());
        });

        verify(validator).validateForCreate(inputDto);
    }

    @Test
    void getToolById_withExistingToolOwnedByFarmer_returnsMappedDto() {
        Tool toolEntity = TestData.defaultTool(defaultFarmer);

        when(toolRepository.findById(TestData.DEFAULT_TOOL_ID)).thenReturn(Optional.of(toolEntity));

        ToolDto mappedDto = TestData.validToolDto();
        mappedDto.setId(TestData.DEFAULT_TOOL_ID);

        when(mapper.mapToolEntityToToolDto(toolEntity)).thenReturn(mappedDto);

        ToolDto result = toolService.getToolById(TestData.DEFAULT_TOOL_ID);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(TestData.DEFAULT_TOOL_ID, result.getId());
            assertEquals("My Tractor", result.getName());
            assertEquals("A powerful tractor", result.getDescription());
            assertEquals("Tractor", result.getCategoryDto().getName());
        });

        verify(toolRepository).findById(TestData.DEFAULT_TOOL_ID);
        verify(mapper).mapToolEntityToToolDto(toolEntity);
    }

    @Test
    void updateTool_withValidDtoOwnedByFarmer_updatesAndReturnsMappedDto() throws Exception {
        Tool existingTool = TestData.defaultTool(defaultFarmer);
        when(toolRepository.findById(TestData.DEFAULT_TOOL_ID)).thenReturn(Optional.of(existingTool));
        Garage garage = new Garage();
        garage.setId(1L);
        garage.setOwner(defaultFarmer);
        garage.setOwnerId(defaultFarmer.getId());
        garage.setCoordinate(new Coordinate(0,0,0));

        when(garageRepository.getGarageByIdAndOwnerId(1L, defaultFarmer.getId()))
            .thenReturn(Optional.of(garage));

        ToolDto updateDto = TestData.updatedToolDto();
        ToolCategory updatedCategory = TestData.updatedCategoryEntity();

        when(toolCategoryService.getOrCreateToolCategory("Updated Category"))
            .thenReturn(updatedCategory);

        when(toolRepository.save(any(Tool.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToolDto mappedDto = TestData.updatedMappedToolDto();
        when(mapper.mapToolEntityToToolDto(existingTool)).thenReturn(mappedDto);

        ToolDto result = toolService.updateTool(TestData.DEFAULT_TOOL_ID, updateDto);

        assertNotNull(result);
        assertEquals(TestData.DEFAULT_TOOL_ID, result.getId());
        assertEquals("Updated name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(42, result.getRemainingValue());
        assertFalse(result.isUsable());
        assertEquals("Updated Category", result.getCategoryDto().getName());
        assertEquals("new-image-data", result.getImage());

        ArgumentCaptor<Tool> toolCaptor = ArgumentCaptor.forClass(Tool.class);
        verify(toolRepository).save(toolCaptor.capture());
        Tool savedTool = toolCaptor.getValue();

        assertEquals("Updated name", savedTool.getName());
        assertEquals("Updated description", savedTool.getDescription());
        assertEquals(42, savedTool.getRemainingValue());
        assertFalse(savedTool.isUsable());
        assertEquals(updatedCategory, savedTool.getCategory());
        assertArrayEquals("new-image-data".getBytes(StandardCharsets.UTF_8), savedTool.getImage());
        assertEquals(defaultFarmer, savedTool.getOwner());

        verify(validator).validateForUpdate(updateDto);
        verify(mapper).mapToolEntityToToolDto(existingTool);
    }

    @Test
    void createTool_withInvalidDto_throwsValidationException() throws Exception {
        ToolDto invalidDto = TestData.validToolDto();

        when(toolCategoryService.getOrCreateToolCategory("Tractor"))
            .thenReturn(TestData.defaultCategory());

        doThrow(new ValidationException("Validation Error",
            java.util.List.of("Name is required")))
            .when(validator)
            .validateForCreate(invalidDto);

        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> toolService.createTool(invalidDto)
        );

        assertAll(
            () -> assertEquals("Validation Error", ex.summary()),
            () -> assertTrue(ex.errors().contains("Name is required")),
            () -> assertEquals("Validation Error: Name is required", ex.getMessage())
        );

        verify(toolRepository, never()).save(any());
    }

    @Test
    void getToolById_withToolOwnedByAnotherFarmer_throwsAccessDeniedException() {
        Tool toolOwnedByOther = TestData.defaultTool(TestData.otherFarmer());
        when(toolRepository.findById(TestData.DEFAULT_TOOL_ID)).thenReturn(Optional.of(toolOwnedByOther));

        assertThrows(
            AccessDeniedException.class,
            () -> toolService.getToolById(TestData.DEFAULT_TOOL_ID)
        );

        verify(mapper, never()).mapToolEntityToToolDto(any());
    }
}
