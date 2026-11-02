package ru.caselab.edm.backend.approval.api;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.approval.application.ApprovementService;
import ru.caselab.edm.backend.approval.application.SignatureService;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.platform.security.AuthenticatedUser;

import java.util.UUID;

/**
 * Approval and signing use cases. URLs remain under {@code /document} for
 * backwards compatibility, but the controller belongs to the approval context.
 */
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Approval", description = "Document approval and signature operations")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('USER')")
public class ApprovalController {

    private final ApprovementService approvementService;
    private final SignatureService signatureService;
    private final DocumentService documentService;

    @Operation(summary = "Start approval process for current document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval process was started",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApprovementProcessDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document or user not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Document already sent to user",
                    content = @Content)
    })
    @PostMapping("/approvement/start")
    public ResponseEntity<ApprovementProcessDTO> startApprovement(
            @Valid @RequestBody ApprovementProcessCreateDTO processCreateDTO,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return new ResponseEntity<>(
                approvementService.createApprovementProcess(processCreateDTO, authenticatedUser),
                HttpStatus.OK);
    }

    @Operation(summary = "Result approval process for current document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Result of approval process",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApprovementProcessResultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document not found", content = @Content)
    })
    @PostMapping("/approvement/{documentId}/result")
    public ResponseEntity<ApprovementProcessResultDTO> resultApprovement(
            @PathVariable Long documentId) {
        return new ResponseEntity<>(approvementService.resultOfApprovementProcess(documentId),
                HttpStatus.OK);
    }

    @Operation(summary = "Sign document with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document was successfully signed",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Signature already exists")
    })
    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO,
                             @Parameter(description = "Document ID", required = true, example = "1")
                             @PathVariable Long id,
                             @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        signatureService.sign(signatureCreateDTO, id, authenticatedUser);
    }

    @Operation(summary = "Send document for signature",
            description = "Send document for signature to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document sent for signature",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApprovementProcessItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Document or user not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access to the document is forbidden",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Document already sent to user",
                    content = @Content)
    })
    @PostMapping("/{id}/send_for_signature")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApprovementProcessItemDTO> sendForSignature(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable(name = "id") Long id,
            @Parameter(description = "User ID", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(name = "userId") UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return new ResponseEntity<>(
                documentService.sendForSign(userId, id, authenticatedUser), HttpStatus.OK);
    }
}
