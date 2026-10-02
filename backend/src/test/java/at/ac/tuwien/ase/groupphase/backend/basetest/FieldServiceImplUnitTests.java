package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.FieldMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUser;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.impl.FieldServiceImpl;
import at.ac.tuwien.ase.groupphase.backend.service.validation.FieldValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FieldServiceImplUnitTests {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private FieldMapper fieldMapper;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private FieldValidator fieldValidator;
    @InjectMocks
    private FieldServiceImpl fieldService;


    private Farmer defaultFarmer;
    private Farmer otherFarmer;
    private Field defaultField;
    private MockedStatic<JwtUtil> jwtUtilMock;


    @BeforeEach
    void setup() {
        defaultFarmer = TestData.defaultFarmer();
        otherFarmer = TestData.otherFarmer();
        defaultField = TestData.defaultField(defaultFarmer);

        JwtUser jwtUser = TestData.JWT_USER;

        jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(JwtUtil::getActiveJwtUser).thenReturn(jwtUser);

        lenient().when(farmerRepository.findById(defaultFarmer.getId()))
            .thenReturn(Optional.of(defaultFarmer));

        lenient().doNothing().when(fieldValidator).validatePolygon(any(), any(), any());
    }

    @AfterEach
    void cleanup() {
        jwtUtilMock.close();
    }

    @Test
    void create_withValidDto_savesFieldAndReturnsSimpleDto() {
        FieldCreateDto createDto = TestData.validFieldCreateDto();

        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> {
            Field field = invocation.getArgument(0);
            field.setId(1L);
            return field;
        });

        SimpleFieldDto expectedDto = TestData.validSimpleFieldDto();
        when(fieldMapper.fieldToSimpleFieldDto(any(Field.class))).thenReturn(expectedDto);

        SimpleFieldDto result = fieldService.create(createDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test field", result.name());

        ArgumentCaptor<Field> fieldCaptor = ArgumentCaptor.forClass(Field.class);
        verify(fieldRepository).save(fieldCaptor.capture());
        Field savedField = fieldCaptor.getValue();

        assertAll(() -> {
            assertEquals("Test field", savedField.getName());
            assertEquals(defaultFarmer, savedField.getOwner());
            assertNotNull(savedField.getBoundaries());
        });
    }


    @Test
    void update_withValidDtoAndOwner_updatesFieldAndReturnsSimpleDto() {
        FieldUpdateDto updateDto = TestData.validFieldUpdateDto();
        SimpleFieldDto expectedDto = TestData.updatedSimpleFieldDto();

        when(fieldRepository.findById(1L)).thenReturn(Optional.of(defaultField));
        when(fieldRepository.save(any(Field.class))).thenReturn(defaultField);
        when(fieldMapper.fieldToSimpleFieldDto(any(Field.class))).thenReturn(expectedDto);

        SimpleFieldDto result = fieldService.update(updateDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Updated field", result.name());

        verify(fieldRepository).findById(1L);
        verify(fieldRepository).save(defaultField);
        assertEquals("Updated field", defaultField.getName());
    }

    @Test
    void update_withNonExistentField_throwsNotFoundException() {
        FieldUpdateDto updateDto = TestData.validFieldUpdateDto();

        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> fieldService.update(updateDto, 999L)
        );

        assertEquals("Field not found", exception.getMessage());
        verify(fieldRepository, never()).save(any());
    }

    @Test
    void update_withDifferentOwner_throwsAccessDeniedException() {
        Field fieldOwnedByOther = TestData.defaultField(otherFarmer);
        FieldUpdateDto updateDto = TestData.validFieldUpdateDto();

        when(fieldRepository.findById(1L)).thenReturn(Optional.of(fieldOwnedByOther));

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> fieldService.update(updateDto, 1L)
        );

        assertEquals("You are not allowed to edit this field", exception.getMessage());
        verify(fieldRepository, never()).save(any());
    }

    @Test
    void getFieldById_withValidIdAndOwner_returnsFieldDetailDto() {
        FieldDetailDto expectedDto = TestData.validFieldDetailDto();

        when(fieldRepository.findById(1L)).thenReturn(Optional.of(defaultField));
        when(fieldMapper.fieldToFieldDetailDto(defaultField)).thenReturn(expectedDto);

        FieldDetailDto result = fieldService.getFieldById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Detailed field", result.name());

        verify(fieldRepository).findById(1L);
        verify(fieldMapper).fieldToFieldDetailDto(defaultField);
    }

    @Test
    void getFieldById_withNonExistentField_throwsNotFoundException() {
        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> fieldService.getFieldById(999L)
        );

        assertEquals("Field not found", exception.getMessage());
    }

    @Test
    void getFieldById_withDifferentOwner_throwsAccessDeniedException() {
        Field fieldOwnedByOther = TestData.defaultField(otherFarmer);

        when(fieldRepository.findById(1L)).thenReturn(Optional.of(fieldOwnedByOther));

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> fieldService.getFieldById(1L)
        );

        assertEquals("You are not allowed to edit this field", exception.getMessage());
        verify(fieldMapper, never()).fieldToFieldDetailDto(any());
    }

    @Test
    void deleteFieldById_withValidIdAndOwner_deletesField() {
        when(fieldRepository.findById(1L)).thenReturn(Optional.of(defaultField));

        assertDoesNotThrow(() -> fieldService.deleteFieldById(1L));

        verify(fieldRepository).findById(1L);
        verify(fieldRepository).delete(defaultField);
    }

    @Test
    void deleteFieldById_withNonExistentField_throwsNotFoundException() {
        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> fieldService.deleteFieldById(999L)
        );

        assertEquals("Field not found", exception.getMessage());
        verify(fieldRepository, never()).delete(any());
    }

    @Test
    void deleteFieldById_withDifferentOwner_throwsAccessDeniedException() {
        Field fieldOwnedByOther = TestData.defaultField(otherFarmer);

        when(fieldRepository.findById(1L)).thenReturn(Optional.of(fieldOwnedByOther));

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> fieldService.deleteFieldById(1L)
        );

        assertEquals("You are not allowed to delete this field", exception.getMessage());
        verify(fieldRepository, never()).delete(any());
    }

    @Test
    void getAllFields_returnsAllFieldsForCurrentFarmer() {
        Field field1 = TestData.defaultField(defaultFarmer);
        field1.setName("Field 1");

        Field field2 = TestData.defaultField(defaultFarmer);
        field2.setId(2L);
        field2.setName("Field 2");

        List<Field> fields = List.of(field1, field2);

        SimpleFieldDto dto1 = new SimpleFieldDto(1L, "Field 1", null, null);
        SimpleFieldDto dto2 = new SimpleFieldDto(2L, "Field 2", null, null);
        List<SimpleFieldDto> expectedDtos = List.of(dto1, dto2);

        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(fields);
        when(fieldMapper.fieldListToSimpleFieldDtoList(fields)).thenReturn(expectedDtos);

        List<SimpleFieldDto> result = fieldService.getAllFields();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Field 1", result.get(0).name());
        assertEquals("Field 2", result.get(1).name());

        verify(fieldRepository).findAllByOwner(defaultFarmer);
        verify(fieldMapper).fieldListToSimpleFieldDtoList(fields);
    }

    @Test
    void getAllFields_withNoFields_returnsEmptyList() {
        when(fieldRepository.findAllByOwner(defaultFarmer)).thenReturn(List.of());
        when(fieldMapper.fieldListToSimpleFieldDtoList(List.of())).thenReturn(List.of());

        List<SimpleFieldDto> result = fieldService.getAllFields();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(fieldRepository).findAllByOwner(defaultFarmer);
    }

    @Test
    void create_withFarmerNotFound_throwsNotFoundException() {
        FieldCreateDto createDto = TestData.validFieldCreateDto();

        when(farmerRepository.findById(defaultFarmer.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> fieldService.create(createDto)
        );

        assertTrue(exception.getMessage().contains("Could not find a farmer with the id"));
        verify(fieldRepository, never()).save(any());
    }
}