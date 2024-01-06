package com.ing.hubs.dto.course;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePatchDto {
    @Size(min = 2, max = 50, message = "Course name must be between 2 and 50 characters")
    private String courseName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Min(value = 10, message = "Capacity must be at least 10")
    @Max(value = 300, message = "Capacity cannot exceed 300")
    private Integer capacity;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;
}