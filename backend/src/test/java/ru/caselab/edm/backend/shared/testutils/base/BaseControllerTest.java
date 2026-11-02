package ru.caselab.edm.backend.shared.testutils.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.caselab.edm.backend.integration.search.AttributeSearchRepository;
import ru.caselab.edm.platform.security.AuthenticatedUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private AttributeSearchRepository attributeSearchRepository;
    @MockBean
    private UserDetailsService userDetailsService;
    protected ObjectMapper objectMapper;

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder,
                                           Object requestBody,
                                           AuthenticatedUser authenticatedUser) throws Exception {
        requestBuilder.with(user(authenticatedUser));
        return performRequest(requestBuilder, requestBody);
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder,
                                           Object requestBody) throws Exception {
        requestBuilder.content(writeAsJson(requestBody));
        return performRequest(requestBuilder);
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(
                requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        );
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
