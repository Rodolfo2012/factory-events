package com.factory.events.controller;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;
import com.factory.events.exception.DuplicateEventException;
import com.factory.events.exception.GlobalExceptionHandler;
import com.factory.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    private static final String VALID_BODY = """
            {
              "eventId": "EVT-1001",
              "eventType": "PRODUCTION",
              "description": "Production line started",
              "timestamp": "2026-05-20T10:00:00"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService service;

    @Test
    void shouldReturn201WhenEventIsRegistered() throws Exception {
        when(service.register(any())).thenReturn(new FactoryEvent(
                "EVT-1001", EventType.PRODUCTION, "Production line started", LocalDateTime.of(2026, 5, 20, 10, 0)));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/events/EVT-1001"))
                .andExpect(jsonPath("$.eventId").value("EVT-1001"))
                .andExpect(jsonPath("$.eventType").value("PRODUCTION"));
    }

    @Test
    void shouldReturn409WhenEventIsDuplicated() throws Exception {
        when(service.register(any())).thenThrow(new DuplicateEventException("EVT-1001"));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Evento já processado: EVT-1001"));
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(4));
    }

    @Test
    void shouldReturn400WhenEventTypeIsUnknown() throws Exception {
        String body = VALID_BODY.replace("PRODUCTION", "MAINTENANCE");

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("PRODUCTION")));
    }

    @Test
    void shouldReturn400WhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEventIdIsEmpty() throws Exception {
        String body = VALID_BODY.replace("\"EVT-1001\"", "\"\"");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("eventId é obrigatório"));
    }

    @Test
    void shouldReturn400WhenEventIdIsBlank() throws Exception {
        String body = VALID_BODY.replace("\"EVT-1001\"", "\"   \"");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("eventId é obrigatório"));
    }

    @Test
    void shouldReturn400WhenTimestampHasTimezone() throws Exception {
        String body = VALID_BODY.replace("\"2026-05-20T10:00:00\"", "\"2026-05-20T10:00:00Z\"");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("fuso horário")));
    }

    @Test
    void shouldReturn400WhenEventIdIsNumeric() throws Exception {
        String body = VALID_BODY.replace("\"EVT-1001\"", "123");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
