package com.ing.hubs.exception.course;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidScheduleException extends ResponseException {
    public InvalidScheduleException(String message) {
        super.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);
        super.setMessage(message);
    }
}
