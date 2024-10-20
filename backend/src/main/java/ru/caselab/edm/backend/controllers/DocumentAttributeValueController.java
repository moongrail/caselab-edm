package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;

@RestController
@RequestMapping("/attribute-values")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentAttributeValueController {

    private final DocumentAttributeValueService documentAttributeValueService;


    public DocumentAttributeValueController(DocumentAttributeValueService attributeValueService) {
        this.documentAttributeValueService = attributeValueService;
    }

    @Operation(summary = "Create an document attribute value",
            description = "Setting the value for the attribute.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attribute value successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentAttributeValueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found Attribute by this id or document version",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<DocumentAttributeValueDTO> createAttributeValue(
            @Parameter(description = "Details of the new document attribute value to be created\"")
            @Valid @RequestBody DocumentAttributeValueCreateDTO valueDTO) {

        DocumentAttributeValueDTO createValue = documentAttributeValueService.createDocumentAttributeValue(valueDTO);
        return new ResponseEntity<>(createValue, HttpStatus.CREATED);
    }
    @Operation(
            summary = "Get document attribute value by document version id and attribute id",
            description = ""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attribute value found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentAttributeValueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found Attribute by this id or document version",
                    content = @Content)
    })
    @GetMapping("/{documentId}/{attributeId}")
    public ResponseEntity<DocumentAttributeValueDTO> getAttributeValueById(
            @Parameter(description = "document id for this attribute value")
            @PathVariable Long documentId,

            @Parameter(description = "attribute id for this attribute value")
            @PathVariable Long attributeId) {
        DocumentAttributeValueDTO value = documentAttributeValueService.getDocumentAttributeValueByDocumentAndAttribute(documentId, attributeId);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(
            summary = "Get document attribute value by id",
            description = ""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attribute value found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentAttributeValueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found Attribute value by id",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentAttributeValueDTO> getAttributeValueById(
            @Parameter(description = "attribute value id")
            @PathVariable Long id) {
        DocumentAttributeValueDTO value = documentAttributeValueService.getDocumentAttributeValueById(id);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(
            summary = "Update document attribute value details",
            description = "Update details of an existing document attribute value by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "document attribute value updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentAttributeValueDTO.class))),
            @ApiResponse(responseCode = "404", description = "document attribute value not found with the given ID or document type not found with the given ids ",
                    content = @Content),
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentAttributeValueDTO> updateAttribute(
            @Parameter(description = "id of document attribute value")
            @PathVariable Long id,

            @Parameter(description = "Updated document attribute value details")
            @RequestBody DocumentAttributeValueUpdateDTO updateValue) {
        DocumentAttributeValueDTO value = documentAttributeValueService.updateDocumentAttributeValue(id, updateValue);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete an document attribute value",
            description = "Delete document attribute value by unique id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document Attribute value deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Document Attribute value not found with the given ID",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(
            @Parameter(description = "id of document attribute value")
            @PathVariable Long id) {
        documentAttributeValueService.deleteAttributeValue(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
