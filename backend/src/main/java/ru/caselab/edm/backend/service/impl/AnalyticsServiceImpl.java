package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.service.AnalyticsService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;

    @Override
    public List<TopUsersByDocumentCreationProjection> findTopUsersByDocumentCreation(LocalDate startDate,
                                                                                     LocalDate endDate,
                                                                                     Pageable pageable) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return userRepository.findTopUserByDocumentCreation(startInstant, endInstant, pageable);
    }
}
