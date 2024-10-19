package ru.caselab.edm.backend.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.configurations.props.MinioConfigProperties;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.exceptions.MinioServiceException;
import ru.caselab.edm.backend.service.helpers.InputStreamContentTypeDetector;
import ru.caselab.edm.backend.service.impl.MinioServiceImpl;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {

    private static final String BUCKET_NAME = "test-bucket";

    private MinioService minioService;
    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioConfigProperties minioConfigProperties;
    @Mock
    private InputStreamContentTypeDetector contentTypeDetector;

    @BeforeEach
    void setUpAndmockPropertiesInvocation() {
        when(minioConfigProperties.getBucketName()).thenReturn(BUCKET_NAME);
        minioService = new MinioServiceImpl(minioConfigProperties, minioClient, contentTypeDetector);
    }

    @Test
    void saveObject_okConditions_shouldUploadSuccessfully() throws Exception {
        MinioSaveDto mockSaveObject = mock(MinioSaveDto.class);
        when(mockSaveObject.data()).thenReturn(new byte[0]);
        when(mockSaveObject.objectName()).thenReturn("test-name");
        when(contentTypeDetector.detect(any(InputStream.class))).thenReturn("text/plain");

        minioService.saveObject(mockSaveObject);

        verify(mockSaveObject).data();
        verify(contentTypeDetector).detect(any(InputStream.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void saveObject_minioClientThrowsException_shouldThrowMinioServiceException() throws Exception {
        MinioSaveDto mockSaveObject = mock(MinioSaveDto.class);

        when(mockSaveObject.data()).thenReturn(new byte[0]);
        when(mockSaveObject.objectName()).thenReturn("test-name");
        when(contentTypeDetector.detect(any(InputStream.class))).thenReturn("text/plain");

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new IOException());

        assertThatThrownBy(() -> minioService.saveObject(mockSaveObject))
                .isInstanceOf(MinioServiceException.class);

        verify(mockSaveObject).data();
        verify(contentTypeDetector).detect(any(InputStream.class));
        verify(mockSaveObject, times(2)).objectName(); //first invocation in put object and second during the logging
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void deleteByObjectName_existsObject_shouldDelete() throws Exception {
        String objectName = "test-object";

        minioService.deleteByObjectName(objectName);

        ArgumentCaptor<RemoveObjectArgs> removeArgsCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(removeArgsCaptor.capture());

        var removeArgs = removeArgsCaptor.getValue();
        assertThat(removeArgs.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(removeArgs.object()).isEqualTo(objectName);
    }

    @Test
    void deleteByObjectName_minioClientThrowsException_shouldThrowMinioServiceException() throws Exception {
        String objectName = "test-object";

        doThrow(new IOException())
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> minioService.deleteByObjectName(objectName))
                .isInstanceOf(MinioServiceException.class);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void generateTemporaryUrlToObject_existsObjectName_shouldGenerateUrl() throws Exception {
        String expectedUrl = "test";
        String objectName = "test-object";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String actualUrl = minioService.generateTemporaryUrlToObject(objectName);
        assertThat(actualUrl).isEqualTo(expectedUrl);

        ArgumentCaptor<GetPresignedObjectUrlArgs> presignedUrlCaptor =
                ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(presignedUrlCaptor.capture());

        var getPresignedObjectUrlArgs = presignedUrlCaptor.getValue();
        assertThat(getPresignedObjectUrlArgs.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(getPresignedObjectUrlArgs.expiry()).isPositive();
        assertThat(getPresignedObjectUrlArgs.method()).isEqualTo(Method.GET);
    }

    @Test
    void generateTemporaryUrlToObject_minioClientThrowsException_shouldThrowMinioServiceException() throws Exception {
        String objectName = "test-object";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new IOException());

        assertThatThrownBy(() -> minioService.generateTemporaryUrlToObject(objectName))
                .isInstanceOf(MinioServiceException.class);

        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
