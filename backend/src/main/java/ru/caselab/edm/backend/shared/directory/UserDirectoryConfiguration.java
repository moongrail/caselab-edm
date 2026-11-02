package ru.caselab.edm.backend.shared.directory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityServiceProperties.class)
public class UserDirectoryConfiguration {

    @Bean
    public RestClient identityRestClient(SecurityServiceProperties properties) {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.url());
        if (properties.internalToken() != null && !properties.internalToken().isBlank()) {
            builder.defaultHeader(HttpUserDirectoryClient.INTERNAL_TOKEN_HEADER,
                    properties.internalToken());
        }
        return builder.build();
    }

    @Bean
    public UserDirectory userDirectory(RestClient identityRestClient,
                                       SecurityServiceProperties properties) {
        return new HttpUserDirectoryClient(identityRestClient, properties);
    }
}
