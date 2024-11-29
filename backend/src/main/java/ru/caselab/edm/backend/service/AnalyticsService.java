package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.repository.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentSigningProjection;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    List<TopUsersByDocumentCreationProjection> findTopUsersByDocumentCreation(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopUsersByDocumentSigningProjection> findTopUsersByDocumentSigning(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<TopUsersByReplacementProjection> findTopUserByReplacement(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
