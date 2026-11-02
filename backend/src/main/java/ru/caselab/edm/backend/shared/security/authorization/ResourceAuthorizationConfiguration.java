package ru.caselab.edm.backend.shared.security.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class ResourceAuthorizationConfiguration {

    @Bean
    public ResourceAuthorizationService resourceAuthorizationService(
            List<ResourceAccessPolicy> policies) {
        return new ResourceAuthorizationServiceImpl(policies);
    }
}
