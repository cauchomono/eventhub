package com.spring.eventhub.bookings.dto;

import java.time.Instant;
import java.util.UUID;



public record ResponseBookingsDto(
        int id,
        UUID eventId,
        int userId,
        int seats,
        Instant createdAt) {
}