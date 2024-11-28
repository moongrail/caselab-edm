package ru.caselab.edm.backend.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.repository.projection.TopVotesByParticipants;
import ru.caselab.edm.backend.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final AnalyticsService analyticsService;

    @GetMapping("/top-users-by-document-creation")
    public List<TopUsersByDocumentCreationProjection> getTopUsersByDocumentCreation(@RequestParam LocalDate startDate,
                                                                                    @RequestParam LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        return analyticsService.findTopUsersByDocumentCreation(startDate, endDate, pageable);
    }

    @GetMapping("/top-votes-by-participants")
    public List<TopVotesByParticipants> getTopVotesByParticipants(@RequestParam LocalDate startDate,
                                                                  @RequestParam LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        return analyticsService.findTopVotesByParticipants(startDate, endDate, pageable);
    }

}
