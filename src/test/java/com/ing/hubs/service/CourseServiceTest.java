package com.ing.hubs.service;

import com.ing.hubs.dto.course.CourseDto;
import com.ing.hubs.dto.course.CoursePatchDto;
import com.ing.hubs.dto.course.CourseResponseDto;
import com.ing.hubs.exception.CouldNotDeleteEntityException;
import com.ing.hubs.exception.course.CouldNotCreateCourseException;
import com.ing.hubs.exception.DuplicateDataException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private CourseService courseService;

    @Nested
    class CouldNotCreateCourseCourseValidationTests {
        private final CourseDto courseDto = new CourseDto();
        private final String jwtToken = "validToken";

        @Test
        void shouldThrowExceptionWhenInvalidCapacity() {
            final int capacity = 5;

            courseDto.setCapacity(capacity);

            courseDto.setStartDate(LocalDate.now().plusDays(1));
            courseDto.setEndDate(LocalDate.now().plusDays(10));

            CouldNotCreateCourseException exception = assertThrows(CouldNotCreateCourseException.class, () -> courseService.createCourse(courseDto, jwtToken));
            assertThat(exception.getMessage(), containsString("Minimum capacity is 10 students!"));
        }

        @Test
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            final LocalDate startDate = LocalDate.now().plusDays(12);
            final LocalDate endDate = LocalDate.now().plusDays(11);

            courseDto.setEndDate(endDate);
            courseDto.setStartDate(startDate);

            CouldNotCreateCourseException exception = assertThrows(CouldNotCreateCourseException.class,
                    () -> courseService.createCourse(courseDto, jwtToken));
            assertThat(exception.getMessage(), containsString("Start date cannot be after end date!"));
        }

        @Test
        void shouldThrowExceptionWhenStartDateIsBeforeCurrentDate() {
            final LocalDate startDate = LocalDate.now().minusDays(1);
            final LocalDate endDate = LocalDate.now().plusDays(11);

            courseDto.setEndDate(endDate);
            courseDto.setStartDate(startDate);

            CouldNotCreateCourseException exception = assertThrows(CouldNotCreateCourseException.class,
                    () -> courseService.createCourse(courseDto, jwtToken));
            assertThat(exception.getMessage(), containsString("Start date cannot be before current date!"));
        }

        @Test
        void shouldThrowExceptionWhenEndDateIsMoreThanTwoYearsInTheFuture() {
            final LocalDate startDate = LocalDate.now().plusDays(1500);
            final LocalDate endDate = LocalDate.now().plusDays(2000);

            courseDto.setEndDate(endDate);
            courseDto.setStartDate(startDate);

            CouldNotCreateCourseException exception = assertThrows(CouldNotCreateCourseException.class,
                    () -> courseService.createCourse(courseDto, jwtToken));
            assertThat(exception.getMessage(), containsString("Course can not be created more than 2 years the future!"));
        }
    }

    @Nested
    class CourseDeletionTests {
        private final UUID courseId = UUID.randomUUID();
        private final UUID userId = UUID.randomUUID();
        private final String jwtToken = "validToken";
        private final Course course = new Course();
        private final User user = new User();

        @Test
        public void shouldDeleteCourseWhenAuthorizedUser() {
            user.setId(userId);
            course.setUser(user);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(userId);
            when(userService.findUserById(userId)).thenReturn(user);

            courseService.deleteById(courseId, jwtToken);
            verify(courseRepository, times(1)).deleteById(courseId);
        }

        @Test
        public void shouldThrownExceptionForCourseDeletionWhenUserUnauthorized() {
            user.setId(UUID.randomUUID());
            course.setUser(user);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(UUID.randomUUID());
            when(userService.findUserById(any(UUID.class))).thenReturn(new User());

            assertThrows(UnauthorizedAccessException.class, () -> courseService.deleteById(courseId, jwtToken));
        }

        @Test
        void shouldThrowCouldNotDeleteCourseExWhenDeletingCourseWithActiveAndApprovedEnrollments(){
            final Enrollment enrollment = Enrollment.builder()
                    .status(EnrollmentStatus.ACTIVE)
                    .course(this.course)
                    .build();
            this.course.setEnrollments(Set.of(enrollment));
            this.user.setId(this.userId);
            this.course.setUser(this.user);

            when(courseRepository.findById(any(UUID.class))).thenReturn(Optional.of(this.course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(userId);
            when(userService.findUserById(userId)).thenReturn(user);


            var ex = assertThrows(CouldNotDeleteEntityException.class, () -> courseService.deleteById(courseId, jwtToken));
            assertEquals("Students are enrolled in this course", ex.getMessage());
        }
    }

    @Nested
    class CourseCreationTests {
        private final CourseDto courseDto = new CourseDto();
        private final String jwtToken = "validToken";
        private final UUID userId = UUID.randomUUID();

        @BeforeEach
        void beforeEach() {
            courseDto.setStartDate(LocalDate.now().plusDays(1));
            courseDto.setEndDate(LocalDate.now().plusDays(10));
            courseDto.setCapacity(20);
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(userId);
        }

        @Test
        void shouldCreateCourseWhenValidArguments() {
            final User teacher = User.builder()
                    .role(Role.TEACHER)
                    .build();

            when(userService.findUserById(userId)).thenReturn(teacher);

            final Course course = Course.builder()
                    .id(UUID.randomUUID())
                    .build();

            when(modelMapper.map(courseDto, Course.class)).thenReturn(course);

            final Set<Schedule> schedules = new HashSet<>();

            when(scheduleService.mapDtoToSchedules(courseDto)).thenReturn(schedules);

            when(modelMapper.map(course, CourseResponseDto.class)).thenReturn(new CourseResponseDto());
            when(courseRepository.findByCourseName(course.getCourseName())).thenReturn(Optional.of(course));

            final CourseResponseDto result = courseService.createCourse(courseDto, jwtToken);

            assertNotNull(result);
            verify(userService, times(1)).saveUser(teacher);
        }

        @Test
        void shouldThrowUnauthorizedAccessExceptionForNonTeacher() {
            final User student = User.builder()
                    .role(Role.STUDENT)
                    .build();

            when(userService.findUserById(userId)).thenReturn(student);

            assertThrows(UnauthorizedAccessException.class, () -> courseService.createCourse(courseDto, jwtToken));
            verify(userService, never()).saveUser(any());
        }
    }

    @Test
    void shouldThrowCouldNotCreateCourseExWhenCourseIsLongerThanOneYear(){
        final CourseDto courseDto = new CourseDto();
        courseDto.setStartDate(LocalDate.now());
        courseDto.setEndDate(LocalDate.now().plusYears(2));
        courseDto.setCourseName("courseName");

        when(courseRepository.existsByCourseName(courseDto.getCourseName())).thenReturn(false);

        var ex = assertThrows(CouldNotCreateCourseException.class, () -> courseService.createCourse(courseDto, "token"));
        assertEquals("Course duration can not be more than 1 year!", ex.getMessage());
        verify(userService, never()).saveUser(any());
    }

    @Test
    void shouldThrowCouldNotCreateCourseExWhenCourseLengthIsLessThanOneDay(){
        final CourseDto courseDto = new CourseDto();
        courseDto.setStartDate(LocalDate.now());
        courseDto.setEndDate(LocalDate.now());
        courseDto.setCourseName("courseName");

        when(courseRepository.existsByCourseName(courseDto.getCourseName())).thenReturn(false);
        var ex = assertThrows(CouldNotCreateCourseException.class, () -> courseService.createCourse(courseDto, "token"));
        assertEquals("Invalid course duration. The course must be at least one day long!", ex.getMessage());
        verify(userService, never()).saveUser(any());
    }

    @Nested
    class CourseUpdateTests {
        private final CoursePatchDto coursePatchDto = new CoursePatchDto();
        private final UUID courseId = UUID.randomUUID();
        private final UUID userId = UUID.randomUUID();
        private final String jwtToken = "validToken";
        private final User user = new User();
        private final Course course = new Course();

        @BeforeEach
        void beforeEach() {
            user.setId(userId);
            course.setUser(user);
        }

        @Test
        void shouldUpdateCourseWhenValidArguments() {
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(userId);
            when(userService.findUserById(userId)).thenReturn(user);
            when(modelMapper.map(any(Course.class), eq(CourseResponseDto.class)))
                    .thenReturn(new CourseResponseDto());

            final CourseResponseDto result = courseService.updateCourse(courseId, coursePatchDto, jwtToken);

            assertNotNull(result);
            verify(courseRepository, times(1)).findById(courseId);
            verify(userService, times(1)).findUserById(userId);
        }

        @Test
        void shouldThrowExceptionWhenDuplicateCourseNameForUpdate() {
            coursePatchDto.setCourseName("newCourseName");

            when(courseRepository.existsByCourseName(coursePatchDto.getCourseName())).thenReturn(true);
            assertThrows(DuplicateDataException.class, () -> courseService.updateCourse(courseId, coursePatchDto, jwtToken));
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowExceptionWhenNotAuthorizedForCourseUpdate() {

            user.setRole(Role.STUDENT);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(UUID.randomUUID());
            when(userService.findUserById(any(UUID.class))).thenReturn(new User());
            assertThrows(UnauthorizedAccessException.class, () -> courseService.updateCourse(courseId, coursePatchDto, jwtToken));
        }

        @Test
        void shouldThrowExceptionWhenCourseCapacityGetsLower() {
            coursePatchDto.setCourseName("name");

            course.setCapacity(40);
            user.setRole(Role.TEACHER);

            coursePatchDto.setCapacity(course.getCapacity() - 1);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(userId);
            when(userService.findUserById(userId)).thenReturn(user);

            CouldNotCreateCourseException exception = assertThrows(CouldNotCreateCourseException.class, () -> courseService.updateCourse(courseId, coursePatchDto, jwtToken));
            assertThat(exception.getMessage(), containsString("Course capacity cannot be reduced!"));
        }
    }

    @Test
    void shouldCallCourseRepositoryWhenFindingById() {
        final UUID id = UUID.randomUUID();

        when(modelMapper.map(any(Course.class), eq(CourseResponseDto.class)))
                .thenReturn(new CourseResponseDto());
        when(courseRepository.findById(id)).thenReturn(Optional.of(new Course()));

        courseService.findById(id);
        courseService.findCourseById(id);

        verify(courseRepository, times(2)).findById(id);
    }

    @Test
    void shouldReturnAllCoursesWhenArgumentIsNull() {
        when(courseRepository.findAll()).thenReturn(List.of(new Course(), new Course()));
        when(modelMapper.map(any(Course.class), eq(CourseResponseDto.class))).thenReturn(new CourseResponseDto());

        final List<CourseResponseDto> courseResponseDtos = courseService.findAllCourses(null);

        verify(courseRepository, times(1)).findAll();
        assertEquals(2, courseResponseDtos.size());
    }

    @Test
    void shouldFilterCoursesWhenArgumentIsNotNull() {
        final UUID teacherId = UUID.randomUUID();

        final Course course1 = Course.builder()
                .courseName("Java")
                .user(User.builder().id(teacherId).build())
                .build();

        final Course course2 = Course.builder()
                .courseName("C#")
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        final CourseResponseDto responseDto1 = new CourseResponseDto();
        final CourseResponseDto responseDto2 = new CourseResponseDto();

        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));
        when(modelMapper.map(any(Course.class), eq(CourseResponseDto.class)))
                .thenReturn(responseDto1)
                .thenReturn(responseDto2);

        final List<CourseResponseDto> result = courseService.findAllCourses(teacherId);

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionWhenCourseNameIsNotUnique() {
        final CourseDto courseDto = new CourseDto();
        courseDto.setCourseName("notUniqueName");

        final String jwtToken = "validToken";

        when(courseRepository.existsByCourseName(courseDto.getCourseName())).thenReturn(true);
        assertThrows(DuplicateDataException.class, () -> courseService.createCourse(courseDto, jwtToken));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void shouldSaveCourseWhenValidArguments() {
        final Course course = Course
                .builder()
                .id(UUID.randomUUID())
                .courseName("name")
                .description("desc")
                .capacity(200)
                .credits(6)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();
        courseService.saveCourse(course);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void shouldThrowExceptionWhenDataIntegrityViolated() {
        when(courseRepository.save(any(Course.class)))
                .thenThrow(new DataIntegrityViolationException("course.UK_9dll001xc2cip6hug6axoab0p"));

        assertThrows(DuplicateDataException.class, () -> this.courseService.saveCourse(new Course()));
    }

    @Test
    void shouldCreateCourseResponseDtoWhenValidCourseProvided() {
        final Course course = new Course();
        final Set<Schedule> schedules = new HashSet<>();

        when(scheduleService.findSchedulesByCourse(course)).thenReturn(schedules);

        final CourseResponseDto mappedResponse = new CourseResponseDto();

        when(modelMapper.map(course, CourseResponseDto.class)).thenReturn(mappedResponse);

        final CourseResponseDto result = courseService.createCourseResponse(course);

        assertNotNull(result);
        assertEquals(mappedResponse, result);
        assertEquals(schedules, result.getSchedules());
    }
}