package ru.caselab.edm.backend.repository.projection;

import java.util.UUID;

public interface TopVotesByParticipants {
    Long getProcessId();
    Long getDocumentId();
    Long getParticipantsCount();
}
