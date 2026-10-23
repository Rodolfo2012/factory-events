package com.factory.events.repository;

import com.factory.events.domain.FactoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactoryEventRepository extends JpaRepository<FactoryEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<FactoryEvent> findByEventId(String eventId);
}
