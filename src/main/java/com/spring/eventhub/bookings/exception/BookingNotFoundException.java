package com.spring.eventhub.bookings.exception;

import com.spring.eventhub.common.NotFoundException;

public class BookingNotFoundException extends NotFoundException{
    
    public  BookingNotFoundException(int eventId){
        super("Booking with id "+eventId+" not Found");

    }

}
