package com.ing.hubs.exception.user;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidGenderException extends ResponseException {
    public InvalidGenderException(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("Gender can be either MALE or FEMALE");
    }
}
