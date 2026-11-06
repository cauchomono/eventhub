package com.spring.eventhub.bookings;

import java.time.Instant;
import java.util.UUID;

public record BookingsModel(
        int id,
        UUID eventId,
        Integer userId,
        int seats, 
        Instant createdAt) {

}
