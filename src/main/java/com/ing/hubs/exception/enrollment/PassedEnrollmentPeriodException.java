package com.ing.hubs.exception.enrollment;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class PassedEnrollmentPeriodException extends ResponseException {
    public PassedEnrollmentPeriodException() {
        super.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);
        super.setMessage("Enrollment period has passed!");
    }
}
