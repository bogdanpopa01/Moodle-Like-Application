package com.ing.hubs.service.cron.job;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.service.enrollment.EnrollmentProcessingService;
import com.ing.hubs.service.enrollment.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;


@EnableScheduling
@Component
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class EnrollmentStatusJob {
    private final EnrollmentService enrollmentService;
    private final EnrollmentProcessingService enrollmentProcessingService;

    @Autowired
    public EnrollmentStatusJob(final EnrollmentService enrollmentService,
                               final EnrollmentProcessingService enrollmentProcessingService) {
        this.enrollmentService = enrollmentService;
        this.enrollmentProcessingService = enrollmentProcessingService;
    }

//    @Scheduled(cron = "0 0 * * *")
    @Scheduled(fixedRate = 10000L)
    void disableCompletedEnrollments(){
        this.enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.ACTIVE).forEach(enrollment -> {
            if (enrollment.getGrade() != null &&
                enrollment.getCourse().getEndDate().isBefore(LocalDate.now())){
                this.enrollmentProcessingService.changeEnrollmentStatus(enrollment, EnrollmentStatus.COMPLETED);
            }
        });
    }

//    @Scheduled(cron = "0 0 * * *")
    @Scheduled(fixedRate = 10000L)
    void activateEnrollments(){
        final List<Enrollment> enrollments = this.enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.APPROVED);
        enrollments.forEach(enrollment -> {
            if (enrollment.getCourse().getStartDate().isEqual(LocalDate.now()) ||
                enrollment.getCourse().getStartDate().isBefore(LocalDate.now())){
                this.enrollmentProcessingService.changeEnrollmentStatus(enrollment, EnrollmentStatus.ACTIVE);
            }
        });
    }
//    Commented for presentation purposes
//    @Scheduled(cron = "0 0 * * *")
    void cancelEnrollmentsIfMinimumAttendeesNotReached(){
        final List<Enrollment> approvedAndPendingEnrollments = Stream.concat(
                this.enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.APPROVED).stream(),
                this.enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.PENDING).stream())
                .toList();

        approvedAndPendingEnrollments.forEach(enrollment -> {
            final Course course = enrollment.getCourse();
            final int noOfEnrolledStudents = course.getEnrollments().size();

            if (noOfEnrolledStudents < 5) {
                this.enrollmentProcessingService.changeEnrollmentStatus(enrollment, EnrollmentStatus.CANCELED);
            }
        });
    }
}
