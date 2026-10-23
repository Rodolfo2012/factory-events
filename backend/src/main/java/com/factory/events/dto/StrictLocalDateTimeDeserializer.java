package com.factory.events.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class StrictLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter LOCAL_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .optionalEnd()
            .toFormatter();

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            throw new InvalidFormatException(p,
                    "Formato inválido para o campo " + p.currentName(),
                    p.getText(), LocalDateTime.class);
        }

        String text = p.getText().trim();

        // Rejeita explicitamente timestamps com fuso horário (ex.: 2026-05-20T10:00:00Z
        // ou com offset +02:00 / -0300)
        if (text.endsWith("Z") || text.endsWith("z") || text.matches(".*[+-]\\d{2}(:?\\d{2})?$")) {
            throw new InvalidFormatException(p,
                    "Formato inválido para o campo timestamp (não deve conter fuso horário, esperado formato local)",
                    text, LocalDateTime.class);
        }

        try {
            return LocalDateTime.parse(text, LOCAL_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new InvalidFormatException(p,
                    "Formato inválido para o campo timestamp",
                    text, LocalDateTime.class);
        }
    }
}
