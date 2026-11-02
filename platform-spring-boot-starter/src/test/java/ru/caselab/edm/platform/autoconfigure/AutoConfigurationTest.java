package ru.caselab.edm.platform.autoconfigure;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import ru.caselab.edm.platform.search.SearchProperties;
import ru.caselab.edm.platform.search.SearchReindexer;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.minio.MinioProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoConfigurationTest {

    @Test
    void minioBeansAndBucketInitializer() throws Exception {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("key");
        properties.setSecretKey("secret");
        properties.setBucketName("documents");
        MinioAutoConfiguration configuration = new MinioAutoConfiguration();

        MinioClient client = configuration.minioClient(properties);
        ObjectStorage storage = configuration.objectStorage(properties, client);
        ApplicationRunner runner = configuration.minioBucketInitializer(client, properties);

        assertThat(storage).isNotNull();
        assertThat(runner).isNotNull();
    }

    @Test
    void missingBucket_isCreated() throws Exception {
        MinioClient client = mock(MinioClient.class);
        MinioProperties properties = new MinioProperties();
        properties.setBucketName("documents");
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        new MinioAutoConfiguration().minioBucketInitializer(client, properties).run(null);

        verify(client).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void existingBucket_isNotCreated() throws Exception {
        MinioClient client = mock(MinioClient.class);
        MinioProperties properties = new MinioProperties();
        properties.setBucketName("documents");
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        new MinioAutoConfiguration().minioBucketInitializer(client, properties).run(null);

        verify(client, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void searchRunnerInvokesAllReindexers() throws Exception {
        SearchReindexer first = mock(SearchReindexer.class);
        SearchReindexer second = mock(SearchReindexer.class);

        ApplicationRunner runner = new ElasticsearchAutoConfiguration()
                .searchReindexRunner(List.of(first, second));
        runner.run(null);

        verify(first).reindex();
        verify(second).reindex();
    }

    @Test
    void searchPropertiesAreExposed() {
        SearchProperties properties = new SearchProperties();
        properties.setReindexOnStartup(true);

        assertThat(properties.isReindexOnStartup()).isTrue();
    }
}
