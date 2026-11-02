package ru.caselab.edm.backend.document.api;

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
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentLinkDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentPageDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.backend.document.domain.DocumentSortingType;
import ru.caselab.edm.backend.document.api.mapper.DocumentVersionMapper;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.platform.storage.ObjectStorage;

import java.util.UUID;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management operations")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentVersionMapper documentVersionMapper;
    private final ObjectStorage objectStorage;
    private final AuthorizationService authorizationService;

    @Operation(summary = "Creation document and first version of this document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document and version successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentVersionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document/User not found", content = @Content)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentVersionDtoWithAuthor createDocument(@Valid @RequestBody DocumentCreateDTO documentCreateDTO,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return documentVersionMapper.toDtoWithAuthor(documentService.saveDocument(documentCreateDTO, user.id()), user.login());
    }

    @Operation(summary = "Returning last version all documents of the current user")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentsForUser(@RequestParam(name = "page", defaultValue = "0")
                                                                      @Min(value = 0) int page,
                                                                      @RequestParam(name = "size", defaultValue = "10")
                                                                      @Min(value = 1) @Max(value = 100) int size,
                                                                      @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                                      DocumentSortingType sortingType,
                                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return documentService.getAllDocumentForUser(page, size, user.id(), sortingType);
    }

    @Operation(summary = "Returns the latest version of all documents that the user has created aimed at signing or voting")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping("/owner/before_signer")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentsForUserBeforeSigner(@RequestParam(name = "page", defaultValue = "0")
                                                                      @Min(value = 0) int page,
                                                                      @RequestParam(name = "size", defaultValue = "10")
                                                                      @Min(value = 1) @Max(value = 100) int size,
                                                                      @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                                      DocumentSortingType sortingType,
                                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return documentService.getAllDocumentWhereUserOwnerBeforeSigner(page, size, user.id(), sortingType);
    }

    @Operation(summary = "Returns the latest version of all documents that the user created after signing or completing the vote")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping("/owner/after_signer")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentsForUserAfterSigner(@RequestParam(name = "page", defaultValue = "0")
                                                                      @Min(value = 0) int page,
                                                                      @RequestParam(name = "size", defaultValue = "10")
                                                                      @Min(value = 1) @Max(value = 100) int size,
                                                                      @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                                      DocumentSortingType sortingType,
                                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return documentService.getAllDocumentWhereUserOwnerAfterSigner(page, size, user.id(), sortingType);
    }

    @Operation(summary = "Returning last version of all documents of the sent to the user for approval or signature")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping("/signer/before_signer")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentsWhereUserSignatoriesBeforeSigner(@RequestParam(name = "page", defaultValue = "0")
                                                                                   @Min(value = 0) int page,
                                                                                   @RequestParam(name = "size", defaultValue = "10")
                                                                                   @Min(value = 1) @Max(value = 100) int size,
                                                                                   @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                                                   DocumentSortingType sortingType,
                                                                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return documentService.getAllDocumentWhereUserSignatoriesBeforeSigner(page, size, user.id(), sortingType);
    }


    @Operation(summary = "Returning last version of all documents of the sent to the user for approval or signature after signer user")
    @ApiResponse(responseCode = "200", description = "Documents of the current user were successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPageDTO.class)))
    @GetMapping("/signer/after_signer")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentOutputAllDocumentsDTO> getAllDocumentsWhereUserSignatoriesAfterSigner(@RequestParam(name = "page", defaultValue = "0")
                                                                                   @Min(value = 0) int page,
                                                                                   @RequestParam(name = "size", defaultValue = "10")
                                                                                   @Min(value = 1) @Max(value = 100) int size,
                                                                                   @RequestParam(name = "sort_type", defaultValue = "WITHOUT")
                                                                                   DocumentSortingType sortingType,
                                                                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return documentService.getAllDocumentWhereUserSignatoriesAfterSigner(page, size, user.id(), sortingType);
    }

    @Operation(summary = "Returning last version document of the sent to the user for approval or signature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/signer/{id}/versions/last")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO getLastVersionDocumentWhereUserSignatoriesById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user
    ) {
        DocumentVersion documentForUser = documentService.getLastVersionDocumentWhereUserSignatories(id, user.id());

        return documentVersionMapper.toDto(documentForUser);
    }

    @Operation(summary = "Returning last version document of the current user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/owner/{id}/versions/last")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO getLastVersionDocumentForUserById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user
    ) {
        DocumentVersion documentForUser = documentService.getLastVersionDocumentForUser(id, user.id());

        return documentVersionMapper.toDto(documentForUser);
    }

    @Operation(summary = "Returning all version document of the current user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/owner/{id}/versions")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentVersionDTO> getAllVersionDocumentForUserById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0) int page,
            @RequestParam(name = "size", defaultValue = "10")
            @Min(value = 1) @Max(value = 100) int size
    ) {
        Page<DocumentVersion> documentForUser = documentService.getAllVersionDocumentForUser(id, user.id(), page, size);

        return documentForUser.map(documentVersionMapper::toDto);
    }

    @Operation(summary = "Returning all version document of the sent to the user for approval or signature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document of the current user was successfully returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content)
    })
    @GetMapping("/signer/{id}/versions")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentVersionDTO> getAllVersionDocumentWhereUserSignatoriesById(
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0) int page,
            @RequestParam(name = "size", defaultValue = "10")
            @Min(value = 1) @Max(value = 100) int size
    ) {
        Page<DocumentVersion> documentForUser = documentService.getAllVersionDocumentWhereUserSignatories(id, user.id(), page, size);

        return documentForUser.map(documentVersionMapper::toDto);
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
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody @Valid DocumentUpdateDTO updateDocument) {
        return documentVersionMapper.toDto(documentService.updateDocument(id, updateDocument, user.id()));
    }

    @Operation(summary = "Deleting document")
    @ApiResponse(responseCode = "204", description = "Document was successfully deleted", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long id) {
        documentService.deleteDocument(id,user.id());
    }

    @Operation(summary = "Get archived documents for current user")
    @ApiResponse(responseCode = "200", description = " Archived document of the current user was successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentOutputAllDocumentsDTO.class)))
    @GetMapping("/archive")
    public ResponseEntity<Page<DocumentOutputAllDocumentsDTO>> getArchivedDocuments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0) int page,
            @RequestParam(name = "size", defaultValue = "10")
            @Min(value = 1) @Max(value = 100) int size
    ){
        return new ResponseEntity<>(documentService.getArchivedDocuments(page,size,user.id()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get a link to download the document",
            description = """
                    The method requires the value of the contentUrl field of the document for which the download link will be returned.
                    Note: The link is only valid for 15 minutes. After the time has passed, a new one must be generated!
                    """
    )
    @ApiResponse(responseCode = "200", description = "Download link was successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentLinkDTO.class)))
    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    public DocumentLinkDTO downloadDocument(
            @Parameter(description = "The value of the contentUrl field of the document", example = "")
            @RequestParam("url") String url,
            @AuthenticationPrincipal AuthenticatedUser user) {
        authorizationService.ensureContentAccess(url, user.id());
        return new DocumentLinkDTO(objectStorage.temporaryUrl(url));
    }



}
