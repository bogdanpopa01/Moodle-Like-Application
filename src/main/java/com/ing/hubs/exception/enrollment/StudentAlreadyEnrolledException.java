package com.ing.hubs.exception.enrollment;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class StudentAlreadyEnrolledException extends ResponseException {
    public StudentAlreadyEnrolledException() {
        super.setHttpStatus(HttpStatus.CONFLICT);
        super.setMessage("This student has already enrolled in this course!");
    }
}
