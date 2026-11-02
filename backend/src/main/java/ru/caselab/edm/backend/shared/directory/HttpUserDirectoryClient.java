package ru.caselab.edm.backend.shared.directory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * HTTP adapter for the identity service's internal directory API.
 */
public class HttpUserDirectoryClient implements UserDirectory {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final SecurityServiceProperties properties;

    public HttpUserDirectoryClient(RestClient restClient,
                                   SecurityServiceProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public UserSummary requireUser(UUID id) {
        try {
            return restClient.get()
                    .uri("/internal/users/{id}", id)
                    .retrieve()
                    .body(UserSummary.class);
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("User with ID = %s not found".formatted(id));
            }
            throw new UserDirectoryException("Identity service request failed", ex);
        }
    }

    @Override
    public List<UserSummary> findUsers(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            return restClient.post()
                    .uri("/internal/users/by-ids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.copyOf(ids))
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<UserSummary>>() {
                    });
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            throw new UserDirectoryException("Identity service request failed", ex);
        }
    }

    @Override
    public List<UserSummary> replacementCandidates(UUID userId) {
        try {
            return restClient.get()
                    .uri("/internal/users/{id}/replacement-candidates", userId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<UserSummary>>() {
                    });
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Replacement candidates not found for user %s".formatted(userId));
            }
            throw new UserDirectoryException("Identity service request failed", ex);
        }
    }

    @Override
    public UserPage departmentMembers(Long departmentId, Collection<UUID> exclude,
                                      int page, int size) {
        try {
            InternalPage response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/internal/departments/{departmentId}/members")
                                .queryParam("page", page)
                                .queryParam("size", size);
                        if (exclude != null) {
                            exclude.forEach(id -> uriBuilder.queryParam("exclude", id));
                        }
                        return uriBuilder.build(departmentId);
                    })
                    .retrieve()
                    .body(InternalPage.class);
            if (response == null) {
                throw new UserDirectoryException("Identity service returned an empty page");
            }
            return new UserPage(response.content(), response.page(), response.size(),
                    response.totalElements(), response.totalPages());
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Department with ID = %s not found".formatted(departmentId));
            }
            throw new UserDirectoryException("Identity service request failed", ex);
        }
    }

    public RestClient restClient() {
        return restClient;
    }

    public SecurityServiceProperties properties() {
        return properties;
    }

    public HttpHeaders internalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (properties.internalToken() != null && !properties.internalToken().isBlank()) {
            headers.set(INTERNAL_TOKEN_HEADER, properties.internalToken());
        }
        return headers;
    }

    private record InternalPage(
            List<UserSummary> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
