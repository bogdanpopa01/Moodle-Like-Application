package com.ing.hubs.exception.course;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class CourseCapacityReachedException extends ResponseException {
    public CourseCapacityReachedException() {
        super.setHttpStatus(HttpStatus.CONFLICT);
        super.setMessage("Course capacity has bean reached!");
    }
}
