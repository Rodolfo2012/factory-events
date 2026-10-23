package com.factory.events;

import com.factory.events.repository.AuditRecordRepository;
import com.factory.events.repository.FactoryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FactoryEventRepository eventRepository;

    @Autowired
    private AuditRecordRepository auditRepository;

    @BeforeEach
    void cleanDatabase() {
        auditRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private String body(String id, String type) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "%s",
                  "description": "Evento de teste",
                  "timestamp": "2026-05-20T10:00:00"
                }
                """.formatted(id, type);
    }

    @Test
    void productionEventIsRegisteredWithoutAudit() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body("EVT-1", "PRODUCTION")))
                .andExpect(status().isCreated());

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditRepository.count()).isZero();
    }

    @Test
    void qualityEventIsRegisteredAndAudited() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body("EVT-2", "QUALITY")))
                .andExpect(status().isCreated());

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditRepository.findByEventId("EVT-2")).hasSize(1);
        mockMvc.perform(get("/api/audits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("EVT-2"));
    }

    @Test
    void duplicatedEventIsRejectedAndNotReprocessed() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body("EVT-3", "QUALITY")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body("EVT-3", "QUALITY")))
                .andExpect(status().isConflict());

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditRepository.count()).isEqualTo(1);
    }

    @Test
    void listAndGetReturnRegisteredEvents() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body("EVT-4", "PRODUCTION")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("EVT-4"));
        mockMvc.perform(get("/api/events/EVT-4")).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/EVT-X")).andExpect(status().isNotFound());
    }
}
