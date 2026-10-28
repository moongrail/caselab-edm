package ru.caselab.edm.backend.service.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;
import ru.caselab.edm.backend.dto.minio.MinioSaveDto;
import ru.caselab.edm.backend.exceptions.ContentTypeDetectionException;
import ru.caselab.edm.backend.exceptions.MinioServiceException;
import ru.caselab.edm.backend.service.MinioService;
import ru.caselab.edm.backend.service.helpers.InputStreamContentTypeDetector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    private static final Duration DEFAULT_URL_EXPIRY = Duration.ofMinutes(15);
    private static final long PART_SIZE = -1;
    private static final String DEFAULT_MINIO_REGION = "us-east-1";

    private final String bucketName;
    private final MinioClient minioClient;
    private final MinioClient presignClient;
    private final InputStreamContentTypeDetector contentTypeDetector;
    private final Duration presignedUrlExpiry;

    public MinioServiceImpl(MinioConfigProperties minioConfigProperties,
                            MinioClient minioClient,
                            InputStreamContentTypeDetector contentTypeDetector) {
        this.bucketName = minioConfigProperties.getBucketName();
        this.minioClient = minioClient;
        this.contentTypeDetector = contentTypeDetector;
        this.presignedUrlExpiry = minioConfigProperties.getPresignedUrlExpiry() == null
                ? DEFAULT_URL_EXPIRY
                : minioConfigProperties.getPresignedUrlExpiry();
        this.presignClient = buildPresignClient(minioConfigProperties, minioClient);
    }

    @Override
    public void saveObject(MinioSaveDto minioSaveDto) {
        try (InputStream inputStream = new ByteArrayInputStream(minioSaveDto.data())) {
            String contentType = contentTypeDetector.detect(minioSaveDto.objectName(), inputStream);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(minioSaveDto.objectName())
                    .contentType(contentType)
                    .stream(inputStream, inputStream.available(), PART_SIZE)
                    .build();
            minioClient.putObject(putObjectArgs);
            log.info("Object was uploaded successfully: {}", minioSaveDto.objectName());
        } catch (Exception ex) {
            log.error("Exception while uploading object: {}", minioSaveDto.objectName(), ex);
            throw new MinioServiceException("Failed to upload object", ex);
        }
    }

    @Override
    public void deleteByObjectName(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("Object was deleted successfully: {}", objectName);
        } catch (Exception ex) {
            log.error("Exception while deleting object: {}", objectName, ex);
            throw new MinioServiceException("Failed to delete object", ex);
        }
    }

    @Override
    public String generateTemporaryUrlToObject(String objectName) {
        try {
            long expirySeconds = Math.max(1, presignedUrlExpiry.toSeconds());
            return presignClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry((int) Math.min(expirySeconds, TimeUnit.DAYS.toSeconds(7)), TimeUnit.SECONDS)
                    .build());
        } catch (Exception ex) {
            log.error("Exception while generating temporary URL to object: {}", objectName, ex);
            throw new MinioServiceException("Failed to generate temporary URL", ex);
        }
    }

    private MinioClient buildPresignClient(MinioConfigProperties properties, MinioClient defaultClient) {
        String publicEndpoint = properties.getPublicEndpoint();
        if (publicEndpoint == null || publicEndpoint.isBlank()
                || publicEndpoint.equals(properties.getEndpoint())) {
            return defaultClient;
        }
        return MinioClient.builder()
                .endpoint(publicEndpoint)
                // Presigning must not call the public endpoint from inside the network:
                // it is normally reachable only by clients, not by the backend container.
                .region(DEFAULT_MINIO_REGION)
                .credentials(properties.getUsername(), properties.getPassword())
                .build();
    }
}
