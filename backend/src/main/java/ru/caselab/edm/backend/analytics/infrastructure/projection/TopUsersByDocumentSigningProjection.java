package ru.caselab.edm.backend.analytics.infrastructure.projection;

import java.util.UUID;

public interface TopUsersByDocumentSigningProjection {

    UUID getUserId();
    Long getSignatureCount();
}
