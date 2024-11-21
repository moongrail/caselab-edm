package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.service.JwtService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private AttributeSearchRepository attributeSearchRepository;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private JwtService jwtService;
    protected ObjectMapper objectMapper;


    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder, Object requestBody, UserInfoDetails userInfoDetails) throws Exception {
        requestBuilder.with(user(userInfoDetails));
        return performRequest(requestBuilder, requestBody);
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder, Object requestBody) throws Exception {
        return mockMvc.perform(
                requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(writeAsJson(requestBody))
        );
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
