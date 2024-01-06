package com.ing.hubs.exception.enrollment;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class EnrollmentProcessingException extends ResponseException {
    public EnrollmentProcessingException(final String message) {
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
        super.setMessage(message);
    }
}
