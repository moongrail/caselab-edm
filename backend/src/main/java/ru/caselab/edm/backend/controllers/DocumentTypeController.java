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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.service.DocumentTypeService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/document_type")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Document type", description = "Document type management operations")
@PreAuthorize("hasRole('ADMIN')")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    @Operation(
            summary = "Creating a new document type",
            description = "Create a new document type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document type created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentTypeDTO.class)))
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentTypeDTO createDocumentType(
            @Parameter(description = "Details of the new document type to be created")
            @Valid @RequestBody DocumentTypeCreateDTO documentTypeCreateDTO) {
        return documentTypeService.createDocumentType(documentTypeCreateDTO);
    }


    @Operation(
            summary = "Get all document type",
            description = "Get a list of all document types with pagination. " +
                    "Specify the page number and the number of document type per page."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Getting the list of document types is successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentTypeDTO.class)))
    })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentTypeDTO> showAllDocumentTypes(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of document type per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return documentTypeService.getAllDocumentType(page, size);
    }

    @Operation(
            summary = "Get document type by ID",
            description = "Retrieve a document type by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentTypeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document type not found with the given ID",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentTypeDTO showDocumentType(
            @Parameter(description = "ID of the document type to be retrieved", example = "1")
            @PathVariable Long id) {
        return documentTypeService.getDocumentTypeById(id);
    }

    @Operation(
            summary = "Update document type details",
            description = "Update details of an existing document type by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentTypeDTO.class))),
            @ApiResponse(responseCode = "404",
                    description = "Document type not found with the given ID or role not found with the given name",
                    content = @Content)
    })
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentTypeDTO updateDocumentType(
            @Parameter(description = "ID of the document type to be updated", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Updated document type details")
            @RequestBody @Valid DocumentTypeUpdateDTO updateDocumentType) {
        return documentTypeService.updateDocumentType(id, updateDocumentType);
    }

    @Operation(
            summary = "Delete a document type",
            description = "Delete a document type by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document type deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Document type not found with the given ID",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocumentType(
            @Parameter(description = "ID of the document type to be deleted", example = "1")
            @PathVariable Long id) {
        documentTypeService.deleteDocumentType(id);
    }
}
