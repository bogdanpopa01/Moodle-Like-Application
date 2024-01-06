package com.ing.hubs.service;

import com.ing.hubs.dto.course.CourseDto;
import com.ing.hubs.dto.course.CoursePatchDto;
import com.ing.hubs.dto.course.CourseResponseDto;
import com.ing.hubs.dto.schedule.ScheduleResponseDto;
import com.ing.hubs.exception.CouldNotDeleteEntityException;
import com.ing.hubs.exception.EntityCouldNotSavedException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.exception.course.CouldNotCreateCourseException;
import com.ing.hubs.exception.DuplicateDataException;
import com.ing.hubs.exception.EntityNotFoundException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ScheduleService scheduleService;
    private final SecurityService securityService;
    private final Integer minimumCapacity = 10;

    @Autowired
    public CourseService(final CourseRepository courseRepository,
                         final UserService userService,
                         final ModelMapper modelMapper,
                         final ScheduleService scheduleService,
                         final SecurityService securityService) {
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.scheduleService = scheduleService;
        this.securityService = securityService;
    }

    public CourseResponseDto createCourse(final CourseDto dto,
                                          final String jwtToken) {
        this.validateUniqueData(dto.getCourseName());
//      For Postman collection to work comment this
        this.validateStartAndEndDate(dto.getStartDate(), dto.getEndDate());
//
        this.validateMinimumCapacity(dto.getCapacity());

        final Course course = modelMapper.map(dto, Course.class);
        final User user = this.userService.findUserById(this.securityService.extractUserIdFromToken(jwtToken));

        if (!user.getRole().equals(Role.TEACHER)) {
            throw new UnauthorizedAccessException();
        }

        final Set<Schedule> schedules = this.scheduleService.mapDtoToSchedules(dto);
        this.scheduleService.validateSchedules(schedules);

        course.addSchedules(schedules);
        user.addCourse(course);

        this.userService.saveUser(user);

        final Course savedCourse = this.courseRepository.findByCourseName(course.getCourseName())
                .orElseThrow(() -> new EntityCouldNotSavedException("Course"));
        log.info(String.format("Course with id \"%s\" has been created", savedCourse.getId()));

        return this.createCourseResponse(course);
    }

    public CourseResponseDto createCourseResponse(final Course course) {
        Set<Schedule> scheduleResponse = this.scheduleService.findSchedulesByCourse(course);

        var response = this.modelMapper.map(
                course,
                CourseResponseDto.class);

        final Set<ScheduleResponseDto> scheduleResponseDtos = new HashSet<>();
        scheduleResponse
                .stream()
                .map((element) -> modelMapper.map(element, ScheduleResponseDto.class))
                .forEach(scheduleResponseDtos::add);
        response.setSchedules(scheduleResponseDtos);

        return response;
    }

    public List<CourseResponseDto> findAllCourses(final UUID teacherId) {
        return courseRepository.findAll().stream()
                .filter(course -> teacherId == null || course.getUser().getId().equals(teacherId))
                .map(course -> modelMapper.map(course, CourseResponseDto.class))
                .toList();
    }

    public Course findCourseById(final UUID id) {
        return this.courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Course"));
    }

    public CourseResponseDto findById(final UUID id) {
        return this.modelMapper.map(
                this.courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Course")), CourseResponseDto.class);
    }

    @Transactional
    public void deleteById(final UUID id,
                           final String jwtToken) {
        final Course course = this.findCourseById(id);
        final UUID teacherId = this.securityService.extractUserIdFromToken(jwtToken);

        final User user = userService.findUserById(teacherId);

        if (course.getUser().getId() != user.getId()) {
            throw new UnauthorizedAccessException();
        }

        if (course.getEnrollments().stream()
                .anyMatch(enrollment ->
                        enrollment.getStatus().equals(EnrollmentStatus.APPROVED) ||
                        enrollment.getStatus().equals(EnrollmentStatus.ACTIVE))){
            throw new CouldNotDeleteEntityException("Students are enrolled in this course");
        }

        courseRepository.deleteById(id);
        log.info(String.format("Course with id \"%s\" has been deleted", course.getId()));
    }

    public CourseResponseDto updateCourse(final UUID courseId,
                                          final CoursePatchDto coursePatchDto,
                                          final String jwtToken) {
        if (coursePatchDto.getCourseName() != null) {
            this.validateUniqueData(coursePatchDto.getCourseName());
        }

        final Course course = this.findCourseById(courseId);
        final User user = this.userService.findUserById(this.securityService.extractUserIdFromToken(jwtToken));

        if (course.getUser().getId() != user.getId()) {
            throw new UnauthorizedAccessException();
        }

        if (coursePatchDto.getCapacity() != null && coursePatchDto.getCapacity() < course.getCapacity()) {
            throw new CouldNotCreateCourseException("Course capacity cannot be reduced!");
        }

        Optional.ofNullable(coursePatchDto.getCourseName()).ifPresent(course::setCourseName);
        Optional.ofNullable(coursePatchDto.getDescription()).ifPresent(course::setDescription);
        Optional.ofNullable(coursePatchDto.getCapacity()).ifPresent(course::setCapacity);
        Optional.ofNullable(coursePatchDto.getCredits()).ifPresent(course::setCredits);

        this.saveCourse(course);
        log.info(String.format("Course with id \"%s\" has been updated", course.getId()));

        return this.modelMapper.map(course, CourseResponseDto.class);
    }


    public void saveCourse(final Course course) {
        try {
            this.courseRepository.save(course);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDataException(ex);
        }
    }

    private void validateStartAndEndDate(final LocalDate startDate,
                                         final LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CouldNotCreateCourseException("Start date cannot be after end date!");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new CouldNotCreateCourseException("Start date cannot be before current date!");
        }

        if (startDate.isAfter(LocalDate.now().plusDays(730))) {
            throw new CouldNotCreateCourseException("Course can not be created more than 2 years the future!");
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) > 365){
            throw new CouldNotCreateCourseException("Course duration can not be more than 1 year!");
        }
        if (endDate.isBefore(startDate.plusDays(1))) {
            throw new CouldNotCreateCourseException("Invalid course duration. The course must be at least one day long!");
        }
    }

    private void validateMinimumCapacity(final int capacity) {
        if (capacity < this.minimumCapacity) {
            throw new CouldNotCreateCourseException("Minimum capacity is 10 students!");
        }
    }

    private void validateUniqueData(final String courseName) {
        if (this.courseRepository.existsByCourseName(courseName)) {
            throw new DuplicateDataException(new DataIntegrityViolationException("course.UK_9dll001xc2cip6hug6axoab0p"));
        }
    }
}
