package com.factory.events.controller;

import com.factory.events.dto.AuditResponse;
import com.factory.events.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@Tag(name = "Auditoria", description = "Registros de auditoria gerados por eventos QUALITY")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os registros de auditoria")
    public List<AuditResponse> list() {
        log.debug("GET /api/audits");
        return service.findAll().stream().map(AuditResponse::from).toList();
    }
}
