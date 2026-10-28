package ru.caselab.edm.backend.configurations.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры первичного администратора. Пароль передаётся только через окружение.
 */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminBootstrapProperties(
        boolean enabled,
        String login,
        String email,
        String password,
        String firstName,
        String lastName,
        String position
) {
}
