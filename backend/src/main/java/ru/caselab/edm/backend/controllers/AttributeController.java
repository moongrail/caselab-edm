package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.List;

@RestController
@RequestMapping("/attributes")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Attribute", description = "Attribute management operations")
public class AttributeController {
    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @Operation(summary = "Create an document attribute",
            description = "Creates a new attribute. Note that a DocumentType must be created before creating an Attribute.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attribute successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttributeDTO.class))),
    })
    @PostMapping
    public ResponseEntity<AttributeDTO> createAttribute(
            @Parameter(description = "Details of the new attribute to be created")
            @Valid @RequestBody AttributeCreateDTO attributeDTO) {
        AttributeDTO attribute = attributeService.createAttribute(attributeDTO);
        return new ResponseEntity<>(attribute, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Get document attribute by id",
            description = ""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attribute found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttributeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document attribute not found by given id",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AttributeDTO> getAttributeById(
            @Parameter(description = "ID of the document attribute to be deleted", example = "1")
            @PathVariable Long id) {
        AttributeDTO attributeDTO = attributeService.getAttributeById(id);
        return new ResponseEntity<>(attributeDTO, HttpStatus.OK);
    }

    @Operation(summary = "Get all attributes",
            description = "Retrieves a paginated list of all attributes. You can specify the page number and size of the results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of attributes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttributeDTO.class))),
    })
    @GetMapping
    public ResponseEntity<Page<AttributeDTO>> getAllAttributes(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of attributes per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<AttributeDTO> attributes = attributeService.getAllAttributes(page, size);
        return new ResponseEntity<>(attributes, HttpStatus.OK);
    }

    @Operation(
            summary = "Update document attribute details",
            description = "Update details of an existing document attribute by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "document attribute updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttributeDTO.class))),
            @ApiResponse(responseCode = "404", description = "document attribute not found with the given ID or document type not found with the given ids ",
                    content = @Content),
    })
    @PutMapping("/{id}")
    public ResponseEntity<AttributeDTO> updateAttribute(
            @Parameter(description = "ID of the document attribute", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Updated attribute details")
            @Valid @RequestBody AttributeUpdateDTO updateAttributeDTO) {
        AttributeDTO updatedAttribute = attributeService.updateAttribute(id, updateAttributeDTO);
        return new ResponseEntity<>(updatedAttribute, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete an attribute",
            description = "Delete attribute by unique id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document Attribute deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Document Attribute not found with the given ID",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(
            @Parameter(description = "ID of the document attribute to be deleted", example = "1")
            @PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Search match attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the search list of attributes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttributeSearch.class))),
    })
    @GetMapping("/search")
    public List<AttributeSearch> search(@RequestParam String name) {
        return attributeService.findByNameWithMinLength(name);
    }
}
