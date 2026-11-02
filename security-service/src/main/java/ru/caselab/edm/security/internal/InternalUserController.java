package ru.caselab.edm.security.internal;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Trusted service-to-service directory API. It is protected by a shared
 * internal token and must not be exposed through the public gateway.
 */
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalUserService internalUserService;

    @GetMapping("/users/{id}")
    public ResponseEntity<InternalUserDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(internalUserService.get(id));
    }

    @PostMapping("/users/by-ids")
    public ResponseEntity<List<InternalUserDTO>> getUsers(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(internalUserService.getByIds(ids));
    }

    @GetMapping("/users/{id}/replacement-candidates")
    public ResponseEntity<List<InternalUserDTO>> replacementCandidates(@PathVariable UUID id) {
        return ResponseEntity.ok(internalUserService.replacementCandidates(id));
    }

    @GetMapping("/departments/{departmentId}/members")
    public ResponseEntity<InternalUserPageDTO> departmentMembers(
            @PathVariable Long departmentId,
            @RequestParam(name = "exclude", required = false) List<UUID> exclude,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(
                internalUserService.departmentMembers(departmentId, exclude, page, size));
    }

    @GetMapping("/departments/{departmentId}/manager")
    public ResponseEntity<InternalUserDTO> departmentManager(@PathVariable Long departmentId) {
        return ResponseEntity.ok(internalUserService.departmentManager(departmentId));
    }
}
