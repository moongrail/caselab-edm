package ru.caselab.edm.security.internal;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalUserControllerTest {

    private final InternalUserService service = mock(InternalUserService.class);
    private final InternalUserController controller = new InternalUserController(service);

    @Test
    void delegatesAllDirectoryEndpoints() {
        UUID id = UUID.randomUUID();
        InternalUserDTO dto = new InternalUserDTO(id, "login", "mail", "first", "last",
                null, "position", 1L, "department", 2L, java.util.Set.of());
        InternalUserPageDTO page = new InternalUserPageDTO(List.of(dto), 0, 10, 1, 1);
        when(service.get(id)).thenReturn(dto);
        when(service.getByIds(List.of(id))).thenReturn(List.of(dto));
        when(service.replacementCandidates(id)).thenReturn(List.of(dto));
        when(service.departmentMembers(1L, List.of(id), 0, 10)).thenReturn(page);
        when(service.departmentManager(1L)).thenReturn(dto);

        assertThat(controller.getUser(id).getBody()).isSameAs(dto);
        assertThat(controller.getUsers(List.of(id)).getBody()).containsExactly(dto);
        assertThat(controller.replacementCandidates(id).getBody()).containsExactly(dto);
        assertThat(controller.departmentMembers(1L, List.of(id), 0, 10).getBody()).isSameAs(page);
        assertThat(controller.departmentManager(1L).getBody()).isSameAs(dto);
    }
}
