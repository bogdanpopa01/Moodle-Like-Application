package com.ing.hubs.model.entity.enrollment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentStatusTest {

    @Test
    void shouldReturnTrueWhenTransitionIsValid(){
        final EnrollmentStatus enrollmentStatusPending = EnrollmentStatus.PENDING;
        final EnrollmentStatus enrollmentStatusDenied = EnrollmentStatus.DENIED;

        assertTrue(enrollmentStatusPending.isValidTransition(EnrollmentStatus.APPROVED));
        assertTrue(enrollmentStatusPending.isValidTransition(EnrollmentStatus.DENIED));
        assertTrue(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.APPROVED));
        assertTrue(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.APPROVED));
    }
    @Test
    void shouldReturnFalseWhenTransitionIsInvalid(){
        final EnrollmentStatus enrollmentStatusPending = EnrollmentStatus.PENDING;
        final EnrollmentStatus enrollmentStatusApproved = EnrollmentStatus.APPROVED;
        final EnrollmentStatus enrollmentStatusDenied = EnrollmentStatus.DENIED;
        final EnrollmentStatus enrollmentStatusActive = EnrollmentStatus.ACTIVE;
        final EnrollmentStatus enrollmentStatusCompleted = EnrollmentStatus.COMPLETED;
        final EnrollmentStatus enrollmentStatusCanceled = EnrollmentStatus.CANCELED;

        assertFalse(enrollmentStatusPending.isValidTransition(EnrollmentStatus.ACTIVE));
        assertFalse(enrollmentStatusPending.isValidTransition(EnrollmentStatus.COMPLETED));
        assertFalse(enrollmentStatusPending.isValidTransition(EnrollmentStatus.CANCELED));

        assertFalse(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.PENDING));
        assertFalse(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.ACTIVE));
        assertFalse(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.COMPLETED));
        assertFalse(enrollmentStatusDenied.isValidTransition(EnrollmentStatus.CANCELED));

        for (EnrollmentStatus newStatus : EnrollmentStatus.values()){
            assertFalse(enrollmentStatusApproved.isValidTransition(newStatus));
            assertFalse(enrollmentStatusActive.isValidTransition(newStatus));
            assertFalse(enrollmentStatusCompleted.isValidTransition(newStatus));
            assertFalse(enrollmentStatusCanceled.isValidTransition(newStatus));
        }
    }
}