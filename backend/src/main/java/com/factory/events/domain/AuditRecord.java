package com.factory.events.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    @NonNull
    private String eventId;

    @Column(nullable = false, length = 60)
    @NonNull
    private String action;

    @Column(nullable = false, length = 600)
    @NonNull
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
