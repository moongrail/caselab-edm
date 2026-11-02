package ru.caselab.edm.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.security.internal.InternalApiProperties;
import ru.caselab.edm.security.internal.InternalTokenFilter;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SecurityServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsAndInternalApiIsWiredExactlyOnce() {
        assertThat(applicationContext.getBeansOfType(InternalApiProperties.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(InternalTokenFilter.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(SecurityFilterChain.class)).isNotEmpty();
    }
}
