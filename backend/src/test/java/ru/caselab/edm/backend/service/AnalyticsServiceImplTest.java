package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.projection.TopVotesByParticipants;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentSigningProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.service.impl.AnalyticsServiceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ApprovementProcessRepository approvementProcessRepository;
    @Mock
    private TopUsersByDocumentCreationProjection creationProjection;
    @Mock
    private TopUsersByDocumentSigningProjection signingProjection;
    @Mock
    private TopUsersByReplacementProjection replacementProjection;
    @Mock
    private TopVotesByParticipants votesProjection;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private final LocalDate start = LocalDate.of(2026, 1, 1);
    private final LocalDate end = LocalDate.of(2026, 1, 31);
    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void findTopUsersByDocumentCreation_convertsDatesAndReturnsContent() {
        Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atStartOfDay(ZoneId.systemDefault()).toInstant();
        when(userRepository.findTopUserByDocumentCreation(startInstant, endInstant, pageable))
                .thenReturn(new PageImpl<>(List.of(creationProjection)));

        assertThat(analyticsService.findTopUsersByDocumentCreation(start, end, pageable))
                .containsExactly(creationProjection);
        verify(userRepository).findTopUserByDocumentCreation(startInstant, endInstant, pageable);
    }

    @Test
    void findTopUsersByDocumentSigning_usesFullDays() {
        LocalDateTime startDateTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);
        when(userRepository.findTopUserByDocumentSigning(startDateTime, endDateTime, pageable))
                .thenReturn(new PageImpl<>(List.of(signingProjection)));

        assertThat(analyticsService.findTopUsersByDocumentSigning(start, end, pageable))
                .containsExactly(signingProjection);
    }

    @Test
    void findTopUserByReplacement_convertsDatesAndReturnsContent() {
        Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atStartOfDay(ZoneId.systemDefault()).toInstant();
        when(userRepository.findTopUserByReplacement(startInstant, endInstant, pageable))
                .thenReturn(new PageImpl<>(List.of(replacementProjection)));

        assertThat(analyticsService.findTopUserByReplacement(start, end, pageable))
                .containsExactly(replacementProjection);
    }

    @Test
    void findTopVotesByParticipants_usesEndOfLastDay() {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);
        when(approvementProcessRepository.findTopVotesByParticipants(startDateTime, endDateTime, pageable))
                .thenReturn(new PageImpl<>(List.of(votesProjection)));

        assertThat(analyticsService.findTopVotesByParticipants(start, end, pageable))
                .containsExactly(votesProjection);
    }
}
