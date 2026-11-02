package ru.caselab.edm.security.internal;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class InternalTokenFilterTest {

    private final FilterChain chain = mock(FilterChain.class);

    @Test
    void nonInternalRequest_isNotFiltered() throws Exception {
        InternalTokenFilter filter = new InternalTokenFilter(new InternalApiProperties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        request.setContextPath("/api/v1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void internalRequestWithoutToken_isRejected() throws Exception {
        InternalTokenFilter filter = new InternalTokenFilter(new InternalApiProperties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/internal/users");
        request.setContextPath("/api/v1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    @Test
    void internalRequestWithWrongToken_isRejected() throws Exception {
        InternalTokenFilter filter = new InternalTokenFilter(new InternalApiProperties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/users");
        request.addHeader(InternalTokenFilter.HEADER, "wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    @Test
    void internalRequestWithCorrectToken_passes() throws Exception {
        InternalTokenFilter filter = new InternalTokenFilter(new InternalApiProperties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/users");
        request.addHeader(InternalTokenFilter.HEADER, "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void blankConfiguredToken_rejectsEverything() throws Exception {
        InternalTokenFilter filter = new InternalTokenFilter(new InternalApiProperties(" "));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/users");
        request.addHeader(InternalTokenFilter.HEADER, " ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }
}
