package com.ing.hubs.service;

import com.ing.hubs.dto.course.CourseDto;
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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScheduleService {
    private ScheduleRepository scheduleRepository;
    private ModelMapper modelMapper;

    public ScheduleService() {
    }

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, ModelMapper modelMapper) {
        this.scheduleRepository = scheduleRepository;
        this.modelMapper = modelMapper;
    }

    public Set<Schedule> findSchedulesByCourse(final Course course) {
        return this.scheduleRepository.findSchedulesByCourse(course);
    }

    public Set<Schedule> mapDtoToSchedules(final CourseDto dto) {
        return dto.getSchedules()
                .stream()
                .map(schedule ->
                        this.modelMapper.map(schedule, Schedule.class))
                .collect(Collectors.toSet());
    }

    public List<Schedule> getSchedulesFromEnrollment(final Enrollment enrollment) {
        return enrollment.getCourse().getSchedules().stream().toList();

    }

    public void validateScheduleOverlap(final Course course,
                                        final User student) {
        final List<Schedule> studentsSchedules = student.getEnrollments()
                .stream()
                .filter(enrollment -> (enrollment.getStatus().equals(EnrollmentStatus.APPROVED)) || enrollment.getStatus().equals(EnrollmentStatus.ACTIVE))
                .map(Enrollment::getCourse)
                .flatMap(course1 -> course1.getSchedules().stream())
                .toList();

        final List<Schedule> coursesSchedules = course.getSchedules().stream().toList();

        this.checkOverlap(coursesSchedules, studentsSchedules);
    }


    private void checkOverlap(final List<Schedule> coursesSchedules,
                              final List<Schedule> studentsSchedules) {
        final List<Weekday> studentsBusyWeekdays = this.findBusyDays(studentsSchedules);
        for (Weekday weekday : studentsBusyWeekdays) {
            final List<Schedule> allSchedules = this.concatAndSortScheduleLists(coursesSchedules, studentsSchedules, weekday);
            this.compareStartAndEndTimes(allSchedules);}
    }

    private void compareStartAndEndTimes(final List<Schedule> sortedDistinctSchedules) {
        for (int i = 1; i < sortedDistinctSchedules.size(); i++) {
            final Schedule current = sortedDistinctSchedules.get(i);
            final Schedule previous = sortedDistinctSchedules.get(i - 1);

            if (maxOfStartTimes(current, previous) < minOfEndTimes(current, previous)) {
                throw new InvalidScheduleException("Schedules overlap!");
            }
        }
    }

    private List<Schedule> concatAndSortScheduleLists(final List<Schedule> courseSchedules,
                                                      final List<Schedule> studentSchedules,
                                                      final Weekday weekday) {
        return Stream.concat(
                        studentSchedules.stream()
                                .filter(schedule -> schedule.getWeekday().equals(weekday)),
                        courseSchedules.stream()
                                .filter(schedule -> schedule.getWeekday().equals(weekday)))
                .sorted(Comparator.comparing(Schedule::getStartTime))
                .toList();
    }

    private int maxOfStartTimes(final Schedule schedule1,
                                final Schedule schedule2) {
        return Math.max(schedule1.getStartTime().toSecondOfDay(), schedule2.getStartTime().toSecondOfDay());
    }

    private int minOfEndTimes(final Schedule schedule1,
                              final Schedule schedule2) {
        return Math.min(schedule1.getEndTime().toSecondOfDay(), schedule2.getEndTime().toSecondOfDay());
    }

    private List<Weekday> findBusyDays(final List<Schedule> schedules) {
        return schedules.stream()
                .map(Schedule::getWeekday)
                .distinct()
                .toList();
    }

    void validateSchedules(final Set<Schedule> schedules) {
        schedules.forEach(schedule -> {
            if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
                throw new InvalidScheduleException("Start time cannot be after end time!");
            }

            if (schedule.getStartTime().getMinute() != 0 &&
                    schedule.getStartTime().getMinute() != 30) {
                throw new InvalidScheduleException("Invalid start time!");
            }

            if (schedule.getEndTime().getMinute() != 0 &&
                    schedule.getEndTime().getMinute() != 30) {
                throw new InvalidScheduleException("Invalid end time!");
            }

            final LocalTime maxStartTime = LocalTime.of(18, 0);
            if (schedules.stream().anyMatch(sch -> sch.getStartTime().isAfter(maxStartTime))) {
                throw new InvalidScheduleException("Start time cannot be after 18:00");
            }

            final int maxCourseDurationLength = 3;
            if (schedules.stream().anyMatch(sch -> Duration.between(sch.getStartTime(), sch.getEndTime()).toHours() > maxCourseDurationLength)) {
                throw new InvalidScheduleException("The course cannot be longer than 3 hours!");
            }

            if(schedule.getEndTime().isBefore(schedule.getStartTime().plusMinutes(30))){
                throw new InvalidScheduleException("Invalid schedule duration. Must be at least 30 minutes between start time and end time.");
            }

            this.checkScheduleTypes(schedules);
            this.checkOverlapForCourse(schedules);
        });
    }

    private void checkOverlapForCourse(final Set<Schedule> schedules) {
        final List<Schedule> scheduleList = new ArrayList<>(schedules);
        for (Weekday weekday : this.findBusyDays(scheduleList)) {
            final List<Schedule> sortedSchedule = schedules.stream()
                    .filter(schedule1 -> schedule1.getWeekday().equals(weekday))
                    .sorted(Comparator.comparing(Schedule::getStartTime))
                    .toList();
            if (sortedSchedule.size() == 1) {
                continue;
            }
            this.compareStartAndEndTimes(sortedSchedule);
        }
    }

    private void checkScheduleTypes(final Set<Schedule> schedules){
        if (schedules.stream().noneMatch(schedule -> schedule.getCourseType().equals(CourseType.COURSE))){
            throw new CouldNotCreateCourseException("Schedule of type \"Course\" is mandatory!");
        }

        final List<CourseType> scheduleTypesList = schedules.stream()
                .map(Schedule::getCourseType)
                .toList();
        final Set<CourseType> scheduleTypesSet = new HashSet<>(scheduleTypesList);

        if (scheduleTypesList.size() != scheduleTypesSet.size()) {
            throw new CouldNotCreateCourseException("Duplicate course types!");
        }
    }
}