package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.auth.JwtDTO;
import ru.caselab.edm.backend.dto.auth.LoginUserDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.security.service.RefreshTokenService;
import ru.caselab.edm.backend.service.AuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    @Override
    public JwtDTO authenticate(LoginUserDTO loginUserDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDTO.login(), loginUserDTO.password()));
        UserInfoDetails principal = (UserInfoDetails) authentication.getPrincipal();

        String refreshToken = refreshTokenService.createRefreshToken(principal.getUsername()).getToken();
        String accessToken = jwtService.generateToken(principal);
        log.info("User {} authenticated successfully", principal.getUsername());
        return new JwtDTO(principal.getId(), refreshToken, accessToken);
    }
}
