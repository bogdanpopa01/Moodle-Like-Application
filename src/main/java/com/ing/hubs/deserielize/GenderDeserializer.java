package com.ing.hubs.deserielize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.user.InvalidGenderException;
import com.ing.hubs.model.entity.user.Gender;

import java.io.IOException;

public class GenderDeserializer extends JsonDeserializer<Gender> {
    @Override
    public Gender deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final String value = parser.getValueAsString().toUpperCase();
        try {
            return Gender.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidGenderException();
        }
    }
}
