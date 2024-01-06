package com.ing.hubs.service.enrollment;

import com.ing.hubs.dto.enrollment.UpdateEnrollmentStatusDto;
import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.enrollment.GradeEnrollmentDto;
import com.ing.hubs.exception.course.InvalidScheduleException;
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.EnrollmentRepository;
import com.ing.hubs.service.CourseService;
import com.ing.hubs.service.ScheduleService;
import com.ing.hubs.service.SecurityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@AllArgsConstructor
public class EnrollmentProcessingService {
    private SecurityService securityService;
    private EnrollmentService enrollmentService;
    private CourseService courseService;
    private ScheduleService scheduleService;
    private ModelMapper modelMapper;
    private EnrollmentRepository enrollmentRepository;
    private EnrollmentValidationService validationService;

    public EnrollmentResponseDto updateEnrollmentStatus(final UUID enrollmentId,
                                                        final UpdateEnrollmentStatusDto dto,
                                                        final String jwtToken){
        final Role role = this.securityService.extractRoleFromToken(jwtToken);
        final UUID teacherId = this.securityService.extractUserIdFromToken(jwtToken);
        final Enrollment enrollment = this.enrollmentService.findEnrollmentById(enrollmentId);
        final Course course = this.courseService.findCourseById(enrollment.getCourse().getId());

        switch (role){
            case TEACHER -> {
                this.validationService.validateTeacherPermissions(enrollment, teacherId);
                this.validationService.validateCapacity(course);
                this.validationService.validateIfEnrollmentStatusTransitionIsValid(enrollment.getStatus(), dto.getStatus());

                this.changeEnrollmentStatus(enrollment, dto.getStatus());

                if (enrollment.getStatus().equals(EnrollmentStatus.APPROVED)) {
                    this.handleEnrollmentIfApproved(enrollment);
                }
            }
            case STUDENT -> {
                this.validationService.validateEnrollmentCancellation(enrollment.getStatus(), dto.getStatus());
                this.changeEnrollmentStatus(enrollment, dto.getStatus());
            }
        }

        this.courseService.saveCourse(course);
        return this.modelMapper.map(enrollment, EnrollmentResponseDto.class);
    }

    private void handleEnrollmentIfApproved(final Enrollment enrollment) {
        final Course course = enrollment.getCourse();
        course.setCapacity(course.getCapacity() - 1);

        final User student = enrollment.getUser();
        final List<Course> coursesAwaitingApproval = this.getCoursesAwaitingApproval(student);

        coursesAwaitingApproval.forEach(c -> {
            try {
                this.scheduleService.validateScheduleOverlap(c, student);
            } catch (InvalidScheduleException ex){
                c.getEnrollments()
                        .stream()
                        .filter(e -> e.getUser().equals(student))
                        .forEach(e -> this.changeEnrollmentStatus(e, EnrollmentStatus.CANCELED));}
        });
    }

    private List<Course> getCoursesAwaitingApproval(final User student) {
        return this.enrollmentService.findAllEnrollmentsByUser(student.getId())
                .stream()
                .filter(e -> e.getStatus().equals(EnrollmentStatus.PENDING))
                .map(Enrollment::getCourse)
                .toList();
    }

    public EnrollmentResponseDto gradeEnrollment(final UUID enrollmentId,
                                                 final GradeEnrollmentDto dto,
                                                 final String jwtToken) {
        final UUID teacherId = this.securityService.extractUserIdFromToken(jwtToken);
        var enrollment = this.enrollmentService.findEnrollmentById(enrollmentId);

        this.validationService.validateTeacherPermissions(enrollment, teacherId);
        this.validationService.validateExistingEnrollment(enrollment);
        this.validationService.validateGrade(dto.getGrade());

        enrollment.setGrade(dto.getGrade());
        log.info(String.format("For enrollment with id \"%s\", grade has been set to \"%s\"", enrollment.getId().toString(), enrollment.getGrade()));
        return this.modelMapper.map(this.enrollmentRepository.save(enrollment), EnrollmentResponseDto.class);
    }

    public void changeEnrollmentStatus(final Enrollment enrollment,
                                       final EnrollmentStatus status){
        enrollment.setStatus(status);
        log.info(String.format("For enrollment with id \"%s\", status has been set to \"%s\"", enrollment.getId().toString(), status.toString()));
        this.enrollmentRepository.save(enrollment);
    }
}
