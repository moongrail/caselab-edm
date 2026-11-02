package ru.caselab.edm.backend.shared.directory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Anti-corruption port for the identity service.
 */
public interface UserDirectory {

    UserSummary requireUser(UUID id);

    List<UserSummary> findUsers(Collection<UUID> ids);

    List<UserSummary> replacementCandidates(UUID userId);

    UserPage departmentMembers(Long departmentId, Collection<UUID> exclude,
                               int page, int size);
}
