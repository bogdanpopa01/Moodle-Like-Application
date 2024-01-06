package com.ing.hubs.exception;

import org.springframework.http.HttpStatus;

public class InvalidTimeException extends ResponseException {
    public InvalidTimeException(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("Time format: HH:mm!");
    }
}
