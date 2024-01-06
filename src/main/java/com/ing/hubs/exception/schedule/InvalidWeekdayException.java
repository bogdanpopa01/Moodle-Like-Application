package com.ing.hubs.exception.schedule;

import com.ing.hubs.exception.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidWeekdayException extends ResponseException {
    public InvalidWeekdayException(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("Valid weekdays: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY ");
    }
}
