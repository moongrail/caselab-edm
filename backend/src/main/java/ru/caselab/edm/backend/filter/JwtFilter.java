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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.caselab.edm.backend.service.JwtService;
import ru.caselab.edm.backend.service.impl.JwtServiceImpl;
import ru.caselab.edm.backend.service.impl.UserDetailsServiceImpl;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    public JwtFilter(UserDetailsServiceImpl userService, JwtServiceImpl jwtService) {
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

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            LOGGER.debug(ex.getMessage());
        }
    }
}
