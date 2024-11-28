package ru.caselab.edm.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-users-by-document-creation")
    public List<TopUsersByDocumentCreationProjection> getTopUsersByDocumentCreation(@RequestParam LocalDate startDate,
                                                                                    @RequestParam LocalDate endDate,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(page, size);
        return analyticsService.findTopUsersByDocumentCreation(startDate, endDate, pageable);
    }

}
