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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "User management operations")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieve all users with pagination. Specify the page number and the number of users per page."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided",
                    content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserPageDTO> getAllUsers(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a user by their unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to be retrieved", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Create a new user",
            description = "Create a new user with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user details provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found with the given name",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "User with the same login or email already exists",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "Details of the new user to be created")
            @RequestBody @Valid CreateUserDTO createdUser) {
        return ResponseEntity.ok(userService.createUser(createdUser));
    }

    @Operation(
            summary = "Update user details",
            description = "Update details of an existing user by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID or role not found with the given name",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user details provided",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "User with the same login or email already exists",
                    content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to be updated", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,

            @Parameter(description = "Updated user details")
            @RequestBody @Valid UpdateUserDTO updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @Operation(
            summary = "Update user password",
            description = "Change the password of an authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password updated successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid password details provided",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid old password provided",
                    content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "New password details")
            @RequestBody @Valid UpdatePasswordDTO updatePasswordDTO,
            @AuthenticationPrincipal UserInfoDetails user) {
        userService.updatePassword(user.getId(), updatePasswordDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update user password",
            description = "Change the password of an existing user by ID. Only admin has access to this operation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password updated successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid password details provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content)
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePasswordAsAdmin(
            @Parameter(description = "ID of the user whose password is to be updated", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,
            @Parameter(description = "New password details")
            @RequestBody @Valid UpdatePasswordForAdminDTO updatePasswordDTO) {
        userService.updatePasswordAsAdmin(id, updatePasswordDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete a user",
            description = "Delete a user by their unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to be deleted", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
