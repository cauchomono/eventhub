package com.spring.eventhub.bookings.service;

import java.util.List;
import java.util.UUID;

import com.spring.eventhub.bookings.dto.CreateBookingsDto;
import com.spring.eventhub.bookings.dto.ResponseBookingsDto;

public interface BookingsService {
   ResponseBookingsDto bookingEventByEventId(CreateBookingsDto createBookingsDto, int userId, UUID eventId);
    List<ResponseBookingsDto> getAllBookingsByUserId(int userId);
    List<ResponseBookingsDto> getAllBookings();
    void deleteBooking(int id, int userId, boolean isAdmin);
}
