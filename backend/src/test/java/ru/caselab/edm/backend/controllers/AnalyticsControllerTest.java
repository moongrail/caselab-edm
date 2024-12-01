package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import ru.caselab.edm.backend.service.AnalyticsService;

import java.time.LocalDate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(AnalyticsController.class)
public class AnalyticsControllerTest extends BaseControllerTest {

    private static final String BASE_URI = "/analytics";

    @MockBean
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void getTopUsersByDocumentCreation_validDates_shouldReturnTopUsersWithStatusOk() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        int page = 0;
        int size = 10;

        String urlTemplate = BASE_URI +
                "/top-users-by-document-creation?startDate={startDate}&endDate={endDate}";

        performRequest(get(urlTemplate, startDate, endDate, page, size))
                .andDo(print())
                .andExpect(status().isOk());

        verify(analyticsService).findTopUsersByDocumentCreation(startDate, endDate, PageRequest.of(page, size));
    }

    @Test
    void getTopUsersByDocumentCreation_invalidDates_shouldReturnTopUsersWithStatusBadRequest() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 12, 31);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        int page = 0;
        int size = 10;

        String urlTemplate = BASE_URI +
                "/top-users-by-document-creation?startDate={startDate}&endDate={endDate}";

        performRequest(get(urlTemplate, startDate, endDate, page, size))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(analyticsService, never()).findTopUsersByDocumentCreation(startDate, endDate, PageRequest.of(page, size));
    }
}
