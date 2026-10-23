package com.factory.events.dto;

import com.factory.events.domain.AuditRecord;

import java.time.Instant;

public record AuditResponse(
        Long id,
        String eventId,
        String action,
        String details,
        Instant createdAt
) {

    public static AuditResponse from(AuditRecord record) {
        return new AuditResponse(
                record.getId(),
                record.getEventId(),
                record.getAction(),
                record.getDetails(),
                record.getCreatedAt());
    }
}
