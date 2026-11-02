package ru.caselab.edm.platform.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Shared JWT settings for token issuers and resource servers.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "platform.security.jwt")
public class JwtProperties {

    @NotBlank(message = "platform.security.jwt.secret must be configured (JWT_SECRET)")
    private String secret;

    @NotNull
    private Duration accessTokenTtl = Duration.ofHours(1);

    @NotNull
    private Duration refreshTokenTtl = Duration.ofDays(30);
}
