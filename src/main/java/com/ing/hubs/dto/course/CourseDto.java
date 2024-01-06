package com.ing.hubs.dto.course;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ing.hubs.deserielize.LocalDateCourseDeserializer;
import com.ing.hubs.dto.schedule.ScheduleDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    @NotBlank(message = "Course name cannot be blank")
    @Size(min = 2, max = 50, message = "Course name must be between 2 and 50 characters")
    private String courseName;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 10, message = "Capacity must be at least 10")
    @Max(value = 300, message = "Capacity cannot exceed 300")
    private Integer capacity;

    @NotNull(message = "Credits cannot be null")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;

    @NotNull(message = "Start date cannot be null")
    @JsonDeserialize(using = LocalDateCourseDeserializer.class)
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @JsonDeserialize(using = LocalDateCourseDeserializer.class)
    private LocalDate endDate;

    @NotNull(message = "Schedules cannot be null")
    private Set<ScheduleDto> schedules;
}

