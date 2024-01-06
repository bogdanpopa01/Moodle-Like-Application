package com.ing.hubs.deserielize;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.InvalidDateException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate deserialize(final JsonParser parser,
                                 final DeserializationContext context) throws IOException {
        final String value = parser.getValueAsString();
        try {
            final LocalDate parsedDate = LocalDate.parse(value, DATE_FORMATTER);

            if (parsedDate.getYear() <= 1900 || parsedDate.isAfter(LocalDate.now())) {
                throw new InvalidDateException("Year must be greater than 1900 and less than or equal to the current year!");
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Format must be: yyyy-MM-dd and a valid date!");
        }
    }
}