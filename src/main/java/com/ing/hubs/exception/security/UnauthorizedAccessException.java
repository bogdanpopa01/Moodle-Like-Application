package com.ing.hubs.exception.security;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ResponseException {
    public UnauthorizedAccessException(){
        this.setHttpStatus(HttpStatus.FORBIDDEN);
        this.setMessage("This user is not authorized to access the requested resource!");
    }
}
