package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CreateQrCodeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.ToolService;
import com.google.zxing.WriterException;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "api/v1/tool")
public class ToolEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolService toolService;

    @Autowired
    public ToolEndpoint(ToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping
    public ResponseEntity<ToolDto> createTool(@RequestBody ToolDto dto) throws ValidationException {
        LOGGER.trace("Create tool with id {} and body {}", dto.getId(), dto);
        return ResponseEntity.ok(toolService.createTool(dto));
    }

    @GetMapping
    public ResponseEntity<List<ToolDto>> getAllTools() {
        LOGGER.trace("Getting all tools for current farmer.");
        return ResponseEntity.ok(toolService.getAllTools());
    }

    @GetMapping("{id}")
    public ResponseEntity<ToolDto> getTool(@PathVariable("id") long id) {
        LOGGER.trace("Getting tool with id {}", id);
        return ResponseEntity.ok(toolService.getToolById(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ToolDto> updateTool(@PathVariable("id") long id, @RequestBody ToolDto dto) throws ValidationException {
        LOGGER.trace("Update tool with id {} and body {}", id, dto);
        return ResponseEntity.ok(toolService.updateTool(id, dto));
    }

    @DeleteMapping("{id}")
    public boolean deleteTool(@PathVariable("id") long id) {
        LOGGER.trace("Delete tool with id {}", id);
        toolService.deleteTool(id);
        return true;
    }

    @PostMapping("qrcode")
    public ResponseEntity<byte[]> getQrCode(@RequestBody CreateQrCodeDto qrCodeDto) throws IOException, WriterException {
        var qrcode = toolService.generateQrCode(qrCodeDto);
        return ResponseEntity.ok(qrcode);
    }

    @PermitAll
    @PostMapping("position/{id}/{token}")
    public ResponseEntity<Boolean> setNewPosition(@PathVariable("id") long id, @PathVariable("token") String token, @RequestBody CoordinateDto coordinateDto) {
        LOGGER.trace("Set new position of tool with id {}", id);
        var updated = toolService.setNewPosition(id, token, coordinateDto);
        return ResponseEntity.ok(updated);
    }
}
