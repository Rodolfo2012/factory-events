package com.factory.events.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateEventException ex) {
        log.warn("Evento duplicado rejeitado: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EventNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .sorted()
                .toList();
        log.warn("Payload inválido: {}", details);
        String message = details.size() == 1 ? details.get(0) : "Dados do evento inválidos";
        return build(HttpStatus.BAD_REQUEST, message, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        String message = "Corpo da requisição inválido ou malformado";
        List<String> details = List.of();
        if (ex.getCause() instanceof InvalidFormatException format) {
            String field = format.getPath().isEmpty() ? "" : format.getPath().get(format.getPath().size() - 1).getFieldName();
            Class<?> target = format.getTargetType();
            if (target != null && target.isEnum()) {
                details = List.of("Valor inválido para " + field + ". Valores aceitos: "
                        + Arrays.toString(target.getEnumConstants()));
            } else if (format.getOriginalMessage() != null && !format.getOriginalMessage().isBlank() && !format.getOriginalMessage().startsWith("Cannot deserialize")) {
                details = List.of(format.getOriginalMessage());
            } else {
                details = List.of("Formato inválido para o campo " + field);
            }
        }
        if (!details.isEmpty()) {
            message = details.get(0);
        }
        log.warn("Requisição ilegível: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, message, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição", List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(body);
    }
}
