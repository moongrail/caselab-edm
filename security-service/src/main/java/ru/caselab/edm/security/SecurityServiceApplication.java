package ru.caselab.edm.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Identity and organization microservice.
 *
 * <p>The service owns accounts, roles, departments and refresh tokens. Other
 * services only validate the access tokens it issues; they never read this
 * service's database.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SecurityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityServiceApplication.class, args);
    }
}
