package ru.caselab.edm.backend.configurations.props;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Настройки JWT. Секрет передаётся только через окружение и не хранится в репозитории.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    @NotBlank(message = "security.jwt.secret must be configured (JWT_SECRET)")
    private String secret;

    @NotNull
    private Duration accessTokenTtl = Duration.ofHours(1);

    @NotNull
    private Duration refreshTokenTtl = Duration.ofDays(30);
}
