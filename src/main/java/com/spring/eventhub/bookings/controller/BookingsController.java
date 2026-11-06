package com.spring.eventhub.bookings.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.spring.eventhub.bookings.dto.CreateBookingsDto;
import com.spring.eventhub.bookings.dto.ResponseBookingsDto;
import com.spring.eventhub.bookings.service.BookingsService;
import com.spring.eventhub.users.dto.UserResponseDto;
import com.spring.eventhub.users.enums.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping()
public class BookingsController {

    private final BookingsService bookingsService;

    BookingsController(BookingsService bookingsService) {
        this.bookingsService = bookingsService;
    }

    @GetMapping("/bookings/me")
    public ResponseEntity<List<ResponseBookingsDto>> getAllBookingsByUser(
            @AuthenticationPrincipal UserResponseDto user) {
        return ResponseEntity.ok(this.bookingsService.getAllBookingsByUserId(user.id()));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<ResponseBookingsDto>> getAllBookings() {
        return ResponseEntity.ok(this.bookingsService.getAllBookings());
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@AuthenticationPrincipal UserResponseDto user,
            @PathVariable("id") @NotNull int id) {
        boolean isAdmin = user.roles().contains(Role.ROLE_ADMIN);
        this.bookingsService.deleteBooking(id, user.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<ResponseBookingsDto> createBooking(@Valid @RequestBody CreateBookingsDto createBookingsDto,
            @PathVariable("eventId") UUID eventId, @AuthenticationPrincipal UserResponseDto user) {
        ResponseBookingsDto response = this.bookingsService.bookingEventByEventId(createBookingsDto, user.id(),
                eventId);
        URI uri = UriComponentsBuilder.fromPath("/bookings/me").build().toUri();
        return ResponseEntity.created(uri).body(response);
    }

}
