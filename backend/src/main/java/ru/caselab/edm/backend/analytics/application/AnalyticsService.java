package ru.caselab.edm.backend.analytics.application;

import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentSigningProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopVotesByParticipants;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    List<TopUsersByDocumentCreationProjection> findTopUsersByDocumentCreation(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopUsersByDocumentSigningProjection> findTopUsersByDocumentSigning(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopUsersByReplacementProjection> findTopUserByReplacement(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopVotesByParticipants> findTopVotesByParticipants(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
