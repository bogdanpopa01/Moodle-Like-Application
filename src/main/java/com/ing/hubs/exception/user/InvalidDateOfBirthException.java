package com.ing.hubs.exception.user;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidDateOfBirthException extends ResponseException {
    public InvalidDateOfBirthException() {
        super.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);
        super.setMessage("Enter a valid date of birth");
    }
}
