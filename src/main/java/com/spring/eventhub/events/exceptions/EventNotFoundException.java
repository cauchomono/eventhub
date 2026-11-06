package com.spring.eventhub.events.exceptions;

import java.util.UUID;

import com.spring.eventhub.common.NotFoundException;

public class EventNotFoundException  extends NotFoundException{

    public EventNotFoundException(UUID eventId) {
        super("Event with id " + eventId + " not found");
    }

}
