package ru.caselab.edm.backend.repository.projection;

import java.util.UUID;

public interface TopUsersByDocumentCreationProjection {

    UUID getUserId();
    Long getDocumentCount();
}
