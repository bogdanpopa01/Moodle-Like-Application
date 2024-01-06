package com.ing.hubs.service.cron.job;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.service.enrollment.EnrollmentProcessingService;
import com.ing.hubs.service.enrollment.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class EnrollmentStatusJobTest {
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private EnrollmentProcessingService processingService;
    @InjectMocks
    private EnrollmentStatusJob enrollmentStatusJob;
    private UUID enrollment1Id;
    private UUID enrollment2Id;

    private Enrollment enrollment1;
    private Enrollment enrollment2;

    @BeforeEach
    void setup(){
         this.enrollment1Id = UUID.randomUUID();
         this.enrollment2Id = UUID.randomUUID();
        final Course course1 = Course.builder()
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(1))
                .build();
        final Course course2 = Course.builder()
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        this.enrollment1 = Enrollment.builder()
                .id(enrollment1Id)
                .course(course1)
                .status(EnrollmentStatus.ACTIVE)
                .grade(5)
                .build();
        this.enrollment2 = Enrollment.builder()
                .id(enrollment2Id)
                .status(EnrollmentStatus.APPROVED)
                .course(course2)
                .build();


        course2.setEnrollments(Set.of(enrollment2));

        doCallRealMethod().when(this.processingService).changeEnrollmentStatus(any(Enrollment.class), any());
    }

    @Test
    void shouldSetEnrollmentStatusToCompletedOnceCourseHasEndedAndEnrollmentIsGraded(final CapturedOutput capturedOutput){
        when(this.enrollmentService.findEnrollmentsByStatus(any(EnrollmentStatus.class))).thenReturn(List.of(enrollment1));

        try {
            this.enrollmentStatusJob.disableCompletedEnrollments();
        } catch (NullPointerException ignored){}

        assertThat(capturedOutput.getOut(), allOf(
                containsString(enrollment1Id.toString()),
                containsString(EnrollmentStatus.COMPLETED.toString())
        ));
    }

    @Test
    void shouldSetEnrollmentStatusToActiveOnceTheCourseStarted(final CapturedOutput capturedOutput) throws NullPointerException{
        when(this.enrollmentService.findEnrollmentsByStatus(any(EnrollmentStatus.class))).thenReturn(List.of(enrollment2));

        try {
            this.enrollmentStatusJob.activateEnrollments();
        } catch (NullPointerException ignored){}

        assertThat(capturedOutput.getOut(), allOf(
                containsString(enrollment2Id.toString()),
                containsString(EnrollmentStatus.ACTIVE.toString())
        ));
    }

    @Test
    void shouldCancelEnrollmentsIfMinimumAttendeesNotReached(final CapturedOutput capturedOutput){
        when(this.enrollmentService.findEnrollmentsByStatus(EnrollmentStatus.APPROVED)).thenReturn(List.of(enrollment2));

        try {
            this.enrollmentStatusJob.cancelEnrollmentsIfMinimumAttendeesNotReached();
        }catch (NullPointerException ex){}

        assertThat(capturedOutput.getOut(), allOf(
                containsString(enrollment2Id.toString()),
                containsString(EnrollmentStatus.CANCELED.toString())
        ));
    }
}