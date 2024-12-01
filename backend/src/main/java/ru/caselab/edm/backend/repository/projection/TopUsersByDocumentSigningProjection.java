package ru.caselab.edm.backend.repository.projection;

import java.util.UUID;

public interface TopUsersByDocumentSigningProjection {

    UUID getUserId();
    Long getSignatureCount();
}
