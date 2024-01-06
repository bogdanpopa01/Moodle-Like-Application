package com.ing.hubs.service;

import com.ing.hubs.dto.course.CourseDto;
import com.ing.hubs.dto.schedule.ScheduleDto;
import com.ing.hubs.exception.course.CouldNotCreateCourseException;
import com.ing.hubs.exception.course.InvalidScheduleException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.CourseType;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.course.schedule.Weekday;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;

import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.ScheduleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Spy
    private ModelMapper modelMapper;
    @InjectMocks
    private ScheduleService scheduleService;

    @Nested
    class InvalidSchedulesValidationTests {
        private final Set<Schedule> schedules = new HashSet<>();
        @Test
        void shouldThrowExceptionIfStartTimeIsAfterEndTime() {
            final Schedule schedule = Schedule
                    .builder()
                    .startTime(LocalTime.of(10, 30))
                    .endTime(LocalTime.of(9, 30))
                    .build();
            this.schedules.add(schedule);
            final InvalidScheduleException exception = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(schedules));
            assertThat(exception.getMessage(), containsString("Start time cannot be after end time!"));
        }

        @Test
        void shouldThrowExceptionIfStartTimeIsInvalid() {
            final Schedule schedule = Schedule
                    .builder()
                    .startTime(LocalTime.of(8, 31))
                    .endTime(LocalTime.of(9, 30))
                    .build();
            this.schedules.add(schedule);

            final InvalidScheduleException exception = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(schedules));
            assertThat(exception.getMessage(), containsString("Invalid start time!"));

        }

        @Test
        void shouldThrowExceptionIfEndTimeIsInvalid() {
            final Schedule schedule = Schedule
                    .builder()
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(9, 31))
                    .build();
            this.schedules.add(schedule);

            final InvalidScheduleException exception = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(schedules));
            assertThat(exception.getMessage(), containsString("Invalid end time!"));
        }

        @Test
        void shouldThrowExceptionIfCourseLongerThanThreeHours(){
            final Schedule schedule = Schedule
                    .builder()
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(19, 30))
                    .build();
            this.schedules.add(schedule);

            final InvalidScheduleException exception = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(schedules));
            assertThat(exception.getMessage(), containsString("The course cannot be longer than 3 hours!"));
        }

        @Test
        void shouldThrowExceptionIfStartTimeIsAfterSixPm(){
            final Schedule schedule = Schedule
                    .builder()
                    .startTime(LocalTime.of(18, 30))
                    .endTime(LocalTime.of(19, 30))
                    .build();
            this.schedules.add(schedule);

            final InvalidScheduleException exception = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(schedules));
            assertThat(exception.getMessage(), containsString("Start time cannot be after 18:00"));
        }

        @Test
        void shouldThrowExceptionIfCoursesSchedulesOverlap(){
            final Schedule schedule1 = Schedule.builder()
                    .startTime(LocalTime.of(10,0))
                    .endTime(LocalTime.of(12,0))
                    .courseType(CourseType.COURSE)
                    .weekday(Weekday.MONDAY)
                    .build();
            final Schedule schedule2 = Schedule.builder()
                    .startTime(LocalTime.of(11,0))
                    .endTime(LocalTime.of(13,0))
                    .courseType(CourseType.SEMINAR)
                    .weekday(Weekday.MONDAY)
                    .build();
            final Schedule schedule3 = Schedule.builder()
                    .startTime(LocalTime.of(11,0))
                    .endTime(LocalTime.of(13,0))
                    .courseType(CourseType.LAB)
                    .weekday(Weekday.TUESDAY)
                    .build();

            assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(Set.of(schedule1, schedule2, schedule3)));
        }

        @Test
        void shouldThrowExIfCourseLengthIsLessThan30Minutes(){
            final Schedule schedule1 = Schedule.builder()
                    .startTime(LocalTime.of(10,0))
                    .endTime(LocalTime.of(10,0))
                    .weekday(Weekday.MONDAY)
                    .build();

            final InvalidScheduleException ex = assertThrows(InvalidScheduleException.class, () -> scheduleService.validateSchedules(Set.of(schedule1)));
            assertEquals("Invalid schedule duration. Must be at least 30 minutes between start time and end time.", ex.getMessage());
        }

        @Test
        void shouldThrowCouldNotCreateCourseExceptionWhenNoCourseSchedule(){
            final Schedule schedule1 = Schedule.builder()
                    .startTime(LocalTime.of(10,0))
                    .endTime(LocalTime.of(12,0))
                    .courseType(CourseType.SEMINAR)
                    .weekday(Weekday.MONDAY)
                    .build();
            final Schedule schedule2 = Schedule.builder()
                    .startTime(LocalTime.of(12,0))
                    .endTime(LocalTime.of(14,0))
                    .courseType(CourseType.LAB)
                    .weekday(Weekday.MONDAY)
                    .build();

            CouldNotCreateCourseException ex = assertThrows(CouldNotCreateCourseException.class, () -> scheduleService.validateSchedules(Set.of(schedule1, schedule2)));
            assertEquals("Schedule of type \"Course\" is mandatory!", ex.getMessage());
        }

        @Test
        void shouldThrowCouldNotCreateCourseExceptionWhenDuplicateCourseType(){
            final Schedule schedule1 = Schedule.builder()
                    .startTime(LocalTime.of(10,0))
                    .endTime(LocalTime.of(12,0))
                    .courseType(CourseType.COURSE)
                    .weekday(Weekday.MONDAY)
                    .build();
            final Schedule schedule2 = Schedule.builder()
                    .startTime(LocalTime.of(12,0))
                    .endTime(LocalTime.of(14,0))
                    .courseType(CourseType.COURSE)
                    .weekday(Weekday.MONDAY)
                    .build();

            CouldNotCreateCourseException ex = assertThrows(CouldNotCreateCourseException.class, () -> scheduleService.validateSchedules(Set.of(schedule1, schedule2)));
            assertEquals("Duplicate course types!", ex.getMessage());
        }
    }

    @Test
    void shouldCallScheduleRepositoryWhenFindingByCourse() {
        final Course course = new Course();
        final Set<Schedule> expectedSchedules = new HashSet<>();
        when(this.scheduleRepository.findSchedulesByCourse(course)).thenReturn(expectedSchedules);

        final Set<Schedule> actualSchedules = this.scheduleService.findSchedulesByCourse(course);
        verify(this.scheduleRepository, times(1)).findSchedulesByCourse(any(Course.class));

        assertEquals(expectedSchedules, actualSchedules);
    }

    @Test
    void shouldMapScheduleDtoToScheduleForValidCourse() {
        final CourseDto courseDto = new CourseDto();

        final ScheduleDto scheduleDto1 = new ScheduleDto(
                CourseType.SEMINAR,
                LocalTime.of(10, 30),
                LocalTime.of(11, 30),
                Weekday.MONDAY);

        final ScheduleDto scheduleDto2 = new ScheduleDto(
                CourseType.COURSE,
                LocalTime.of(11, 30),
                LocalTime.of(12, 30),
                Weekday.MONDAY);

        courseDto.setSchedules(Set.of(scheduleDto1, scheduleDto2));

        final Schedule schedule1 = Schedule.builder()
                .courseType(CourseType.SEMINAR)
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .weekday(Weekday.SATURDAY)
                .build();

        final Schedule schedule2 = Schedule.builder()
                .courseType(CourseType.COURSE)
                .startTime(LocalTime.of(11, 30))
                .endTime(LocalTime.of(12, 30))
                .weekday(Weekday.SATURDAY)
                .build();

        when(this.modelMapper.map(scheduleDto1, Schedule.class)).thenReturn(schedule1);
        when(this.modelMapper.map(scheduleDto2, Schedule.class)).thenReturn(schedule2);

        final Set<Schedule> resultSchedules = this.scheduleService.mapDtoToSchedules(courseDto);

        verify(this.modelMapper, times(1)).map(scheduleDto1, Schedule.class);
        verify(this.modelMapper, times(1)).map(scheduleDto2, Schedule.class);

        final Set<Schedule> expectedSchedules = Set.of(schedule1, schedule2);
        assertEquals(expectedSchedules, resultSchedules);
    }

    @Test
    void shouldReturnSchedulesFromEnrollment() {
        final Enrollment enrollment = Enrollment
                .builder()
                .id(UUID.randomUUID())
                .status(EnrollmentStatus.ACTIVE)
                .grade(7)
                .build();

        final Course course = Course
                .builder()
                .courseName("java")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .id(UUID.randomUUID())
                .description("Desc")
                .capacity(100)
                .credits(6)
                .build();

        final Schedule schedule = new Schedule();
        schedule.setStartTime(LocalTime.of(10, 30));
        schedule.setEndTime(LocalTime.of(11, 30));
        schedule.setWeekday(Weekday.FRIDAY);
        schedule.setCourseType(CourseType.SEMINAR);

        final Set<Schedule> schedules = new HashSet<>();
        schedules.add(schedule);
        course.addSchedules(schedules);
        enrollment.setCourse(course);

        final List<Schedule> scheduleList = this.scheduleService.getSchedulesFromEnrollment(enrollment);
        final Set<Schedule> scheduleSet = new HashSet<>(scheduleList);
        assertNotNull(scheduleList);
        assertEquals(schedules, scheduleSet);
    }

    @Test
    void shouldThrowInvalidScheduleExWhenSchedulesOverlap(){
        final User student = new User();

        final Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .courseType(CourseType.COURSE)
                .startTime(LocalTime.of(10,0))
                .endTime(LocalTime.of(12,0))
                .weekday(Weekday.MONDAY)
                .build();
        final Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .courseType(CourseType.COURSE)
                .startTime(LocalTime.of(11,0))
                .endTime(LocalTime.of(13,0))
                .weekday(Weekday.MONDAY)
                .build();

        final Course course1 = Course.builder()
                .schedules(Set.of(schedule1))
                .build();
        final Course course2 = Course.builder()
                .schedules(Set.of(schedule2))
                .build();

        final Enrollment enrollment1 = Enrollment.builder()
                .status(EnrollmentStatus.APPROVED)
                .course(course1)
                .user(student)
                .build();

        student.setEnrollments(Set.of(enrollment1));

        assertThrows(InvalidScheduleException.class, () -> this.scheduleService.validateScheduleOverlap(course2, student));
    }
}