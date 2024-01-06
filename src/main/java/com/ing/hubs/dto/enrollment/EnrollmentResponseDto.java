package com.ing.hubs.dto.enrollment;

import com.ing.hubs.dto.course.CourseResponseDto;
import com.ing.hubs.dto.user.UserResponseDto;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentResponseDto {
    private UUID id;
    private Integer grade;
    private EnrollmentStatus status;
    private UserResponseDto user;
    private CourseResponseDto course;
}
