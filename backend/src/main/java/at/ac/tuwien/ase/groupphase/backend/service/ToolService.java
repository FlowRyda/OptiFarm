package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CreateQrCodeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for managing {@link at.ac.tuwien.ase.groupphase.backend.entity.Tool} entities.
 * Provides CRUD operations and validation handling for tool-related functionality.
 */
public interface ToolService {

    /**
     * Creates a new tool.
     *
     * @param dto the {@link ToolDto} containing the tool data to create
     * @return the created {@link ToolDto} including its generated ID
     * @throws ValidationException if validation of the tool data fails
     */
    ToolDto createTool(ToolDto dto) throws ValidationException;

    /**
     * Retrieves a tool by its ID.
     *
     * @param id the unique identifier of the tool
     * @return the {@link ToolDto} representing the requested tool
     * @throws NotFoundException     if no tool with the given ID exists
     * @throws AccessDeniedException if the logged in farmer does not own the tool
     */
    ToolDto getToolById(long id) throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves all tools owned for the logged in farmer.
     *
     * @return a list of {@link ToolDto} objects representing all tools owned by the farmer
     */
    List<ToolDto> getAllTools();

    /**
     * Updates an existing tool.
     *
     * @param id  the ID of the tool to update
     * @param dto the {@link ToolDto} containing updated data
     * @return the updated {@link ToolDto}
     * @throws ValidationException   if validation of the updated data fails
     * @throws NotFoundException     if no tool with the given ID exists
     * @throws AccessDeniedException if the logged in farmer does not own the tool
     */
    ToolDto updateTool(long id, ToolDto dto) throws ValidationException, NotFoundException, AccessDeniedException;

    /**
     * Deletes a tool by its ID.
     *
     * @param id the ID of the tool to delete
     * @throws NotFoundException     if no tool with the given ID exists
     * @throws AccessDeniedException if the logged in farmer does not own the tool
     */
    void deleteTool(long id) throws NotFoundException, AccessDeniedException;

    byte[] generateQrCode(CreateQrCodeDto qrCodeDto) throws IOException, WriterException;

    boolean setNewPosition(long id, String token, CoordinateDto coordinatedto);
}
