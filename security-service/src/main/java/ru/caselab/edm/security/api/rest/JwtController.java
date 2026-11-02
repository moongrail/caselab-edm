package ru.caselab.edm.security.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.platform.security.JwtTokenService;
import ru.caselab.edm.security.api.dto.auth.JwtDTO;
import ru.caselab.edm.security.api.dto.auth.RefreshTokenDTO;
import ru.caselab.edm.security.config.JwtClaimsFactory;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.exception.ResourceNotFoundException;
import ru.caselab.edm.security.jwt.RefreshTokenService;

@RestController
@RequestMapping("/jwt")
@Tag(name = "JWT", description = "JWT management operation")
public class JwtController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;
    private final JwtClaimsFactory jwtClaimsFactory;

    public JwtController(RefreshTokenService refreshTokenService,
                         JwtTokenService jwtTokenService,
                         JwtClaimsFactory jwtClaimsFactory) {
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
        this.jwtClaimsFactory = jwtClaimsFactory;
    }

    @Operation(
            summary = "Updates JWT token",
            description = "Updates expired/non-valid JWT token for current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT was successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtDTO.class))),
            @ApiResponse(responseCode = "404", description = "Refresh token was not found",
                    content = @Content)
    })
    @PostMapping("/update")
    @Transactional
    public ResponseEntity<JwtDTO> updateJwtToken(
            @RequestBody @Valid RefreshTokenDTO refreshTokenDto) throws ResourceNotFoundException {
        User user = refreshTokenService.getUserByToken(refreshTokenDto.refreshToken());
        JwtDTO jwtDto = new JwtDTO(user.getId(), refreshTokenDto.refreshToken(),
                jwtTokenService.createAccessToken(jwtClaimsFactory.from(user)));
        return ResponseEntity.ok(jwtDto);
    }
}
