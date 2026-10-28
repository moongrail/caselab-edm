package ru.caselab.edm.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;

@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    @MockBean
    private AttributeSearchRepository attributeSearchRepository;

    @Test
    void contextLoads() {
        // Контекст поднимается без Docker: Elasticsearch и MinIO не обязательны для старта.
    }
}
