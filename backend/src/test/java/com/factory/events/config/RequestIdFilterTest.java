package com.factory.events.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void geraRequestIdQuandoNaoInformado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/events"), response, new MockFilterChain());

        String id = response.getHeader(RequestIdFilter.HEADER);
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void reaproveitaRequestIdRecebido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events");
        request.addHeader(RequestIdFilter.HEADER, "abc-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("abc-123", response.getHeader(RequestIdFilter.HEADER));
    }

    @Test
    void limpaMdcAoFinalizar() throws Exception {
        filter.doFilter(new MockHttpServletRequest("GET", "/api/events"),
                new MockHttpServletResponse(), new MockFilterChain());

        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }
}
