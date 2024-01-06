package com.ing.hubs.exception.security;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class NoTokenFoundException extends ResponseException {
    public NoTokenFoundException(){
        this.setHttpStatus(HttpStatus.UNAUTHORIZED);
        this.setMessage("The token cannot be null or empty!");
    }
}