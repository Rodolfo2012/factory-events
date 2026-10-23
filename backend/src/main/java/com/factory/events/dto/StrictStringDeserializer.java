package com.factory.events.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;

public class StrictStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            throw new InvalidFormatException(p,
                    "Formato inválido para o campo " + p.currentName() + " (deve ser texto entre aspas)",
                    p.getText(), String.class);
        }
        String text = p.getText();
        if (text != null) {
            String trimmed = text.trim();
            // Se o conteúdo for apenas aspas ou aspas com espaços dentro (ex: "" ou "   ")
            if (trimmed.matches("^\"\\s*\"$")) {
                return "";
            }
        }
        return text;
    }
}
