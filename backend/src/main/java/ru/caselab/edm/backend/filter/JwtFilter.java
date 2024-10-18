package ru.caselab.edm.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.caselab.edm.backend.security.service.JwtService;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userService;

    @Autowired
    public JwtFilter(UserDetailsService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String login = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                LOGGER.debug(authHeader.substring(7));
                token = authHeader.substring(7);
                login = jwtService.getLogin(token);
                System.out.println("JWT: " + token);
                System.out.println("Login: " + login);
                System.out.println("JWT expiration: " + jwtService.getExpiration(token));
            }

            if (login != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userService.loadUserByUsername(login);
                if (jwtService.validateToken(token, user)) {
                    System.out.println("JWT is valid");
                    LOGGER.info(String.valueOf(user.getAuthorities()));
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

            }

            LOGGER.info(response.getStatus() + " - Response status");
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            LOGGER.debug(ex.getMessage());
        }
    }
}
