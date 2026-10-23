package com.factory.events.exception;

public class DuplicateEventException extends RuntimeException {

    public DuplicateEventException(String eventId) {
        super("Evento já processado: " + eventId);
    }
}
