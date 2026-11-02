package ru.caselab.edm.platform.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtTokenService tokenService;
    private JwtAuthenticationFilter filter;
    private FilterChain chain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        tokenService = mock(JwtTokenService.class);
        filter = new JwtAuthenticationFilter(tokenService, List.of("/auth", "/jwt/update"));
        chain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withoutBearerToken_passesThrough() throws Exception {
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(tokenService, never()).parseAccessToken(any());
    }

    @Test
    void publicPath_passesThroughEvenWithHeader() throws Exception {
        request.setRequestURI("/api/v1/auth");
        request.setContextPath("/api/v1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(tokenService, never()).parseAccessToken(any());
    }

    @Test
    void existingAuthentication_passesThrough() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void validToken_installsAuthenticatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(tokenService.parseAccessToken("token")).thenReturn(
                new JwtClaims(id, "owner", Set.of("USER"), 1L, 2L));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new AuthenticatedUser(id, "owner", Set.of("USER"), 1L, 2L));
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidToken_returnsUnauthorizedAndStopsChain() throws Exception {
        when(tokenService.parseAccessToken("bad"))
                .thenThrow(new JwtAuthenticationException("bad"));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid or expired token");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void committedResponse_isNotWrittenAgain() throws Exception {
        when(tokenService.parseAccessToken("bad"))
                .thenThrow(new JwtAuthenticationException("bad"));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        response.setCommitted(true);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }
}
