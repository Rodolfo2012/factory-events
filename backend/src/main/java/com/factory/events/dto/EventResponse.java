package com.factory.events.dto;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;

import java.time.Instant;
import java.time.LocalDateTime;

public record EventResponse(
        String eventId,
        EventType eventType,
        String description,
        LocalDateTime timestamp,
        Instant receivedAt
) {

    public static EventResponse from(FactoryEvent event) {
        return new EventResponse(
                event.getEventId(),
                event.getEventType(),
                event.getDescription(),
                event.getTimestamp(),
                event.getReceivedAt());
    }
}
