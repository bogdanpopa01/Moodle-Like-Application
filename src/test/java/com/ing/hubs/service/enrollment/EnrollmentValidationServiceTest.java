package com.ing.hubs.service.enrollment;

import com.ing.hubs.exception.course.CourseCapacityReachedException;
import com.ing.hubs.exception.enrollment.EnrollmentProcessingException;
import com.ing.hubs.exception.enrollment.PassedEnrollmentPeriodException;
import com.ing.hubs.exception.enrollment.StudentAlreadyEnrolledException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class EnrollmentValidationServiceTest {
    private final EnrollmentValidationService enrollmentValidationService;

    public EnrollmentValidationServiceTest() {
        this.enrollmentValidationService = new EnrollmentValidationService();
    }

    @Test
    void shouldThrowStudentAlreadyEnrolledException(){
        final Course course = new Course();
        final Enrollment enrollment = new Enrollment();
        final User student = new User();

        enrollment.setUser(student);
        enrollment.setCourse(course);
        course.getEnrollments().add(enrollment);

        assertThrows(StudentAlreadyEnrolledException.class, () -> enrollmentValidationService.validateIfAlreadyEnrolled(course, student));
    }

    @Test
    void shouldThrowCourseCapacityReachedExceptionWhenInvalidCapacity() {
        final Course invalidCourse = Course.builder()
                .capacity(0)
                .build();
        final Course validCourse = Course.builder()
                .capacity(20)
                .build();

        assertThrows(CourseCapacityReachedException.class, () -> enrollmentValidationService.validateCapacity(invalidCourse));
        assertDoesNotThrow(() -> enrollmentValidationService.validateCapacity(validCourse));
    }

    @Test
    void shouldThrowEnrollmentNotActiveExceptionWhenEnrollmentNotActive() {
        final Enrollment invalidEnrollment = Enrollment.builder()
                .status(EnrollmentStatus.PENDING)
                .build();
        final Enrollment validEnrollment = Enrollment.builder()
                .status(EnrollmentStatus.ACTIVE)
                .build();
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateExistingEnrollment(invalidEnrollment));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateExistingEnrollment(validEnrollment));
    }

    @Test
    void shouldThrowEnrollmentProcessingExceptionWhenEnrollmentAlreadyGraded() {
        final Enrollment invalidEnrollment = Enrollment.builder()
                .status(EnrollmentStatus.ACTIVE)
                .grade(8)
                .build();
        final Enrollment validEnrollment = Enrollment.builder()
                .status(EnrollmentStatus.ACTIVE)
                .build();
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateExistingEnrollment(invalidEnrollment));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateExistingEnrollment(validEnrollment));
        }

    @Test
    void shouldThrowUnauthorizedAccessExceptionWhenIdDoesNotMatch() {
        final UUID teacherId = UUID.randomUUID();
        final Course course = new Course();
        final Enrollment enrollment = new Enrollment();
        final User teacher = User.builder()
                .id(teacherId)
                .build();
        course.setUser(teacher);
        enrollment.setCourse(course);

        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentValidationService.validateTeacherPermissions(enrollment, UUID.randomUUID()));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateTeacherPermissions(enrollment, teacherId));
    }

    @Test
    void shouldThrowEnrollmentProcessingExceptionWhenGradeIsInvalid() {
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateGrade(-1));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateGrade(11));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateGrade(5));
    }

    @Test
    void shouldThrowEnrollmentProcessingExceptionWhenEnrollmentAlreadyProcessed() {
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.APPROVED, EnrollmentStatus.CANCELED));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.DENIED, EnrollmentStatus.CANCELED));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.ACTIVE, EnrollmentStatus.CANCELED));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.COMPLETED, EnrollmentStatus.CANCELED));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.PENDING, EnrollmentStatus.CANCELED));
    }

    @Test
    void shouldThrowUnauthorizedAccessExceptionForInvalidCancellationStatus() {
        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.PENDING, EnrollmentStatus.APPROVED));
        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.PENDING, EnrollmentStatus.DENIED));
        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.PENDING, EnrollmentStatus.ACTIVE));
        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentValidationService.validateEnrollmentCancellation(EnrollmentStatus.PENDING, EnrollmentStatus.COMPLETED));
    }

    @Test
    void shouldThrowEnrollmentProcessingExceptionForInvalidStatusTransition() {
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.PENDING, EnrollmentStatus.ACTIVE));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.PENDING, EnrollmentStatus.COMPLETED));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.DENIED, EnrollmentStatus.ACTIVE));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.DENIED, EnrollmentStatus.PENDING));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.DENIED, EnrollmentStatus.COMPLETED));
        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.DENIED, EnrollmentStatus.CANCELED));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.PENDING, EnrollmentStatus.APPROVED));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.PENDING, EnrollmentStatus.DENIED));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateIfEnrollmentStatusTransitionIsValid(EnrollmentStatus.DENIED, EnrollmentStatus.APPROVED));
    }

    @Test
    void shouldThrowPassedEnrollmentPeriodWhenCourseAlreadyStarted(){
        final Course course = Course.builder()
                .startDate(LocalDate.now().minusDays(1))
                .build();

        assertThrows(PassedEnrollmentPeriodException.class, () -> this.enrollmentValidationService.validateEnrollmentPeriod(course));
        course.setStartDate(LocalDate.now().plusDays(1));
        assertDoesNotThrow(() -> this.enrollmentValidationService.validateEnrollmentPeriod(course));
    }
}