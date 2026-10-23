package com.factory.events.service;

import com.factory.events.domain.AuditRecord;
import com.factory.events.repository.AuditRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRecordRepository repository;

    public AuditService(AuditRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> findAll() {
        List<AuditRecord> records = repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        log.debug("Consulta de auditoria retornou {} registros", records.size());
        return records;
    }
}
