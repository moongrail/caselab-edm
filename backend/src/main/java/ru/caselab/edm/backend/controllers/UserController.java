package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "User management operations")
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
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
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to be updated", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,

            @Parameter(description = "Updated user details")
            @RequestBody @Valid UpdateUserDTO updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @Operation(
            summary = "Update user password",
            description = "Change the password of an existing user by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password updated successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid password details provided",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid old password provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content)
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "ID of the user whose password is to be updated", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,

            @Parameter(description = "New password details")
            @RequestBody @Valid UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(id, updatePasswordDTO);
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
