package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.service.ToolCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "api/v1/tool-category")
public class ToolCategoryEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolCategoryService toolCategoryService;

    @Autowired
    public ToolCategoryEndpoint(ToolCategoryService toolCategoryService) {
        this.toolCategoryService = toolCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<ToolCategoryDto>> getAllToolCategoriesByUser() {
        LOGGER.trace("Getting all tool categories for current farmer.");
        return ResponseEntity.ok(toolCategoryService.getAllToolCategories());
    }
}
