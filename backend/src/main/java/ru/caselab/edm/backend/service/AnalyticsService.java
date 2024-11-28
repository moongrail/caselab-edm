package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.repository.projection.*;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    List<TopUsersByDocumentCreationProjection> findTopUsersByDocumentCreation(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopVotesByParticipants> findTopVotesByParticipants(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
