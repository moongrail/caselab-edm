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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentPageDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.mapper.document.DocumentMapper;
import ru.caselab.edm.backend.mapper.documentversion.DocumentVersionMapper;
import ru.caselab.edm.backend.service.ApprovementService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.MinioService;
import ru.caselab.edm.backend.service.SignatureService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management operations")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SignatureService signatureService;
    private final DocumentVersionMapper documentVersionMapper;
    private final ApprovementService approvementService;
    private final MinioService minioService;

    @Operation(
            summary = "Start approval process for current document version"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval process was startes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApprovementProcessDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document with provided version ID not found or user not found with provided ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Document already sent to user", content = @Content)
    })
    @PostMapping("/approvement/start")
    public ResponseEntity<ApprovementProcessDTO> startApprovement(
            @Valid @RequestBody ApprovementProcessCreateDTO processCreateDTO,
            @AuthenticationPrincipal UserInfoDetails authenticatedUser
    ) {
        return new ResponseEntity<>(approvementService.createApprovementProcess(processCreateDTO, authenticatedUser), HttpStatus.OK);
    }


    @Operation(summary = "Sign document with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document was successfully signed",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Signature already exists")
    })
    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO,
                             @Parameter(description = "Document ID", required = true, example = "1")
                             @PathVariable Long id,
                             @AuthenticationPrincipal UserInfoDetails authenticatedUser) {
        signatureService.sign(signatureCreateDTO, id, authenticatedUser);
    }

    @Operation(
            summary = "Send document for signature",
            description = "Send document for signature to user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document sent for signature",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApprovementProcessItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document with provided version ID not found or user not found with provided ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Document already sent to user", content = @Content)
    })
    @PostMapping("/{id}/send_for_signature")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApprovementProcessItemDTO> sendForSignature(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable(name = "id") Long id,
            @Parameter(description = "User ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000,550e8400-e29b-41d4-a716-446655440001")
            @RequestParam(name = "userId") UUID userId,
            @AuthenticationPrincipal UserInfoDetails authenticatedUser) {
        return new ResponseEntity<>(documentService.sendForSign(userId, id, authenticatedUser), HttpStatus.OK);
    }


    @Operation(summary = "Creation document and first version of this document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document and version successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentVersionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document/User not found", content = @Content)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentVersionDTO createDocument(@Valid @RequestBody DocumentCreateDTO documentCreateDTO,
                                             @AuthenticationPrincipal UserInfoDetails user) {
        return documentVersionMapper.toDto(documentService.saveDocument(documentCreateDTO, user.getId()));
    }

    @Operation(summary = "Returning last version all documents of the current user")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocuments(@RequestParam(name = "page", defaultValue = "0")
                                                               @Min(value = 0) int page,
                                                               @RequestParam(name = "size", defaultValue = "10")
                                                               @Min(value = 1) @Max(value = 100) int size,
                                                               @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                               DocumentSortingType sortingType,
                                                               @AuthenticationPrincipal UserInfoDetails user) {
        return documentService.getAllDocumentForUser(page, size, user.getId(), sortingType);
    }

    @Operation(summary = "Returning last version document of the current user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/{id}/versions/last")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO getLastVersionDocumentById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal UserInfoDetails user
    ) {
        DocumentVersion documentForUser = documentService.getLastVersionDocumentForUser(id, user.getId());

        return documentVersionMapper.toDto(documentForUser);
    }

    @Operation(summary = "Returning all version document of the current user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.OK)
    public List<DocumentVersionDTO> getAllVersionDocumentById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal UserInfoDetails user
    ) {
        List<DocumentVersion> documentForUser = documentService.getAllVersionDocumentForUser(id, user.getId());

        return documentVersionMapper.toDto(documentForUser);
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
            @AuthenticationPrincipal UserInfoDetails user,
            @RequestBody @Valid DocumentUpdateDTO updateDocument) {
        return documentVersionMapper.toDto(documentService.updateDocument(id, updateDocument, user.getId()));
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

    @Operation(
            summary = "Get a link to download the document",
            description = """
                    The method requires the value of the contentUrl field of the document for which the download link will be returned.
                    Note: The link is only valid for 15 minutes. After the time has passed, a new one must be generated!
                    """
    )
    @ApiResponse(responseCode = "200", description = "Download link was successfully returned", content = @Content)
    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    public String downloadDocument(
            @Parameter(description = "The value of the contentUrl field of the document", example = "")
            @RequestParam("url") String url) {
        return minioService.generateTemporaryUrlToObject(url);
    }

}