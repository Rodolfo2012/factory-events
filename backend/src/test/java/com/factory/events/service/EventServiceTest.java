package com.factory.events.service;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;
import com.factory.events.dto.EventRequest;
import com.factory.events.exception.DuplicateEventException;
import com.factory.events.exception.EventNotFoundException;
import com.factory.events.handler.EventHandler;
import com.factory.events.repository.FactoryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 5, 20, 10, 0, 0);

    @Mock
    private FactoryEventRepository repository;

    @Mock
    private EventHandler productionHandler;

    @Mock
    private EventHandler qualityHandler;

    private EventService service;

    @BeforeEach
    void setUp() {
        when(productionHandler.supportedType()).thenReturn(EventType.PRODUCTION);
        when(qualityHandler.supportedType()).thenReturn(EventType.QUALITY);
        service = new EventService(repository, List.of(productionHandler, qualityHandler));
    }

    @Test
    void shouldRegisterProductionEventWithoutTriggeringQualityHandler() {
        EventRequest request = new EventRequest("EVT-1001", EventType.PRODUCTION, "Production line started", TIMESTAMP);
        when(repository.existsByEventId("EVT-1001")).thenReturn(false);

        FactoryEvent result = service.register(request);

        assertThat(result.getEventId()).isEqualTo("EVT-1001");
        verify(repository).saveAndFlush(any(FactoryEvent.class));
        verify(productionHandler).handle(result);
        verify(qualityHandler, never()).handle(any());
    }

    @Test
    void shouldTriggerQualityHandlerForQualityEvent() {
        EventRequest request = new EventRequest("EVT-2001", EventType.QUALITY, "Defect detected", TIMESTAMP);
        when(repository.existsByEventId("EVT-2001")).thenReturn(false);

        FactoryEvent result = service.register(request);

        verify(qualityHandler).handle(result);
        verify(productionHandler, never()).handle(any());
    }

    @Test
    void shouldRejectDuplicateEventBeforePersisting() {
        EventRequest request = new EventRequest("EVT-1001", EventType.PRODUCTION, "Production line started", TIMESTAMP);
        when(repository.existsByEventId("EVT-1001")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(DuplicateEventException.class);

        verify(repository, never()).saveAndFlush(any());
        verify(productionHandler, never()).handle(any());
    }

    @Test
    void shouldTranslateConstraintViolationIntoDuplicateException() {
        EventRequest request = new EventRequest("EVT-1001", EventType.PRODUCTION, "Production line started", TIMESTAMP);
        when(repository.existsByEventId("EVT-1001")).thenReturn(false);
        when(repository.saveAndFlush(any(FactoryEvent.class))).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(DuplicateEventException.class);

        verify(productionHandler, never()).handle(any());
    }

    @Test
    void shouldThrowWhenEventIsNotFound() {
        when(repository.findByEventId("EVT-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEventId("EVT-404")).isInstanceOf(EventNotFoundException.class);
    }
}
