package ru.caselab.edm.security.testutils.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.caselab.edm.security.domain.UserInfoDetails;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.security.persistence.RoleRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private RoleRepository roleRepository;

    protected ObjectMapper objectMapper;

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder,
                                           Object requestBody,
                                           AuthenticatedUser userInfoDetails) throws Exception {
        requestBuilder.with(user(userInfoDetails));
        return performRequest(requestBuilder, requestBody);
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder,
                                           Object requestBody) throws Exception {
        requestBuilder.content(writeAsJson(requestBody));
        return performRequest(requestBuilder);
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
