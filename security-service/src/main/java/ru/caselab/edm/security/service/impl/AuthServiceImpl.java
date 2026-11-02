package ru.caselab.edm.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.platform.security.JwtTokenService;
import ru.caselab.edm.security.api.dto.auth.JwtDTO;
import ru.caselab.edm.security.api.dto.auth.LoginUserDTO;
import ru.caselab.edm.security.config.JwtClaimsFactory;
import ru.caselab.edm.security.domain.UserInfoDetails;
import ru.caselab.edm.security.jwt.RefreshTokenService;
import ru.caselab.edm.security.service.AuthService;

@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtClaimsFactory jwtClaimsFactory;

    @Transactional
    @Override
    public JwtDTO authenticate(LoginUserDTO loginUserDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDTO.login(), loginUserDTO.password()));
        UserInfoDetails principal = (UserInfoDetails) authentication.getPrincipal();

        String refreshToken = refreshTokenService.createRefreshToken(principal.getUsername()).getToken();
        String accessToken = jwtTokenService.createAccessToken(jwtClaimsFactory.from(principal));
        log.info("User {} authenticated successfully", principal.getUsername());
        return new JwtDTO(principal.getId(), refreshToken, accessToken);
    }
}
