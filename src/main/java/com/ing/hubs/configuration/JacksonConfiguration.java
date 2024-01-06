package com.ing.hubs.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ing.hubs.deserielize.*;
import com.ing.hubs.model.entity.course.schedule.CourseType;
import com.ing.hubs.model.entity.course.schedule.Weekday;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Gender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class JacksonConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Gender.class, new GenderDeserializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addDeserializer(CourseType.class, new CourseTypeDeserializer());
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        module.addDeserializer(EnrollmentStatus.class, new EnrollmentStatusDeserializer());
        module.addDeserializer(Weekday.class, new WeekdayDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
