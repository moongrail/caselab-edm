package ru.caselab.edm.backend.shared.directory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

class HttpUserDirectoryClientTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String BASE_URL = "http://security:8081/api/v1";

    private MockRestServiceServer server;
    private HttpUserDirectoryClient client;
    private SecurityServiceProperties properties;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        properties = new SecurityServiceProperties(BASE_URL, "secret-token");
        client = new HttpUserDirectoryClient(builder.build(), properties);
    }

    @Test
    void requireUser_returnsProjection() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"id":"%s","login":"alice","email":"alice@example.com",
                         "firstName":"Alice","lastName":"Smith","patronymic":null,
                         "position":"Engineer","departmentId":1,"departmentName":"Dev",
                         "managedDepartmentId":1,"roles":["USER"]}
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        UserSummary user = client.requireUser(USER_ID);

        assertThat(user.id()).isEqualTo(USER_ID);
        assertThat(user.login()).isEqualTo("alice");
        assertThat(user.roles()).containsExactly("USER");
    }

    @Test
    void requireUser_missingUser_mapsToNotFound() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.requireUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(USER_ID.toString());
    }

    @Test
    void requireUser_serverError_mapsToDirectoryException() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.requireUser(USER_ID))
                .isInstanceOf(UserDirectoryException.class)
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);
    }

    @Test
    void findUsers_emptyInputSkipsRemoteCall() {
        assertThat(client.findUsers(List.of())).isEmpty();
        assertThat(client.findUsers(null)).isEmpty();
    }

    @Test
    void findUsers_sendsIdsAndReturnsProjection() {
        server.expect(requestTo(BASE_URL + "/internal/users/by-ids"))
                .andExpect(method(POST))
                .andExpect(content().json("[\"" + USER_ID + "\"]"))
                .andRespond(withSuccess("""
                        [{"id":"%s","login":"alice","email":"alice@example.com",
                          "firstName":"Alice","lastName":"Smith","position":"Engineer","roles":["USER"]}]
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        assertThat(client.findUsers(Set.of(USER_ID))).extracting(UserSummary::id)
                .containsExactly(USER_ID);
    }

    @Test
    void findUsers_serverError_mapsToDirectoryException() {
        server.expect(requestTo(BASE_URL + "/internal/users/by-ids"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.findUsers(List.of(USER_ID)))
                .isInstanceOf(UserDirectoryException.class);
    }

    @Test
    void replacementCandidates_returnsProjection() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID
                        + "/replacement-candidates"))
                .andRespond(withSuccess("""
                        [{"id":"%s","login":"alice","email":"alice@example.com",
                          "firstName":"Alice","lastName":"Smith","position":"Engineer","roles":[]}]
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        assertThat(client.replacementCandidates(USER_ID)).hasSize(1);
    }

    @Test
    void replacementCandidates_missingUser_mapsToNotFound() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID
                        + "/replacement-candidates"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.replacementCandidates(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void replacementCandidates_serverError_mapsToDirectoryException() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID
                        + "/replacement-candidates"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.replacementCandidates(USER_ID))
                .isInstanceOf(UserDirectoryException.class);
    }

    @Test
    void departmentMembers_passesPagingAndExclusions() {
        UUID excluded = UUID.fromString("22222222-2222-2222-2222-222222222222");
        server.expect(requestTo(BASE_URL + "/internal/departments/5/members"
                        + "?page=2&size=10&exclude=" + excluded))
                .andRespond(withSuccess("""
                        {"content":[{"id":"%s","login":"alice","email":"alice@example.com",
                          "firstName":"Alice","lastName":"Smith","position":"Engineer","roles":[]}],
                         "page":2,"size":10,"totalElements":21,"totalPages":3}
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        UserPage page = client.departmentMembers(5L, List.of(excluded), 2, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.number()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(21);
    }

    @Test
    void departmentMembers_withoutExclusions_handlesNullContent() {
        server.expect(requestTo(BASE_URL + "/internal/departments/5/members?page=0&size=5"))
                .andRespond(withSuccess("""
                        {"content":null,"page":0,"size":5,"totalElements":0,"totalPages":0}
                        """, MediaType.APPLICATION_JSON));

        UserPage page = client.departmentMembers(5L, null, 0, 5);

        assertThat(page.content()).isNull();
    }

    @Test
    void departmentMembers_emptyBody_mapsToDirectoryException() {
        server.expect(requestTo(BASE_URL + "/internal/departments/5/members?page=0&size=5"))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> client.departmentMembers(5L, null, 0, 5))
                .isInstanceOf(UserDirectoryException.class);
    }

    @Test
    void departmentMembers_missingDepartment_mapsToNotFound() {
        server.expect(requestTo(BASE_URL + "/internal/departments/5/members?page=0&size=5"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.departmentMembers(5L, null, 0, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void departmentMembers_serverError_mapsToDirectoryException() {
        server.expect(requestTo(BASE_URL + "/internal/departments/5/members?page=0&size=5"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.departmentMembers(5L, null, 0, 5))
                .isInstanceOf(UserDirectoryException.class);
    }

    @Test
    void internalHeaders_includeConfiguredTokenAndAccessorsExposeDependencies() {
        HttpHeaders headers = client.internalHeaders();

        assertThat(headers.getFirst(HttpUserDirectoryClient.INTERNAL_TOKEN_HEADER))
                .isEqualTo("secret-token");
        assertThat(client.properties()).isSameAs(properties);
        assertThat(client.restClient()).isNotNull();
    }

    @Test
    void internalHeaders_omitBlankToken() {
        client = new HttpUserDirectoryClient(RestClient.create(BASE_URL),
                new SecurityServiceProperties(" ", null));

        assertThat(client.internalHeaders()).doesNotContainKey(
                HttpUserDirectoryClient.INTERNAL_TOKEN_HEADER);
        assertThat(client.properties().url()).isEqualTo("http://localhost:8081/api/v1");
    }
}
