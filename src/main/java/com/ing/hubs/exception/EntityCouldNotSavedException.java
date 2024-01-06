package com.ing.hubs.exception;

import org.springframework.http.HttpStatus;

public class EntityCouldNotSavedException extends ResponseException {
    public EntityCouldNotSavedException(final String message) {
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
        super.setMessage(String.format("%s could not be saved!"));
    }
}
