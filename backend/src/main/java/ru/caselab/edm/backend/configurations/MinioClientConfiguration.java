package ru.caselab.edm.backend.configurations;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;

@Configuration
public class MinioClientConfiguration {

    @Bean
    public MinioClient minioClient(MinioConfigProperties minioConfigProperties) {
        return MinioClient.builder()
                .endpoint(minioConfigProperties.getEndpoint())
                .credentials(minioConfigProperties.getUsername(), minioConfigProperties.getPassword())
                .build();
    }
}
