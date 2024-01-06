package com.ing.hubs.exception.user;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidIdentifierException extends ResponseException {
    public InvalidIdentifierException() {
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
        super.setMessage("Invalid identifier!");
    }
}
