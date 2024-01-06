package com.ing.hubs.exception.user;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidEmailException extends ResponseException {
    public InvalidEmailException() {
        super.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);
        super.setMessage("Enter a valid email!");
    }
}
