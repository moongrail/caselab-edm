package ru.caselab.edm.backend.repository.projection;

import java.util.UUID;

public interface TopUsersByReplacementProjection {

    UUID getUserId();
    Long getReplacementCount();
}
