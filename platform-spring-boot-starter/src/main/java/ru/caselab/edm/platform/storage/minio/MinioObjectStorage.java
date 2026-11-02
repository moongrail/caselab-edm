package ru.caselab.edm.platform.storage.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.StorageException;
import ru.caselab.edm.platform.storage.StorageObject;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MinioObjectStorage implements ObjectStorage {

    private static final Duration DEFAULT_URL_EXPIRY = Duration.ofMinutes(15);
    private static final long PART_SIZE = -1;
    private static final String DEFAULT_MINIO_REGION = "us-east-1";

    private final String bucketName;
    private final MinioClient minioClient;
    private final MinioClient presignClient;
    private final Duration presignedUrlExpiry;

    public MinioObjectStorage(MinioProperties properties, MinioClient minioClient) {
        this.bucketName = properties.getBucketName();
        this.minioClient = minioClient;
        this.presignedUrlExpiry = properties.getPresignedUrlExpiry() == null
                ? DEFAULT_URL_EXPIRY
                : properties.getPresignedUrlExpiry();
        this.presignClient = buildPresignClient(properties, minioClient);
    }

    @Override
    public void put(StorageObject object) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(object.data())) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(object.objectName())
                    .contentType(object.contentType())
                    .stream(inputStream, inputStream.available(), PART_SIZE)
                    .build();
            minioClient.putObject(args);
            log.info("Object was uploaded successfully: {}", safe(object.objectName()));
        } catch (Exception ex) {
            log.error("Exception while uploading object: {}", safe(object.objectName()), ex);
            throw new StorageException("Failed to upload object", ex);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("Object was deleted successfully: {}", safe(objectName));
        } catch (Exception ex) {
            log.error("Exception while deleting object: {}", safe(objectName), ex);
            throw new StorageException("Failed to delete object", ex);
        }
    }

    @Override
    public String temporaryUrl(String objectName) {
        try {
            long expirySeconds = Math.max(1, presignedUrlExpiry.toSeconds());
            return presignClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry((int) Math.min(expirySeconds, TimeUnit.DAYS.toSeconds(7)),
                            TimeUnit.SECONDS)
                    .build());
        } catch (Exception ex) {
            log.error("Exception while generating temporary URL to object: {}",
                    safe(objectName), ex);
            throw new StorageException("Failed to generate temporary URL", ex);
        }
    }

    private static String safe(String value) {
        return value.replace("\r", "").replace("\n", "");
    }

    private static MinioClient buildPresignClient(MinioProperties properties,
                                                   MinioClient defaultClient) {
        String publicEndpoint = properties.getPublicEndpoint();
        if (publicEndpoint == null || publicEndpoint.isBlank()
                || publicEndpoint.equals(properties.getEndpoint())) {
            return defaultClient;
        }
        return MinioClient.builder()
                .endpoint(publicEndpoint)
                .region(DEFAULT_MINIO_REGION)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
