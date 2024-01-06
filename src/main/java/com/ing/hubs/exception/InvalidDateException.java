package com.ing.hubs.exception;

import org.springframework.http.HttpStatus;

public class InvalidDateException extends ResponseException {
    public InvalidDateException(String message){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage(message);
    }
}
