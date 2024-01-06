package com.ing.hubs.exception.course;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class CouldNotCreateCourseException extends ResponseException {
    public CouldNotCreateCourseException(final String message) {
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
        super.setMessage(message);
    }
}
