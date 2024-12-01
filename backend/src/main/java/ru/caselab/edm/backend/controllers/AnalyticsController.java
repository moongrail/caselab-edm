package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentCreationProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByDocumentSigningProjection;
import ru.caselab.edm.backend.repository.projection.TopUsersByReplacementProjection;
import ru.caselab.edm.backend.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Analytics data")
@SecurityRequirement(name = "bearer-jwt")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-users-by-document-creation")
    public List<TopUsersByDocumentCreationProjection> getTopUsersByDocumentCreation(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(page, size);
        return analyticsService.findTopUsersByDocumentCreation(startDate, endDate, pageable);
    }

    @GetMapping("/top-users-by-document-signing-count")
    public List<TopUsersByDocumentSigningProjection> getTopUsersByDocumentSigningCount(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(page, size);
        return analyticsService.findTopUsersByDocumentSigning(startDate, endDate, pageable);
    }

    @GetMapping("/top-users-by-replacement-count")
    public List<TopUsersByReplacementProjection> getTopUsersByReplacementCount(
            @Parameter(description = "Page number starting from 0", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date's parameters. Start date bust be before end date");
        }

        PageRequest pageable = PageRequest.of(page, size);
        return analyticsService.findTopUserByReplacement(startDate, endDate, pageable);
    }

}
