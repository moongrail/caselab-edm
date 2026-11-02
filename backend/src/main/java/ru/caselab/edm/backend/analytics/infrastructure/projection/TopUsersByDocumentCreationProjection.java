package ru.caselab.edm.backend.analytics.infrastructure.projection;

import java.util.UUID;

public interface TopUsersByDocumentCreationProjection {

    UUID getUserId();
    Long getDocumentCount();
}
