package ru.caselab.edm.backend.shared.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceAuthorizationServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void delegatesToSupportingPolicy() {
        ResourceAccessPolicy policy = mock(ResourceAccessPolicy.class);
        when(policy.supports("document")).thenReturn(true);
        ResourceAccessRequest request = request("document", "42", ResourceAction.READ);

        new ResourceAuthorizationServiceImpl(List.of(policy)).verify(request);

        verify(policy).verify(request);
    }

    @Test
    void skipsNonSupportingPolicies() {
        ResourceAccessPolicy first = mock(ResourceAccessPolicy.class);
        ResourceAccessPolicy second = mock(ResourceAccessPolicy.class);
        when(first.supports("document")).thenReturn(false);
        when(second.supports("document")).thenReturn(true);
        ResourceAccessRequest request = request("document", "42", ResourceAction.WRITE);

        assertThatCode(() -> new ResourceAuthorizationServiceImpl(List.of(first, second))
                .verify(request)).doesNotThrowAnyException();

        verify(second).supports("document");
        verify(second).verify(request);
    }

    @Test
    void failsClosedWhenNoPolicyIsRegistered() {
        ResourceRoutingPolicy policy = new ResourceRoutingPolicy();

        assertThatThrownBy(() -> new ResourceAuthorizationServiceImpl(List.of(policy))
                .verify(request("unknown", "1", ResourceAction.READ)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void rejectsRequestWithoutOwner() {
        assertThatThrownBy(() -> new ResourceAccessRequest("document", "1", null, ResourceAction.READ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("userId must not be null");
    }

    private static ResourceAccessRequest request(String type, String id, ResourceAction action) {
        return new ResourceAccessRequest(type, id, USER_ID, action);
    }

    private static final class ResourceRoutingPolicy implements ResourceAccessPolicy {

        @Override
        public boolean supports(String resourceType) {
            return "document".equals(resourceType);
        }

        @Override
        public void verify(ResourceAccessRequest request) {
            assertThat(request.action()).isNotNull();
        }
    }
}
