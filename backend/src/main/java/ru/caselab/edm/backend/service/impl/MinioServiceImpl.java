package ru.caselab.edm.backend.service.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.exceptions.ContentTypeDetectionException;
import ru.caselab.edm.backend.exceptions.MinioServiceException;
import ru.caselab.edm.backend.service.MinioService;
import ru.caselab.edm.backend.service.helpers.InputStreamContentTypeDetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    private static final int DEFAULT_DURATION_MINUTES = 15;
    private static final long PART_SIZE = -1;

    private final String bucketName;
    private final MinioClient minioClient;
    private final InputStreamContentTypeDetector contentTypeDetector;

    public MinioServiceImpl(MinioConfigProperties minioConfigProperties,
                        MinioClient minioClient,
                        InputStreamContentTypeDetector contentTypeDetector) {
        this.bucketName = minioConfigProperties.getBucketName();
        this.minioClient = minioClient;
        this.contentTypeDetector = contentTypeDetector;
    }

    public void saveObject(MinioSaveDto minioSaveDto) {
        try (InputStream inputStream = getInputStream(minioSaveDto)) {
            String contentType = getContentType(inputStream);
            putObject(minioSaveDto.objectName(), contentType, inputStream);
            log.info("Object was uploaded successfully: {}", minioSaveDto.objectName());
        } catch (Exception ex) {
            log.warn("Exception while uploading object: {}", minioSaveDto.objectName(), ex);
            throw new MinioServiceException("Failed to upload object", ex);
        }
    }

    public void deleteByObjectName(String objectName) {
        try {
            removeObject(objectName);
            log.info("Object was deleted successfully: {}", objectName);
        } catch (Exception ex) {
            log.warn("Exception while deleting object: {}", objectName, ex);
            throw new MinioServiceException("Failed to delete object", ex);
        }
    }

    public String generateTemporaryUrlToObject(String objectName) {
        try {
            return getPresignedObjectUrlArgs(objectName);
        } catch (Exception ex) {
            log.warn("Exception while generating temporary URL to object: {}", objectName, ex);
            throw new MinioServiceException("Failed to generate temporary URL", ex);
        }
    }

    private InputStream getInputStream(MinioSaveDto minioSaveDto) {
        return new ByteArrayInputStream(minioSaveDto.data());
    }
    private String getContentType(InputStream inputStream) throws ContentTypeDetectionException {
        return contentTypeDetector.detect(inputStream);
    }

    private void putObject(String objectName, String contentType, InputStream inputStream) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        var putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .contentType(contentType)
                .stream(inputStream, inputStream.available(), PART_SIZE)
                .build();

        minioClient.putObject(putObjectArgs);
    }

    private void removeObject(String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        var removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        minioClient.removeObject(removeObjectArgs);
    }

    private String getPresignedObjectUrlArgs(String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        var getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(DEFAULT_DURATION_MINUTES)
                .build();

        return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }
}
