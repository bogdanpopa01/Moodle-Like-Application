package com.ing.hubs.controller;

import com.ing.hubs.dto.enrollment.UpdateEnrollmentStatusDto;
import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.enrollment.GradeEnrollmentDto;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.service.enrollment.EnrollmentProcessingService;
import com.ing.hubs.service.enrollment.EnrollmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/enrollments")
public class EnrollmentController {
    private EnrollmentService enrollmentService;
    private EnrollmentProcessingService enrollmentProcessingService;

    @PostMapping("/{courseId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public EnrollmentResponseDto create(@PathVariable @Valid final UUID courseId,
                                        @RequestHeader(name = "Authorization") final String jwtToken){
        return this.enrollmentService.createEnrollment(courseId, jwtToken);
    }

    @PatchMapping("/{enrollmentId}")
    public EnrollmentResponseDto updateEnrollmentStatus(@PathVariable final UUID enrollmentId,
                                                        @RequestBody @Valid final UpdateEnrollmentStatusDto dto,
                                                        @RequestHeader(name = "Authorization") final String jwtToken){
        return this.enrollmentProcessingService.updateEnrollmentStatus(enrollmentId, dto, jwtToken);
    }

    @PatchMapping("/grades/{enrollmentId}")
    public EnrollmentResponseDto gradeEnrollment(@PathVariable final UUID enrollmentId,
                                                 @RequestBody @Valid final GradeEnrollmentDto dto,
                                                 @RequestHeader(name = "Authorization") final String jwtToken){
        return this.enrollmentProcessingService.gradeEnrollment(enrollmentId, dto, jwtToken);
    }

    @GetMapping("/courses/{courseId}")
    public List<EnrollmentResponseDto> findAllEnrollmentsByCourseId(@PathVariable final UUID courseId,
                                                                    @RequestHeader(name = "Authorization") final String jwtToken){
        return this.enrollmentService.findAllEnrollmentsByCourse(courseId, jwtToken);
    }
}
