package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.service.impl.AnalyticsServiceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Mock
    private UserRepository userRepository;

    @Test
    void findTopUsersByDocumentCreation_withUsers_shouldReturnUserProjections() {
        // Prepare
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Pageable pageable = PageRequest.of(0, 10);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        TopUsersByDocumentCreationProjection first = mock(TopUsersByDocumentCreationProjection.class);
        TopUsersByDocumentCreationProjection second = mock(TopUsersByDocumentCreationProjection.class);
        List<TopUsersByDocumentCreationProjection> mockProjections = List.of(first, second);

        when(userRepository.findTopUserByDocumentCreation(startInstant, endInstant, pageable))
                .thenReturn(mockProjections);

        // Invoke
        List<TopUsersByDocumentCreationProjection> result =
                analyticsService.findTopUsersByDocumentCreation(startDate, endDate, pageable);

        // Asserts
        verify(userRepository).findTopUserByDocumentCreation(startInstant, endInstant, pageable);

        assertThat(result).isEqualTo(mockProjections);
    }
}
