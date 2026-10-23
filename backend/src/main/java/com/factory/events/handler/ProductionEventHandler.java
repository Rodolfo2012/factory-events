package com.factory.events.handler;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductionEventHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductionEventHandler.class);

    @Override
    public EventType supportedType() {
        return EventType.PRODUCTION;
    }

    @Override
    public void handle(FactoryEvent event) {
        log.info("Evento de produção registrado eventId={}", event.getEventId());
    }
}
