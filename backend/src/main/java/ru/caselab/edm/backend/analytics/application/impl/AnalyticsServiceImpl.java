package ru.caselab.edm.backend.analytics.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.analytics.infrastructure.AnalyticsRepository;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementProcessRepository;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopVotesByParticipants;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByDocumentSigningProjection;
import ru.caselab.edm.backend.analytics.infrastructure.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.analytics.application.AnalyticsService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final ApprovementProcessRepository approvementProcessRepository;

    @Override
    public List<TopUsersByDocumentCreationProjection> findTopUsersByDocumentCreation(LocalDate startDate,
                                                                                     LocalDate endDate,
                                                                                     Pageable pageable) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return analyticsRepository.findTopUserByDocumentCreation(startInstant, endInstant, pageable)
                .getContent();
    }

    @Override
    public List<TopUsersByDocumentSigningProjection> findTopUsersByDocumentSigning(LocalDate startDate, LocalDate endDate, Pageable pageable) {

        LocalDateTime startDateWithTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateWithTime = LocalDateTime.of(endDate, LocalTime.MAX);
        return analyticsRepository.findTopUserByDocumentSigning(startDateWithTime, endDateWithTime, pageable).getContent();
    }

    @Override
    public List<TopUsersByReplacementProjection> findTopUserByReplacement(LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return analyticsRepository.findTopUserByReplacement(startInstant, endInstant, pageable).getContent();
    }

    @Override
    public List<TopVotesByParticipants> findTopVotesByParticipants(LocalDate startDate, LocalDate endDate, Pageable pageable) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return approvementProcessRepository.findTopVotesByParticipants(startDateTime, endDateTime, pageable)
                .getContent();
    }
}
