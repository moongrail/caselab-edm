package ru.caselab.edm.backend.configurations.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Component
public class MinioConfigProperties {

    private final String endpoint;
    private final String publicEndpoint;
    private final String username;
    private final String password;
    private final String bucketName;
    private final Duration presignedUrlExpiry;

    public MinioConfigProperties(@Value("${minio.config.endpoint}") String endpoint,
                                 @Value("${minio.config.public-endpoint:}") String publicEndpoint,
                                 @Value("${minio.config.username}") String username,
                                 @Value("${minio.config.password}") String password,
                                 @Value("${minio.config.bucket-name:documents}") String bucketName,
                                 @Value("${minio.config.presigned-url-expiry:15m}") Duration presignedUrlExpiry) {
        this.endpoint = endpoint;
        this.publicEndpoint = publicEndpoint;
        this.username = username;
        this.password = password;
        this.bucketName = bucketName;
        this.presignedUrlExpiry = presignedUrlExpiry;
    }
}
