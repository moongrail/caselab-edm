package ru.caselab.edm.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.caselab.edm.backend.exceptions.ExpiredJwtTokenException;
import ru.caselab.edm.backend.security.filter.JwtFilter;
import ru.caselab.edm.backend.security.service.JwtService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private UserDetails userDetails;

    private JwtFilter filter;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtFilter(jwtService, userDetailsService);
        responseBody = new StringWriter();
        org.mockito.Mockito.lenient().when(response.getWriter())
                .thenReturn(new PrintWriter(responseBody));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_withoutAuthorizationHeader_passesThrough() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getLogin(any());
    }

    @Test
    void doFilter_withNonBearerHeader_passesThrough() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withExistingAuthentication_passesThrough() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of()));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getLogin(any());
    }

    @Test
    void doFilter_withValidToken_setsAuthentication() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        when(jwtService.getLogin("token")).thenReturn("owner");
        when(userDetailsService.loadUserByUsername("owner")).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("owner");
        when(jwtService.validateToken("token", userDetails)).thenReturn(true);
        org.mockito.Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(userDetails).getAuthorities();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("owner");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withExpiredTokenOnRefreshEndpoint_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/jwt/update");
        when(request.getContextPath()).thenReturn("/api/v1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getLogin(any());
    }

    @Test
    void doFilter_onAuthEndpoint_ignoresAuthorizationHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth");
        when(request.getContextPath()).thenReturn("/api/v1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getLogin(any());
    }

    @Test
    void doFilter_withExpiredToken_returns401() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer expired");
        when(jwtService.getLogin("expired")).thenThrow(new ExpiredJwtTokenException("expired"));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(responseBody.toString()).contains("Unauthorized");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_withUnknownUser_returns401() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        when(jwtService.getLogin("token")).thenReturn("missing");
        when(userDetailsService.loadUserByUsername("missing"))
                .thenThrow(new UsernameNotFoundException("not found"));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_withInvalidToken_returns401() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        when(jwtService.getLogin("token")).thenThrow(new IllegalArgumentException("bad token"));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_whenResponseAlreadyCommitted_doesNotWriteAgain() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        when(jwtService.getLogin("token")).thenThrow(new IllegalArgumentException("bad token"));
        when(response.isCommitted()).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(response, never()).getWriter();
        verify(filterChain, never()).doFilter(request, response);
    }
}
