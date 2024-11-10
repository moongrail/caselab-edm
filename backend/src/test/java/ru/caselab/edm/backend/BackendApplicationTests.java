package ru.caselab.edm.backend;

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


/**
 *      Объяснения почему так сделал
 *      Контейнер без впн не качается.
 *      Плюс надо его еще настраивать.
 *      Везде где задевает ставьте @MockBean.
 TODO:  На случай если кому то захочется донастроить.
 */
//    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.7.1";

//    @DynamicPropertySource
//    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.elasticsearch.rest.uris", elasticsearchContainer::getHttpHostAddress);
//        registry.add("spring.elasticsearch.username", () -> "user");
//        registry.add("spring.elasticsearch.password", () -> "pass");
//        registry.add("spring.data.elasticsearch.repositories.enabled", () -> "false");
//    }

//    static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
//            .withAccessToHost(true)
//            .withSharedMemorySize(512L)
//            .withPassword("pass");
//
//    static {
//        elasticsearchContainer.start();
//    }
}
