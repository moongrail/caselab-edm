package ru.caselab.edm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Application entry point composing the platform starter with the business
 * bounded contexts.
 *
 * <p>The persistence model spans the document, approval and delegation
 * contexts. Identity data is owned by the security service and is accessed
 * only through its internal HTTP directory API.</p>
 */
@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
@EntityScan({
        "ru.caselab.edm.backend.document.domain",
        "ru.caselab.edm.backend.approval.domain",
        "ru.caselab.edm.backend.delegation.domain"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
