package com.ing.hubs.service.enrollment;

import com.ing.hubs.exception.enrollment.EnrollmentProcessingException;
import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.enrollment.GradeEnrollmentDto;
import com.ing.hubs.dto.enrollment.UpdateEnrollmentStatusDto;
import com.ing.hubs.exception.course.CourseCapacityReachedException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.CourseType;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.course.schedule.Weekday;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.EnrollmentRepository;
import com.ing.hubs.service.CourseService;
import com.ing.hubs.service.ScheduleService;
import com.ing.hubs.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class EnrollmentProcessingServiceTest {
    @Mock
    private SecurityService securityService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private CourseService courseService;
    @Spy
    private ScheduleService scheduleService;
    @Spy
    private ModelMapper modelMapper;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Spy
    private EnrollmentValidationService validationService;
    @InjectMocks
    private EnrollmentProcessingService enrollmentProcessingService;

    private User teacher;
    private User student;
    private UUID teacherId;
    private UUID studentId;
    private UUID courseId;
    private UUID enrollmentId;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void commonSetup() {
        this.teacherId = UUID.randomUUID();
        this.studentId = UUID.randomUUID();
        this.courseId = UUID.randomUUID();
        this.enrollmentId = UUID.randomUUID();
        this.teacher = User.builder().id(this.teacherId).build();
        this.student = User.builder().id(this.studentId).build();
        this.course = Course.builder().id(this.courseId).user(this.teacher).build();
        this.enrollment = Enrollment.builder().id(this.enrollmentId).course(this.course).user(this.student).build();
    }

    @Test
    void shouldThrowEnrollmentAlreadyProcessedExWhenCancelingAnAlreadyProcessedEnrollment(){
        final Enrollment invalidEnrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .status(EnrollmentStatus.APPROVED)
                .course(this.course)
                .build();
        final Enrollment validEnrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .status(EnrollmentStatus.PENDING)
                .course(this.course)
                .build();

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.STUDENT);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(invalidEnrollment).thenReturn(validEnrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(course);

        assertThrows(EnrollmentProcessingException.class, () ->
                this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.CANCELED), "token"));
        assertDoesNotThrow(() ->
                this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.CANCELED), "token"));
    }

    @Test
    void shouldThrowUnauthorizedAccessExWhenNewStatusIsNotCancelled(){
        this.enrollment.setCourse(this.course);

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.STUDENT);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        assertThrows(UnauthorizedAccessException.class, () ->
                this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.APPROVED), "token"));
        assertDoesNotThrow(() ->
                this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.CANCELED), "token"));
    }

    @Test
    void shouldChangeEnrollmentStatusToCanceledWhenCancellationIsValid(){
        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.STUDENT);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        EnrollmentResponseDto processedEnrollment = this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.CANCELED), "token");
        verify(courseService, times(1)).saveCourse(any(Course.class));
        assertEquals(processedEnrollment.getStatus(), EnrollmentStatus.CANCELED);
    }

    @Test
    void shouldThrowUnauthorizedAccessExWhenTeacherDidNotCreateTheCourse(){
        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.APPROVED), "token"));
    }

    @Test
    void shouldThrowCourseCapacityReachedExWhenCourseCapacityIsZero(){
        this.course.setCapacity(0);

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        assertThrows(CourseCapacityReachedException.class, () -> this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.APPROVED), "token"));
    }

    @Test
    void shouldThrowEnrollmentAlreadyProcessedExWhenStatusIsNotPendingOrDenied(){
        this.course.setCapacity(10);
        this.enrollment.setStatus(EnrollmentStatus.APPROVED);

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.APPROVED), "token"));
    }

    @Test
    void shouldChangeEnrollmentStatusIfUpdateIsValid(){
        this.course.setCapacity(10);
        this.enrollment.setStatus(EnrollmentStatus.PENDING);

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(this.enrollment);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(this.course);

        assertEquals(EnrollmentStatus.DENIED, this.enrollmentProcessingService.updateEnrollmentStatus(UUID.randomUUID(), new UpdateEnrollmentStatusDto(EnrollmentStatus.DENIED), "token").getStatus());
    }

    @Test
    void shouldCancelStudentsOverlappingEnrollmentsIfEnrollmentIsApproved(final CapturedOutput capturedOutput){
        final UUID enrollment1Id = UUID.randomUUID();
        final UUID enrollment2Id = UUID.randomUUID();

        final User teacher = User.builder()
                .id(this.teacherId)
                .build();
        final User student = User.builder()
                .id(this.studentId)
                .build();

        final Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .courseType(CourseType.COURSE)
                .startTime(LocalTime.MIDNIGHT)
                .endTime(LocalTime.MIDNIGHT.plus(Duration.ofHours(1)))
                .weekday(Weekday.MONDAY)
                .build();
        final Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .courseType(CourseType.COURSE)
                .startTime(LocalTime.MIDNIGHT)
                .endTime(LocalTime.MIDNIGHT.plus(Duration.ofHours(1)))
                .weekday(Weekday.MONDAY)
                .build();

        final Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .user(teacher)
                .capacity(10)
                .schedules(Set.of(schedule1))
                .build();
        final Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .user(teacher)
                .capacity(10)
                .schedules(Set.of(schedule1))
                .build();

        final Enrollment enrollment1 = Enrollment.builder()
                .id(enrollment1Id)
                .status(EnrollmentStatus.PENDING)
                .course(course1)
                .user(student)
                .build();
        final Enrollment enrollment2 = Enrollment.builder()
                .id(enrollment2Id)
                .status(EnrollmentStatus.PENDING)
                .course(course2)
                .user(student)
                .build();

        schedule1.setCourse(course1);
        schedule2.setCourse(course2);

        course1.setSchedules(Set.of(schedule1));
        course2.setSchedules(Set.of(schedule2));

        course1.setEnrollments(Set.of(enrollment1));
        course2.setEnrollments(Set.of(enrollment2));

        student.setEnrollments(Set.of(enrollment1, enrollment2));

        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(enrollment1Id)).thenReturn(enrollment1);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(course1);
        when(enrollmentService.findAllEnrollmentsByUser(any(UUID.class))).thenReturn(List.of(enrollment1, enrollment2));

        assertEquals(EnrollmentStatus.APPROVED, this.enrollmentProcessingService.updateEnrollmentStatus(enrollment1Id, new UpdateEnrollmentStatusDto(EnrollmentStatus.APPROVED), "token").getStatus());
        assertThat(capturedOutput.getOut(), allOf(
                containsString(enrollment2Id.toString()),
                containsString(EnrollmentStatus.CANCELED.toString())
        ));
    }

    @Test
    void shouldThrowEnrollmentNotActiveExceptionWhenGradingNonActiveEnrollment(){
        this.enrollment.setStatus(EnrollmentStatus.PENDING);

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(enrollment);

        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentProcessingService.gradeEnrollment(enrollment.getId(), new GradeEnrollmentDto(8), "token"));
    }

    @Test
    void shouldThrowEnrollmentGradingExceptionWhenGradeIsInvalid(){
        this.enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(enrollment);

        assertThrows(EnrollmentProcessingException.class, () -> this.enrollmentProcessingService.gradeEnrollment(enrollment.getId(), new GradeEnrollmentDto(12), "token"));
    }

    @Test
    void shouldGradeEnrollmentWhenDataIsValid(final CapturedOutput capturedOutput){
        this.enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.teacherId);
        when(enrollmentService.findEnrollmentById(any(UUID.class))).thenReturn(enrollment);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        this.enrollmentProcessingService.gradeEnrollment(enrollment.getId(), new GradeEnrollmentDto(8), "token");
        assertThat(capturedOutput.getOut(), containsString(enrollment.getId().toString()));
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }
}