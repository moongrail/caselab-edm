package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.JwtDTO;
import ru.caselab.edm.backend.dto.RefreshTokenDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.service.JwtService;
import ru.caselab.edm.backend.service.RefreshTokenService;
import ru.caselab.edm.backend.service.impl.UserDetailsServiceImpl;

@RestController
@RequestMapping("/jwt")
@Tag(name = "JWT", description = "JWT management operation")
public class JwtController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);


    @Autowired
    public JwtController(RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @Operation(
            summary = "Updates JWT token",
            description = "Updates expired/non-valid JWT token for current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT was successfully updated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtDTO.class))),
            @ApiResponse(responseCode = "404", description = "Refresh token was not found",
            content = @Content)
    })
    @PostMapping("/update")
    public ResponseEntity<JwtDTO> updateJwtToken(
            @RequestBody @Valid RefreshTokenDTO refreshTokenDto) throws ResourceNotFoundException {
        User user = refreshTokenService.getUserByToken(refreshTokenDto.refreshToken());

        UserInfoDetails userInfoDetails = new UserInfoDetails(user);

        JwtDTO jwtDto = new JwtDTO(refreshTokenDto.refreshToken(),
                jwtService.generateToken(userInfoDetails));
        return ResponseEntity.ok(jwtDto);
    }
}
