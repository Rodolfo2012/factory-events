package com.factory.events.handler;

import com.factory.events.domain.AuditRecord;
import com.factory.events.domain.EventType;
import com.factory.events.domain.FactoryEvent;
import com.factory.events.repository.AuditRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QualityEventHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(QualityEventHandler.class);

    private static final String AUDIT_ACTION = "QUALITY_EVENT_REGISTERED";

    private final AuditRecordRepository auditRecordRepository;

    public QualityEventHandler(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Override
    public EventType supportedType() {
        return EventType.QUALITY;
    }

    @Override
    public void handle(FactoryEvent event) {
        String details = "Evento de qualidade " + event.getEventId() + " ocorrido em "
                + event.getTimestamp() + ": " + event.getDescription();
        auditRecordRepository.save(new AuditRecord(event.getEventId(), AUDIT_ACTION, details));
        log.info("Registro de auditoria gerado eventId={}", event.getEventId());
    }
}
