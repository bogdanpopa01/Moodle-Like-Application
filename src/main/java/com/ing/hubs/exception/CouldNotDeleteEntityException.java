package com.ing.hubs.exception;

import org.springframework.http.HttpStatus;

public class CouldNotDeleteEntityException extends ResponseException {
    public CouldNotDeleteEntityException(final String message){
        this.setHttpStatus(HttpStatus.FORBIDDEN);
        this.setMessage(message);
    }
}
