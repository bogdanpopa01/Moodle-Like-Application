package com.ing.hubs.exception;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ResponseException {
    public EntityNotFoundException(String entityName) {
        super.setHttpStatus(HttpStatus.NOT_FOUND);
        super.setMessage(String.format("%s not found!", entityName));
    }
}
