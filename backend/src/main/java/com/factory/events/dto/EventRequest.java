package com.factory.events.dto;

import com.factory.events.domain.EventType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank(message = "eventId é obrigatório")
        @Size(max = 100, message = "eventId deve ter no máximo 100 caracteres")
        @JsonDeserialize(using = StrictStringDeserializer.class)
        String eventId,

        @NotNull(message = "eventType é obrigatório")
        EventType eventType,

        @NotBlank(message = "description é obrigatório")
        @Size(max = 500, message = "description deve ter no máximo 500 caracteres")
        @JsonDeserialize(using = StrictStringDeserializer.class)
        String description,

        @NotNull(message = "timestamp é obrigatório")
        @JsonDeserialize(using = StrictLocalDateTimeDeserializer.class)
        LocalDateTime timestamp
) {
}
