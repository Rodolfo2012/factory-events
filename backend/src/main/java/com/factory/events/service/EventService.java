package com.factory.events.service;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;
import com.factory.events.dto.EventRequest;
import com.factory.events.exception.DuplicateEventException;
import com.factory.events.exception.EventNotFoundException;
import com.factory.events.handler.EventHandler;
import com.factory.events.repository.FactoryEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final FactoryEventRepository repository;
    private final Map<EventType, EventHandler> handlers = new EnumMap<>(EventType.class);

    public EventService(FactoryEventRepository repository, List<EventHandler> eventHandlers) {
        this.repository = repository;
        eventHandlers.forEach(handler -> handlers.put(handler.supportedType(), handler));
    }

    @Transactional
    public FactoryEvent register(EventRequest request) {
        log.info("Evento recebido eventId={} type={}", request.eventId(), request.eventType());

        if (repository.existsByEventId(request.eventId())) {
            throw new DuplicateEventException(request.eventId());
        }

        FactoryEvent event = new FactoryEvent(
                request.eventId(),
                request.eventType(),
                request.description(),
                request.timestamp());

        try {
            repository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEventException(request.eventId());
        }

        EventHandler handler = handlers.get(event.getEventType());
        if (handler != null) {
            handler.handle(event);
        }

        return event;
    }

    @Transactional(readOnly = true)
    public List<FactoryEvent> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "receivedAt"));
    }

    @Transactional(readOnly = true)
    public FactoryEvent findByEventId(String eventId) {
        return repository.findByEventId(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }
}
