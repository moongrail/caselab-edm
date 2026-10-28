package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.caselab.edm.backend.dto.auth.JwtDTO;
import ru.caselab.edm.backend.dto.auth.LoginUserDTO;
import ru.caselab.edm.backend.entity.RefreshToken;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.security.service.RefreshTokenService;
import ru.caselab.edm.backend.service.impl.AuthServiceImpl;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticate_returnsTokensAndUserId() {
        User user = user(OWNER_ID, "owner");
        user.setRoles(new HashSet<>());
        UserInfoDetails principal = new UserInfoDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(refreshTokenService.createRefreshToken("owner")).thenReturn(refreshToken);
        when(jwtService.generateToken(principal)).thenReturn("access-token");

        JwtDTO result = authService.authenticate(new LoginUserDTO("owner", "password"));

        assertThat(result.userId()).isEqualTo(OWNER_ID);
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.jwt()).isEqualTo("access-token");
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("owner");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password");
    }
}
