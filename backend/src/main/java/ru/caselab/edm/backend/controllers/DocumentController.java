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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentVersionDTO;
import ru.caselab.edm.backend.mapper.DocumentMapper;
import ru.caselab.edm.backend.mapper.DocumentVersionMapper;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.SignatureService;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management operations")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SignatureService signatureService;
    private final DocumentVersionMapper documentVersionMapper;

/*    @Operation(summary = "Sign document with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document was successfully signed",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Signature already exists")
    })
    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO,
                             @Parameter(description = "Document id", required = true, example = "1")
                             @PathVariable Long id) {
        signatureService.sign(signatureCreateDTO, id);
    }

    @Operation(
            summary = "Send document for signature",
            description = "Send document for signature to users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document sent for signature", content = @Content),
            @ApiResponse(responseCode = "404", description = "Document with provided version ID not found",
                    content = @Content)
    })
    @PostMapping("/{id}/send_for_signature")
    @ResponseStatus(HttpStatus.OK)
    public void sendForSignature(
            @Parameter(description = "Document version ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "List of user IDs", required = true, example = "550e8400-e29b-41d4-a716-446655440000,550e8400-e29b-41d4-a716-446655440001")
            @NotEmpty @RequestParam List<UUID> userIds) {
        documentService.sendForSign(userIds, id);
    }*/


    @Operation(summary = "Creation document and first version of this document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document and version successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentVersionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document/User not found", content = @Content)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentVersionDTO createDocument(@Valid @RequestBody DocumentCreateDTO documentCreateDTO) {
        return documentVersionMapper.toDto(documentService.saveDocument(documentCreateDTO));
    }

/*    @Operation(summary = "Returning all documents of the current user")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size,
                                           @AuthenticationPrincipal UserInfoDetails user) {
        return documentMapper.toDtoPage(documentService.getAllDocumentForUser(page, size, user.getId()));
    }*/

    @Operation(summary = "Returning all documents of the current user")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size) {
        return documentMapper.toDtoPage(documentService.getAllDocuments(page, size));
    }

    @Operation(summary = "Returning document of the current user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO getDocumentById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id
            //,@AuthenticationPrincipal UserInfoDetails user
    ) {
        return documentMapper.toDto(documentService.getDocument(id));
        //return documentMapper.toDto(documentService.getDocumentForUser(id, user.getId()));
    }

    @Operation(summary = "Updating document fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document fields was successfully updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentVersionDTO.class))),
            @ApiResponse(responseCode = "404", description = "User/Document/Document type not found", content = @Content)
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO updateDocument(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody @Valid DocumentUpdateDTO updateDocument) {
        return documentVersionMapper.toDto(documentService.updateDocument(id, updateDocument));
    }

    @Operation(summary = "Deleting document")
    @ApiResponse(responseCode = "204", description = "Document was successfully deleted", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}
