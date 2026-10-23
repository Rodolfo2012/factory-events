package com.factory.events.handler;

import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;

public interface EventHandler {

    EventType supportedType();

    void handle(FactoryEvent event);
}
