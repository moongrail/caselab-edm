package ru.caselab.edm.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.approval.api.ApprovalController;
import ru.caselab.edm.backend.document.api.DocumentController;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.integration.search.AttributeSearchRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    @MockBean
    private AttributeSearchRepository attributeSearchRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void backendModuleIsAutoConfigured() {
        assertThat(applicationContext.getBeansOfType(DocumentController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(ApprovalController.class)).hasSize(1);
        assertThat(applicationContext.getBean(AuthorizationService.class)).isNotNull();
        assertThat(applicationContext.getBeanNamesForType(SecurityFilterChain.class))
                .contains("securityFilterChain");
    }
}
