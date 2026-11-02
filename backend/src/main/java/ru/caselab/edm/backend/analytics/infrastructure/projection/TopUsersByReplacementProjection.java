package ru.caselab.edm.backend.analytics.infrastructure.projection;

import java.util.UUID;

public interface TopUsersByReplacementProjection {

    UUID getUserId();
    Long getReplacementCount();
}
