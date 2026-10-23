package com.factory.events.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(String eventId) {
        super("Evento não encontrado: " + eventId);
    }
}
