package com.ing.hubs.service.enrollment;

import com.ing.hubs.dto.course.CourseResponseDto;
import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.schedule.ScheduleResponseDto;
import com.ing.hubs.dto.schedule.StudentsScheduleResponseDto;
import com.ing.hubs.dto.user.UserResponseDto;
import com.ing.hubs.exception.EntityCouldNotSavedException;
import com.ing.hubs.exception.user.InvalidIdentifierException;
import com.ing.hubs.exception.security.UnauthorizedAccessException;
import com.ing.hubs.exception.EntityNotFoundException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.EnrollmentRepository;
import com.ing.hubs.service.CourseService;
import com.ing.hubs.service.ScheduleService;
import com.ing.hubs.service.SecurityService;
import com.ing.hubs.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class EnrollmentService {
    private UserService userService;
    private CourseService courseService;
    private EnrollmentRepository enrollmentRepository;
    private ModelMapper modelMapper;
    private SecurityService securityService;
    private ScheduleService scheduleService;
    private EnrollmentValidationService validationService;


    public EnrollmentResponseDto createEnrollment(final UUID courseId,
                                                  final String jwtToken) {
        final User student = this.userService.findUserById(this.securityService.extractUserIdFromToken(jwtToken));

        if (student.getRole() != Role.STUDENT) {
            throw new UnauthorizedAccessException();
        }

        final Course course = this.courseService.findCourseById(courseId);
//      For Postman collection to work comment this
        this.validationService.validateEnrollmentPeriod(course);
//
        this.validationService.validateCapacity(course);
        this.validationService.validateIfAlreadyEnrolled(course, student);
        this.scheduleService.validateScheduleOverlap(course, student);

        final Enrollment enrollment = Enrollment.builder()
                .user(student)
                .course(course)
                .build();

        student.addEnrollment(enrollment);

        this.userService.saveUser(student);
        this.courseService.saveCourse(course);

        final Enrollment savedEnrollment = this.enrollmentRepository.findEnrollmentByUserAndCourse(student, course)
                .orElseThrow(() -> new EntityCouldNotSavedException("Enrollment"));
        log.info(String.format("Enrollment with id \"%s\" has been created", savedEnrollment.getId()));

        final EnrollmentResponseDto response = this.modelMapper.map(savedEnrollment, EnrollmentResponseDto.class);
        response.setCourse(this.modelMapper.map(course, CourseResponseDto.class));
        response.setUser(this.modelMapper.map(student, UserResponseDto.class));

        return response;
    }

    public List<EnrollmentResponseDto> findAll(final EnrollmentStatus enrollmentStatusFilter,
                                               final String jwtToken) {
        final UUID userId = securityService.extractUserIdFromToken(jwtToken);
        final User user = userService.findUserById(userId);

        final Stream<Enrollment> enrollments = this.findAllEnrollments()
                .stream()
                .filter(enr -> enrollmentStatusFilter == null || enr.getStatus().equals(enrollmentStatusFilter));

        Stream<Enrollment> filteredEnrollments = Stream.empty();

        switch (user.getRole()){
            case TEACHER -> filteredEnrollments = enrollments
                    .filter(enr -> enr.getCourse().getUser().getId().equals(user.getId()));
            case STUDENT -> filteredEnrollments = enrollments
                    .filter(enrollment -> enrollment.getUser().getId().equals(userId));
        }

        return filteredEnrollments
                .map(enr -> this.modelMapper.map(enr, EnrollmentResponseDto.class))
                .toList();
    }

    private List<Enrollment> findAllEnrollments() {
        return this.enrollmentRepository.findAll()
                .stream()
                .toList();
    }

    public Map<String, Integer> viewGrades(final String jwtToken) {
        if(!this.securityService.extractRoleFromToken(jwtToken).equals(Role.STUDENT)){
            throw new UnauthorizedAccessException();
        }

        final Map<String, Integer> grades = new HashMap<>();
        this.findAllEnrollmentsByUser(jwtToken).stream()
                .filter(enrollment ->
                        enrollment.getStatus().equals(EnrollmentStatus.APPROVED) ||
                        enrollment.getStatus().equals(EnrollmentStatus.ACTIVE) ||
                        enrollment.getStatus().equals(EnrollmentStatus.COMPLETED))
                .forEach(enrollment ->
                        grades.put(enrollment.getCourse().getCourseName(), enrollment.getGrade()));
        return grades;
    }

    public List<EnrollmentResponseDto> findAllEnrollmentsByCourse(final UUID courseId,
                                                                 final String jwtToken) {
        final UUID userId = securityService.extractUserIdFromToken(jwtToken);
        final Role role = this.securityService.extractRoleFromToken(jwtToken);

        if(!role.equals(Role.TEACHER)){
            throw new UnauthorizedAccessException();
        }

        if(!courseService.findCourseById(courseId).getUser().getId().equals(userId)){
            throw new UnauthorizedAccessException();
        }

        return this.findAllEnrollments().stream()
                .filter(enrollment -> enrollment.getCourse().getId().equals(courseId))
                .map(enrollment ->
                        this.modelMapper.map(enrollment, EnrollmentResponseDto.class))
                .toList();
    }

    List<Enrollment> findAllEnrollmentsByUser(final Object identifier) {
        if (identifier instanceof UUID){
            return this.findAllEnrollments().stream().filter(enrollment -> enrollment.getUser().getId().equals(identifier))
                    .collect(Collectors.toList());
        }
        if (identifier instanceof String) {
            final UUID id = this.securityService.extractUserIdFromToken(identifier.toString());
            return this.findAllEnrollments().stream().filter(enrollment -> enrollment.getUser().getId().equals(id))
                    .collect(Collectors.toList());
        }
        throw new InvalidIdentifierException();
    }

    public Enrollment findEnrollmentById(final UUID enrollmentId) {
        return this.enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new EntityNotFoundException("Enrollment"));
    }

    public List<StudentsScheduleResponseDto> findStudentsActiveSchedules(final String jwtToken) {
        return this.findAllEnrollmentsByUser(jwtToken).stream()
                .filter(enrollment -> enrollment.getStatus().equals(EnrollmentStatus.ACTIVE))
                .map(enrollment -> {
                    final List<ScheduleResponseDto> coursesSchedules = scheduleService.getSchedulesFromEnrollment(enrollment)
                            .stream()
                            .map(schedule -> modelMapper.map(schedule, ScheduleResponseDto.class))
                            .toList();
                    final String courseName = enrollment.getCourse().getCourseName();
                    return new StudentsScheduleResponseDto(courseName, coursesSchedules);
                })
                .collect(Collectors.toList());
    }

    public List<Enrollment> findEnrollmentsByStatus(final EnrollmentStatus enrollmentStatus) {
        final List<Enrollment> enrollments = this.enrollmentRepository.findAll();
        if (enrollmentStatus != null) {
            return enrollments.stream()
                    .filter(enrollment -> enrollment.getStatus().equals(enrollmentStatus))
                    .toList();
        }

        return enrollments;
    }
}
