package ru.caselab.edm.backend.configurations;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Configuration
public class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;


    @PostConstruct
    public void initializeBucket() {
        try {
            String bucketName = minioConfigProperties.getBucketName();

            if (!isBucketExists(bucketName)) {
                createBucket(bucketName);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isBucketExists(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    private void createBucket(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }
}
