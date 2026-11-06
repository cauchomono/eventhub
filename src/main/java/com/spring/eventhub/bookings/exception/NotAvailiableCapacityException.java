package com.spring.eventhub.bookings.exception;

public class NotAvailiableCapacityException extends RuntimeException{

    public NotAvailiableCapacityException(String message){
        super(message);
    }

}
