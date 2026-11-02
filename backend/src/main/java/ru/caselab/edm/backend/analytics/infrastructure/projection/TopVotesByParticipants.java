package ru.caselab.edm.backend.analytics.infrastructure.projection;


import java.util.UUID;

public interface TopVotesByParticipants {
    Long getProcessId();
    Long getDocumentId();
    Long getParticipantsCount();
}