package ru.caselab.edm.backend.configurations.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MinioConfigProperties {

    private final String endpoint;
    private final String username;
    private final String password;
    private final String bucketName;

    public MinioConfigProperties(@Value("${minio.config.endpoint}") String endpoint,
                                 @Value("${minio.config.username}") String username,
                                 @Value("${minio.config.password}") String password,
                                 @Value("${minio.bucket.name}") String bucketName) {
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.bucketName = bucketName;
    }
}
