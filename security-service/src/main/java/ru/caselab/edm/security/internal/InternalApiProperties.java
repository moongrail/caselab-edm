package ru.caselab.edm.security.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.internal")
public record InternalApiProperties(String token) {
}
