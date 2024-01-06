package com.ing.hubs.deserielize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ing.hubs.exception.enrollment.InvalidEnrollmentStatus;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;

import java.io.IOException;

public class EnrollmentStatusDeserializer extends JsonDeserializer<EnrollmentStatus> {
    @Override
    public EnrollmentStatus deserialize(JsonParser parser, DeserializationContext
            context) throws IOException {
        String value = parser.getValueAsString().toUpperCase();
        try{
            EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(value);
            if (enrollmentStatus != EnrollmentStatus.APPROVED &&
                enrollmentStatus != EnrollmentStatus.DENIED &&
                enrollmentStatus != EnrollmentStatus.CANCELED) {
                throw new InvalidEnrollmentStatus();
            }
            return enrollmentStatus;
        }catch (IllegalArgumentException e){
            throw new InvalidEnrollmentStatus();
        }
    }
}
