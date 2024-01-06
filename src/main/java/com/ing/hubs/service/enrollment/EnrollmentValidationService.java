package com.ing.hubs.service.enrollment;

import com.ing.hubs.exception.enrollment.EnrollmentProcessingException;
import com.ing.hubs.exception.course.CourseCapacityReachedException;
import com.ing.hubs.exception.enrollment.*;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class EnrollmentValidationService {
    void validateIfAlreadyEnrolled(final Course course,
                                   final User student){
        if(course.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getUser().equals(student))){
            throw new StudentAlreadyEnrolledException();
        }
    }

    void validateCapacity(final Course course){
        if (course.getCapacity() <= 0){
            throw new CourseCapacityReachedException();
        }
    }

    void validateExistingEnrollment(final Enrollment enrollment){
        if (!enrollment.getStatus().equals(EnrollmentStatus.ACTIVE)){
            throw new EnrollmentProcessingException("Enrollment is not active!");
        }
        if (enrollment.getGrade() != null){
            throw new EnrollmentProcessingException("Enrollment already graded!");
        }
    }

    void validateTeacherPermissions(final Enrollment enrollment,
                                    final UUID teacherId){
        if (!enrollment.getCourse().getUser().getId().equals(teacherId)){
            throw new UnauthorizedAccessException();
        }
    }

    void validateGrade(final int grade){
        if (grade < 1 || grade > 10){
            throw new EnrollmentProcessingException("Grade must be between 1 and 10!");
        }
    }

    void validateEnrollmentCancellation(final EnrollmentStatus currentStatus,
                                        final EnrollmentStatus newStatus){
        if (!currentStatus.equals(EnrollmentStatus.PENDING)){
            throw new EnrollmentProcessingException("Enrollment already processed");
        }
        if (!newStatus.equals(EnrollmentStatus.CANCELED)){
            throw new UnauthorizedAccessException();
        }
    }

    void validateIfEnrollmentStatusTransitionIsValid(final EnrollmentStatus currentStatus,
                                                     final EnrollmentStatus newStatus){
        if (!newStatus.equals(EnrollmentStatus.APPROVED) && !newStatus.equals(EnrollmentStatus.DENIED)){
            throw new EnrollmentProcessingException("Invalid new status");
        }
        if (!currentStatus.isValidTransition(newStatus)){
            throw new EnrollmentProcessingException("Enrollment already processed");
        }
    }

    void validateEnrollmentPeriod(final Course course){
        if (course.getStartDate().isBefore(LocalDate.now()) ||
            course.getStartDate().equals(LocalDate.now())){
            throw new PassedEnrollmentPeriodException();
        }
    }
}
