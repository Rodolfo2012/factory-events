package com.factory.events.controller;

import com.factory.events.dto.EventRequest;
import com.factory.events.dto.EventResponse;
import com.factory.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Recebimento e consulta de eventos de fábrica")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra um evento",
            description = "Eventos QUALITY também geram um registro de auditoria.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Evento registrado"),
                    @ApiResponse(responseCode = "400", description = "Payload inválido"),
                    @ApiResponse(responseCode = "409", description = "Evento já processado")
            })
    public ResponseEntity<EventResponse> register(@Valid @RequestBody EventRequest request) {
        log.debug("POST /api/events eventId={}", request.eventId());
        EventResponse response = EventResponse.from(service.register(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(response.eventId())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista os eventos registrados")
    public List<EventResponse> list() {
        log.debug("GET /api/events");
        return service.findAll().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Consulta um evento pelo eventId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento encontrado"),
                    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
            })
    public EventResponse get(@PathVariable String eventId) {
        log.debug("GET /api/events/{}", eventId);
        return EventResponse.from(service.findByEventId(eventId));
    }
}
