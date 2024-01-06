package com.ing.hubs.dto.enrollment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeEnrollmentDto {
    @Min(value = 1, message = "Min value must be 1")
    @Max(value = 10, message = "Max value must be 10")
    private int grade;
}
