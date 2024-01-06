package com.ing.hubs.deserielize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.InvalidTimeException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String value = parser.getValueAsString();
        try {
            return LocalTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidTimeException();
        }
    }
}
