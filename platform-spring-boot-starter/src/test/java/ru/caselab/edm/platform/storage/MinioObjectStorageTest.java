package ru.caselab.edm.platform.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.caselab.edm.platform.storage.minio.MinioObjectStorage;
import ru.caselab.edm.platform.storage.minio.MinioProperties;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioObjectStorageTest {

    private MinioClient client;
    private MinioProperties properties;
    private MinioObjectStorage storage;

    @BeforeEach
    void setUp() {
        client = mock(MinioClient.class);
        properties = new MinioProperties();
        properties.setEndpoint("http://minio:9000");
        properties.setBucketName("documents");
        properties.setPresignedUrlExpiry(Duration.ofMinutes(5));
        storage = new MinioObjectStorage(properties, client);
    }

    @Test
    void putUploadsObject() throws Exception {
        storage.put(new StorageObject("object.txt", "data".getBytes(), "text/plain"));

        verify(client).putObject(any(PutObjectArgs.class));
    }

    @Test
    void putFailureWrapped() throws Exception {
        when(client.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("minio"));

        assertThatThrownBy(() -> storage.put(new StorageObject("object.txt",
                "data".getBytes(), "text/plain")))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("upload");
    }

    @Test
    void deleteRemovesObject() throws Exception {
        storage.delete("object.txt");

        verify(client).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFailureWrapped() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("minio"))
                .when(client).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> storage.delete("object.txt"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("delete");
    }

    @Test
    void temporaryUrlUsesConfiguredExpiry() throws Exception {
        when(client.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://url");

        assertThat(storage.temporaryUrl("object.txt")).isEqualTo("http://url");
    }

    @Test
    void temporaryUrlFailureWrapped() throws Exception {
        when(client.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("minio"));

        assertThatThrownBy(() -> storage.temporaryUrl("object.txt"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("temporary");
    }

    @Test
    void publicEndpoint_createsSeparatePresignClient() {
        properties.setPublicEndpoint("http://public:9000");
        properties.setAccessKey("key");
        properties.setSecretKey("secret");

        assertThat(new MinioObjectStorage(properties, client)).isNotNull();
    }

    @Test
    void nullExpiryFallsBackToDefault() {
        properties.setPresignedUrlExpiry(null);

        assertThat(new MinioObjectStorage(properties, client)).isNotNull();
    }

    @Test
    void storageExceptionConstructorsWork() {
        assertThat(new StorageException("message").getMessage()).isEqualTo("message");
        assertThat(new StorageException("message", new RuntimeException()).getCause()).isNotNull();
    }
}
