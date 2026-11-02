package ru.caselab.edm.platform.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Public HTTP surface and CORS configuration shared by all services.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.security")
public class SecurityProperties {

    @NotEmpty
    private List<String> publicPaths = List.of(
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/error"
    );

    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of();
        private List<String> allowedMethods =
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders =
                List.of("Authorization", "Content-Type", "Accept", "Origin");
        private List<String> exposedHeaders = List.of("Location");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;
    }
}
