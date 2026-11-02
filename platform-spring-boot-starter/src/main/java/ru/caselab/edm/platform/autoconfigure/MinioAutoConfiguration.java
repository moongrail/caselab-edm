package ru.caselab.edm.platform.autoconfigure;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.minio.MinioObjectStorage;
import ru.caselab.edm.platform.storage.minio.MinioProperties;

/**
 * MinIO object storage integration. The integration is optional: a service
 * that does not put {@code minio} on its classpath is unaffected.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStorage.class)
    public ObjectStorage objectStorage(MinioProperties properties, MinioClient minioClient) {
        return new MinioObjectStorage(properties, minioClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "platform.storage.minio", name = "initialize-bucket",
            havingValue = "true", matchIfMissing = true)
    public ApplicationRunner minioBucketInitializer(MinioClient client, MinioProperties properties) {
        return args -> {
            boolean exists = client.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucketName())
                    .build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucketName())
                        .build());
                log.info("Created MinIO bucket {}", properties.getBucketName());
            }
        };
    }
}
