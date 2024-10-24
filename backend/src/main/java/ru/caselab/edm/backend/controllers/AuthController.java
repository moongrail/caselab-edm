package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.JwtDTO;
import ru.caselab.edm.backend.dto.LoginUserDTO;
import ru.caselab.edm.backend.service.UserService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Operations for authentication")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully login", content = @Content(schema = @Schema(implementation = JwtDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid login or password", content = @Content)
    })
    @PostMapping
    public ResponseEntity<JwtDTO> authUser(
            @Parameter(description = "User login details")
            @RequestBody LoginUserDTO loginUserDTO) {
        return ResponseEntity.ok(userService.auth(loginUserDTO));
    }
}
