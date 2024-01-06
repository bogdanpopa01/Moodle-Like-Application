package com.ing.hubs.dto.enrollment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ing.hubs.deserielize.EnrollmentStatusDeserializer;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEnrollmentStatusDto {
    @NotNull(message = "Enrollment status cannot be null!")
    @JsonDeserialize(using = EnrollmentStatusDeserializer.class)
    private EnrollmentStatus status;
}
