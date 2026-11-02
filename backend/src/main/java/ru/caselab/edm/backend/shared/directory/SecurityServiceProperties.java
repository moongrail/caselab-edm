package ru.caselab.edm.backend.shared.directory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.service")
public record SecurityServiceProperties(String url, String internalToken) {

    public SecurityServiceProperties {
        if (url == null || url.isBlank()) {
            url = "http://localhost:8081/api/v1";
        }
    }
}
