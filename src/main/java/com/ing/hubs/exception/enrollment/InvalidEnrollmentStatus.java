package com.ing.hubs.exception.enrollment;

import com.ing.hubs.exception.InvalidTimeException;
import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidEnrollmentStatus extends ResponseException {
    public InvalidEnrollmentStatus(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("Enrollment statuses: APPROVED, DENIED (for a teacher) and CANCELED (for a student) ");
    }
}
