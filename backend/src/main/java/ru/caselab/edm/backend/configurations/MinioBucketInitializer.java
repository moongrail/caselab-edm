package ru.caselab.edm.backend.configurations;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.minio.initialize-bucket", havingValue = "true", matchIfMissing = true)
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;

    @Override
    public void run(ApplicationArguments args) {
        String bucketName = minioConfigProperties.getBucketName();
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket '{}' created", bucketName);
            }
        } catch (Exception ex) {
            // Инфраструктура может быть недоступна при старте: приложение продолжает работу.
            log.warn("MinIO bucket '{}' initialization skipped: {}", bucketName, ex.getMessage());
        }
    }
}
