package com.ing.hubs.model.entity.enrollment;

import java.util.Arrays;

public enum EnrollmentStatus {
    PENDING, APPROVED, DENIED, ACTIVE, COMPLETED, CANCELED;

    public boolean isValidTransition(EnrollmentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus.equals(APPROVED) || newStatus.equals(DENIED);
            case DENIED -> newStatus.equals(APPROVED);
            default -> false;
        };
    }
}
