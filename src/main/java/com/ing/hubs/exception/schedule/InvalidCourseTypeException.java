package com.ing.hubs.exception.schedule;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidCourseTypeException extends ResponseException {
    public InvalidCourseTypeException(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("Course type can be one of these: COURSE, SEMINAR, LAB!");
    }
}
