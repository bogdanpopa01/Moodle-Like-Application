package com.ing.hubs.deserielize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.schedule.InvalidWeekdayException;
import com.ing.hubs.model.entity.course.schedule.Weekday;

import java.io.IOException;

public class WeekdayDeserializer extends JsonDeserializer<Weekday> {
    @Override
    public Weekday deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString().toUpperCase();
        try {
            return Weekday.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidWeekdayException();
        }
    }
}
