package com.ing.hubs.dto.schedule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ing.hubs.deserielize.CourseTypeDeserializer;
import com.ing.hubs.deserielize.LocalTimeDeserializer;
import com.ing.hubs.deserielize.WeekdayDeserializer;
import com.ing.hubs.model.entity.course.schedule.CourseType;
import com.ing.hubs.model.entity.course.schedule.Weekday;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {
    @NotEmpty(message = "Course type must not be empty")
    @JsonDeserialize(using = CourseTypeDeserializer.class)
    private CourseType courseType;

    @NotEmpty(message = "Start time must not be empty")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime startTime;

    @NotEmpty(message = "End time must not be empty")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime endTime;

    @JsonDeserialize(using = WeekdayDeserializer.class)
    @NotEmpty(message = "Weekday must not be empty")
    private Weekday weekday;
}
