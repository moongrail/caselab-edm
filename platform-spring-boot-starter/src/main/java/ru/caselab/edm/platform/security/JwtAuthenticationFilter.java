package ru.caselab.edm.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates a bearer access token and installs an {@link AuthenticatedUser}
 * principal. No database or remote call is required.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final List<String> publicPaths;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   List<String> publicPaths) {
        this.jwtTokenService = jwtTokenService;
        this.publicPaths = List.copyOf(publicPaths);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || path.isEmpty()) {
            return false;
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String normalizedPath = path;
        return publicPaths.stream()
                .anyMatch(publicPath -> normalizedPath.equals(publicPath)
                        || normalizedPath.startsWith(publicPath + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean bearerPresent = authHeader != null && authHeader.startsWith(BEARER_PREFIX);
        if (bearerPresent
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtClaims claims = jwtTokenService.parseAccessToken(
                        authHeader.substring(BEARER_PREFIX.length()));
                AuthenticatedUser principal = new AuthenticatedUser(
                        claims.userId(), claims.username(), claims.roles(),
                        claims.departmentId(), claims.managedDepartmentId());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtAuthenticationException ex) {
                SecurityContextHolder.clearContext();
                log.debug("JWT authentication failed: {}", ex.getMessage());
                writeUnauthorized(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\"}");
    }
}
