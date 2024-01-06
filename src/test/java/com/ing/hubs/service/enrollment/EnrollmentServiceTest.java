package com.ing.hubs.service.enrollment;

import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.schedule.StudentsScheduleResponseDto;
import com.ing.hubs.exception.course.CourseCapacityReachedException;
import com.ing.hubs.exception.EntityNotFoundException;
import com.ing.hubs.exception.user.InvalidIdentifierException;
import com.ing.hubs.exception.enrollment.StudentAlreadyEnrolledException;
import com.ing.hubs.exception.course.InvalidScheduleException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.EnrollmentRepository;
import com.ing.hubs.service.CourseService;
import com.ing.hubs.service.ScheduleService;
import com.ing.hubs.service.SecurityService;
import com.ing.hubs.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private CourseService courseService;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Spy
    private ModelMapper modelMapper;
    @Mock
    private SecurityService securityService;
    @Mock
    private ScheduleService scheduleService;
    @Spy
    private EnrollmentValidationService validationService;
    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void shouldThrowUnauthorizedAccessExceptionWhenRoleIsNotStudent(){
        final UUID id = UUID.randomUUID();
        final User user = User.builder()
                        .role(Role.TEACHER)
                        .build();

        when(userService.findUserById(any(UUID.class))).thenReturn(user);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(id);

        assertThrows(UnauthorizedAccessException.class, () -> this.enrollmentService.createEnrollment(UUID.randomUUID(), anyString()));
        verify(userService, never()).saveUser(user);
        verify(courseService, never()).saveCourse(any(Course.class));
    }

    @Nested
    class InvalidDataCreateEnrollmentTests{
        private User user;
        @BeforeEach
        void setup(){
            this.user = User.builder()
                    .role(Role.STUDENT)
                    .build();
            when(userService.findUserById(any(UUID.class))).thenReturn(user);
            when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        }

        @AfterEach
        void checkIfSaved(){
            verify(userService, never()).saveUser(user);
            verify(courseService, never()).saveCourse(any(Course.class));
        }

        @Test
        void shouldNotSaveEnrollmentWhenCourseIsFull(){
            final Course course = Course.builder()
                    .capacity(0)
                    .startDate(LocalDate.now().plusDays(1))
                    .build();

            when(courseService.findCourseById(any(UUID.class))).thenReturn(course);

            assertThrows(CourseCapacityReachedException.class, () -> enrollmentService.createEnrollment(UUID.randomUUID(), anyString()));
        }

        @Test
        void shouldNotSaveEnrollmentWhenStudentIsAlreadyEnrolled(){
            final Enrollment enrollment = Enrollment.builder()
                    .user(user)
                    .build();
            final Course course = Course.builder()
                    .capacity(10)
                    .startDate(LocalDate.now().plusDays(1))
                    .enrollments(Set.of(enrollment))
                    .build();

            when(courseService.findCourseById(any(UUID.class))).thenReturn(course);

            assertThrows(StudentAlreadyEnrolledException.class, () -> enrollmentService.createEnrollment(UUID.randomUUID(), anyString()));
        }

        @Test
        void shouldNotSaveEnrollmentWhenSchedulesOverlap(){
            final Course course = Course.builder()
                    .capacity(10)
                    .startDate(LocalDate.now().plusDays(1))
                    .build();

            doThrow(InvalidScheduleException.class).when(scheduleService).validateScheduleOverlap(course, user);
            when(courseService.findCourseById(any(UUID.class))).thenReturn(course);

            assertThrows(InvalidScheduleException.class, () -> enrollmentService.createEnrollment(UUID.randomUUID(), anyString()));
        }
    }

    @Test
    void shouldSaveUserAndCourseAfterCreatingEnrollment(){
        final UUID enrollmentId = UUID.randomUUID();
        final User user = User.builder()
                .role(Role.STUDENT)
                .build();
        final Course course = Course.builder()
                .startDate(LocalDate.now().plusDays(1))
                .capacity(10)
                .build();

        when(userService.findUserById(any(UUID.class))).thenReturn(user);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(courseService.findCourseById(any(UUID.class))).thenReturn(course);
        when(enrollmentRepository.findEnrollmentByUserAndCourse(any(User.class), any(Course.class))).thenReturn(Optional.ofNullable(Enrollment.builder().id(enrollmentId).build()));

        final EnrollmentResponseDto responseDto = enrollmentService.createEnrollment(UUID.randomUUID(), anyString());

        verify(userService, times(1)).saveUser(user);
        verify(courseService, times(1)).saveCourse(any(Course.class));
        assertEquals(responseDto.getId(), enrollmentId);
    }

    @Nested
    class FindAllTests{
        private List<Enrollment> enrollments;
        private User user = User.builder()
                    .id(UUID.randomUUID())
                    .build();
        private Course course = new Course();

        @BeforeEach
        void setup(){
            final Enrollment enrollment1 = Enrollment.builder().status(EnrollmentStatus.PENDING).course(this.course).build();
            final Enrollment enrollment2 = Enrollment.builder().status(EnrollmentStatus.APPROVED).course(this.course).build();
            final Enrollment enrollment3 = Enrollment.builder().status(EnrollmentStatus.DENIED).course(this.course).build();
            final Enrollment enrollment4 = Enrollment.builder().status(EnrollmentStatus.ACTIVE).course(this.course).build();
            final Enrollment enrollment5 = Enrollment.builder().status(EnrollmentStatus.COMPLETED).course(this.course).build();
            final Enrollment enrollment6 = Enrollment.builder().status(EnrollmentStatus.CANCELED).course(this.course).build();

            this.enrollments =List.of(
                    enrollment1,
                    enrollment2,
                    enrollment3,
                    enrollment4,
                    enrollment5,
                    enrollment6);

            when(enrollmentRepository.findAll()).thenReturn(enrollments);
            lenient().when(securityService.extractUserIdFromToken(anyString())).thenReturn(this.user.getId());
            lenient().when(userService.findUserById(any(UUID.class))).thenReturn(this.user);
        }

        @Test
        void shouldReturnEnrollmentsBasedOnStatus(){
            assertEquals(enrollments, enrollmentService.findEnrollmentsByStatus(null));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.PENDING)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.PENDING));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.APPROVED)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.APPROVED));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.DENIED)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.DENIED));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.ACTIVE)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.ACTIVE));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.COMPLETED)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.COMPLETED));
            assertEquals(enrollments.stream().filter(e -> e.getStatus().equals(EnrollmentStatus.CANCELED)).toList(), enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.CANCELED));
        }

        @Test
        void shouldReturnAllEnrollmentsWhenFilterIsNull(){
            user.setRole(Role.TEACHER);
            course.setUser(this.user);

            final List<EnrollmentResponseDto> result = enrollmentService.findAll(null, anyString());

            verify(enrollmentRepository, times(1)).findAll();
            assertEquals(6, result.size());
        }

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "APPROVED", "DENIED", "ACTIVE", "COMPLETED", "CANCELED"})
        void shouldReturnAllEnrollmentsWhenFilterIsPending(final String filter){
            this.enrollments.forEach(enrollment -> enrollment.setUser(this.user));
            user.setRole(Role.STUDENT);
            course.setUser(this.user);

            final List<EnrollmentResponseDto> result = enrollmentService.findAll(EnrollmentStatus.valueOf(filter), anyString());

            verify(enrollmentRepository, times(1)).findAll();
            assertEquals(1, result.size());
        }
    }

    @Test
    void shouldThrowUnauthorizedAccessExceptionWhenRoleIsNotTeacher(){
        final User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.STUDENT);

        assertThrows(UnauthorizedAccessException.class, () -> enrollmentService.findAllEnrollmentsByCourse(UUID.randomUUID(), anyString()));
    }

    @Test
    void shouldThrowUnauthorizedAccessExceptionWhenCourseAndUserDontMatch(){
        final User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        final Course course = Course.builder()
                .user(user)
                .build();

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(course);

        assertThrows(UnauthorizedAccessException.class, () -> enrollmentService.findAllEnrollmentsByCourse(UUID.randomUUID(), anyString()));
    }

    @Test
    void shouldReturnAllEnrollmentsOfACourse(){
        final UUID userId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();
        final User user = User.builder()
                .id(userId)
                .build();
        final Course course = Course.builder()
                .id(courseId)
                .user(user)
                .build();

        final Enrollment enrollment1 = Enrollment.builder().course(course).grade(8).user(new User()).build();
        final Enrollment enrollment2 = Enrollment.builder().course(course).grade(9).user(new User()).build();

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(userId);
        when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);
        when(courseService.findCourseById(any(UUID.class))).thenReturn(course);
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment1, enrollment2));

        assertEquals(2, enrollmentService.findAllEnrollmentsByCourse(courseId, anyString()).size());
    }

    @Test
    void shouldThrowInvalidIdentifierExceptionWhenIdentifierIsInvalid(){
        assertThrows(InvalidIdentifierException.class, () -> enrollmentService.findAllEnrollmentsByUser(new User()));
    }

    @Nested
    class FindEnrollmentsOfStudentTests {
        private User user;
        private final UUID studentId = UUID.randomUUID();
        private List<Enrollment> enrollments;

        @BeforeEach
        void setup(){
            this.user = User.builder().id(this.studentId).build();

            final Course course1 = Course.builder().courseName("course1").build();
            final Course course2 = Course.builder().courseName("course2").build();
            final Course course3 = Course.builder().courseName("course3").build();

            final Enrollment enrollment1 = Enrollment.builder().course(course1).status(EnrollmentStatus.ACTIVE).grade(8).user(user).build();
            final Enrollment enrollment2 = Enrollment.builder().course(course2).status(EnrollmentStatus.APPROVED).grade(9).user(user).build();
            final Enrollment enrollment3 = Enrollment.builder().course(course3).status(EnrollmentStatus.PENDING).user(user).build();

            this.enrollments = List.of(enrollment1, enrollment2, enrollment3);
        }

        @Test
        void shouldReturnMapOfCourseNamesAndGrades() {
            final Map<String, Integer> expectedGrades = new HashMap<>();
            expectedGrades.put("course1", 8);
            expectedGrades.put("course2", 9);

            when(enrollmentRepository.findAll()).thenReturn(enrollments);
            when(securityService.extractUserIdFromToken(anyString())).thenReturn(user.getId());
            when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.STUDENT);
            when(enrollmentService.findAllEnrollmentsByUser(anyString())).thenReturn(enrollments);

            assertEquals(expectedGrades, enrollmentService.viewGrades(anyString()));
        }

        @Test
        void shouldThrowUnauthorizedAccessExWhenTeacherTriesToViewGrades(){
            when(securityService.extractRoleFromToken(anyString())).thenReturn(Role.TEACHER);

            assertThrows(UnauthorizedAccessException.class, () -> enrollmentService.viewGrades("token"));
        }

        @Test
        void shouldReturnAllEnrollmentsOfAStudent() {
            when(enrollmentRepository.findAll()).thenReturn(enrollments);
            when(securityService.extractUserIdFromToken(anyString())).thenReturn(user.getId());
            when(enrollmentService.findAllEnrollmentsByUser(anyString())).thenReturn(enrollments);

            assertEquals(enrollments, enrollmentService.findAllEnrollmentsByUser(anyString()));
            assertEquals(enrollments, enrollmentService.findAllEnrollmentsByUser(studentId));
        }
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotInDb(){
        when(enrollmentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> enrollmentService.findEnrollmentById(UUID.randomUUID()));
        verify(enrollmentRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void shouldReturnAllSchedulesOfAStudent(){
        final UUID userId = UUID.randomUUID();
        final User user = User.builder().id(userId).build();

        final Schedule schedule1 = new Schedule();
        final Schedule schedule2 = new Schedule();
        final Schedule schedule3 = new Schedule();

        final Course course1 = Course.builder().courseName("course1").schedules(Set.of(schedule1)).build();
        final Course course2 = Course.builder().courseName("course2").schedules(Set.of(schedule2)).build();
        final Course course3 = Course.builder().courseName("course3").schedules(Set.of(schedule3)).build();

        final Enrollment enrollment1 = Enrollment.builder().course(course1).status(EnrollmentStatus.ACTIVE).grade(8).user(user).build();
        final Enrollment enrollment2 = Enrollment.builder().course(course2).status(EnrollmentStatus.ACTIVE).grade(9).user(user).build();
        final Enrollment enrollment3 = Enrollment.builder().course(course3).status(EnrollmentStatus.ACTIVE).user(user).build();

        final List<Enrollment> enrollments = List.of(enrollment1, enrollment2, enrollment3);

        when(enrollmentRepository.findAll()).thenReturn(enrollments);
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(userId);

        final List<StudentsScheduleResponseDto> schedules = enrollmentService.findStudentsActiveSchedules("token");
        final List<String> expectedCourseNames = List.of(course1.getCourseName(), course2.getCourseName(), course3.getCourseName());

        IntStream.range(0, schedules.size())
                .forEach(i -> assertEquals(expectedCourseNames.get(i), schedules.get(i).getCourseName()));
    }
}