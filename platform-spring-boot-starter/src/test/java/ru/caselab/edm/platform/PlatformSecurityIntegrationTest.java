package ru.caselab.edm.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.platform.security.JwtClaims;
import ru.caselab.edm.platform.security.JwtTokenService;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PlatformTestApplication.class)
@AutoConfigureMockMvc
@Import(PlatformSecurityIntegrationTest.TestController.class)
@TestPropertySource(properties = {
        "platform.security.jwt.secret=dGVzdC1zZWNyZXQtdmFsdWUtd2l0aC1hdC1sZWFzdC0zMi1ieXRlcw==",
        "platform.security.public-paths=/public",
        "platform.security.cors.allowed-origins=https://example.test",
        "platform.storage.minio.initialize-bucket=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration,"
        + "ru.caselab.edm.platform.autoconfigure.MinioAutoConfiguration"
})
class PlatformSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void publicEndpointIsAccessible() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk());
    }

    @Test
    void privateEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenProvidesPrincipalAndAuthorities() throws Exception {
        UUID id = UUID.randomUUID();
        String token = jwtTokenService.createAccessToken(
                new JwtClaims(id, "owner", Set.of("USER"), 1L, 2L));

        mockMvc.perform(get("/private").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/private").header(HttpHeaders.AUTHORIZATION, "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightIsAnswered() throws Exception {
        mockMvc.perform(options("/private")
                        .header(HttpHeaders.ORIGIN, "https://example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://example.test"));
    }

    @RestController
    static class TestController {

        @GetMapping("/public")
        String publicEndpoint() {
            return "public";
        }

        @GetMapping("/private")
        @PreAuthorize("hasRole('USER')")
        String privateEndpoint(@AuthenticationPrincipal AuthenticatedUser user) {
            return user.id().toString();
        }
    }
}
