package ru.caselab.edm.backend.delegation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.delegation.application.DelegationService;
import ru.caselab.edm.backend.shared.directory.api.UserInfoPageDTO;
import ru.caselab.edm.platform.security.AuthenticatedUser;

import java.util.UUID;

/**
 * Delegation endpoints. They belong to the document collaboration context and
 * stay outside the security module even though the URL is department-scoped.
 */
@RestController
@RequestMapping("/department/document")
@RequiredArgsConstructor
@Tag(name = "Delegation", description = "Document signing delegation")
public class DelegationController {

    private final DelegationService delegationService;

    @Operation(summary = "Get page of available users for delegation sign document's")
    @ApiResponse(responseCode = "200", description = "List of users",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoPageDTO.class)))
    @GetMapping("/{documentId}")
    public ResponseEntity<UserInfoPageDTO> getUsersForDelegation(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long documentId) {
        return ResponseEntity.ok(delegationService
                .getAvailableUsersForDelegation(user.id(), documentId, size, page));
    }

    @Operation(summary = "Delegate sign document's for another user in yours departments")
    @ApiResponse(responseCode = "200", description = "Delegation was successfully done")
    @GetMapping("/{documentId}/delegate")
    public ResponseEntity<Void> delegateSignForUser(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "User id to delegate", required = true,
                    example = "9858b2f2-1aaf-4dbc-98b2-f21aafedbc2e")
            @RequestParam(name = "userId") UUID userToDelegate) {
        delegationService.delegateSign(userToDelegate, user.id(), documentId);
        return ResponseEntity.ok().build();
    }
}
