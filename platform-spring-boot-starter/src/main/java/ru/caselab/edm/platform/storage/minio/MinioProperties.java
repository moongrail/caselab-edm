package ru.caselab.edm.platform.storage.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * MinIO connection settings.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.storage.minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";
    private String publicEndpoint = "";
    private String accessKey;
    private String secretKey;
    private String bucketName = "documents";
    private Duration presignedUrlExpiry = Duration.ofMinutes(15);
    private boolean initializeBucket = true;
}
