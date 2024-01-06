package com.ing.hubs.deserielize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.schedule.InvalidCourseTypeException;
import com.ing.hubs.model.entity.course.schedule.CourseType;

import java.io.IOException;

public class CourseTypeDeserializer extends JsonDeserializer<CourseType> {
    @Override
    public CourseType deserialize(JsonParser parser, DeserializationContext context) throws IOException{
        String value = parser.getValueAsString().toUpperCase();
        try {
            return CourseType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidCourseTypeException();
        }
    }
}
