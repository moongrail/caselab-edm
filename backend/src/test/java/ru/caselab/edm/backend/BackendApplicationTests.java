package ru.caselab.edm.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;

@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    private static final String MINIO_IMAGE = "minio/minio:RELEASE.2024-02-17T01-15-57Z.fips";
    private static final String MINIO_ENDPOINT = "minio.config.endpoint";
    private static final String MINIO_USERNAME = "minio.config.username";
    private static final String MINIO_PASSWORD = "minio.config.password";

    static final MinIOContainer minioContainer = new MinIOContainer(MINIO_IMAGE);

    static {
        minioContainer.start();
    }

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add(MINIO_ENDPOINT, minioContainer::getS3URL);
        registry.add(MINIO_USERNAME, minioContainer::getUserName);
        registry.add(MINIO_PASSWORD, minioContainer::getPassword);
    }

    @Test
    public void contextLoads() {
    }
}
