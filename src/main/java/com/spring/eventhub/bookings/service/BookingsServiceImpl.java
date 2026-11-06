package com.spring.eventhub.bookings.service;


import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import org.springframework.stereotype.Service;

import com.spring.eventhub.bookings.BookingsModel;
import com.spring.eventhub.bookings.dto.CreateBookingsDto;
import com.spring.eventhub.bookings.dto.ResponseBookingsDto;
import com.spring.eventhub.bookings.exception.BookingNotFoundException;
import com.spring.eventhub.bookings.exception.NotAvailiableCapacityException;
import com.spring.eventhub.common.NotFoundException;
import com.spring.eventhub.events.dto.EventResponseDto;
import com.spring.eventhub.events.service.EventService;


@Service
public class BookingsServiceImpl implements BookingsService {

    private static final Instant SAMPLE_BASE = Instant.parse("2026-10-02T12:00:00Z");

    private final Map<Integer, BookingsModel> bookings = new ConcurrentHashMap<>();

    // Siguiente id libre: arranca en 1 y tras cargar los datos de ejemplo queda en
    // 6.
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final EventService eventService;

    public BookingsServiceImpl(EventService eventService) {
        loadSampleData();
        this.eventService = eventService;
    }

    // Datos de ejemplo temporales: en la Fase 7 pasan a una migración de Flyway.
    // Solo reservan usuarios ATTENDEE (3 Bob, 4 Alice, 5 Carol), como exige el
    // contrato.
    private void loadSampleData() {
        add(1, 3, 2); // Bob en Rock en el Parque
        add(3, 3, 1); // Bob en Conferencia Java
        add(6, 4, 5); // Alice en Torneo de Ajedrez
        add(6, 5, 5); // Carol en el mismo evento
        add(4, 4, 3); // Alice en Hackathon Abierto
    }

    // Mismos UUID fijos que los eventos de ejemplo (…0001, …0002) para repetir
    // pruebas con curl.
    private void add(int eventNumber, int userId, int seats) {
        int id = nextId.getAndIncrement();
        UUID eventId = UUID.fromString(String.format("00000000-0000-0000-0000-%012d", eventNumber));
        bookings.put(id, new BookingsModel(id, eventId, userId, seats, SAMPLE_BASE.plusSeconds(id)));
    }

    @Override
    public synchronized ResponseBookingsDto bookingEventByEventId(CreateBookingsDto createBookingsDto, int userId,
            UUID eventId) {
        int id = nextId.getAndIncrement();

        EventResponseDto event = this.eventService.getEventById(eventId);
        int currentCapacity = event.capacity();
        if (!event.isPublished()) {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }
        if (!checkAvailiableSeats(currentCapacity, createBookingsDto.seats(),eventId)) {
            String message = "There is not capacity for asked seats: Capacity " + currentCapacity + " asked Seats: "
                    + createBookingsDto.seats();
            throw new NotAvailiableCapacityException(message);
        }

        BookingsModel newBooking = new BookingsModel(id, eventId, userId, createBookingsDto.seats(), Instant.now());
        this.bookings.put(id, newBooking);
        return fromBookingModelToBookingDto(newBooking);

    }

    private boolean checkAvailiableSeats(int capacity, int askedSeats, UUID eventId) {
        int currentFilledSeats = bookings.values().stream().filter(booking -> booking.eventId().equals(eventId))
                .mapToInt(map -> map.seats()).sum();

        int availiableSeats = capacity - currentFilledSeats;

        return askedSeats <= availiableSeats;
    }

    private ResponseBookingsDto fromBookingModelToBookingDto(BookingsModel model) {
        return new ResponseBookingsDto(model.id(), model.eventId(), model.userId(), model.seats(), model.createdAt());
    }

    @Override
    public List<ResponseBookingsDto> getAllBookingsByUserId(int userId) {
        return bookings.entrySet().stream().filter(entry -> entry.getValue().userId().equals(userId)).map(value -> {
            BookingsModel booking = value.getValue();
            return new ResponseBookingsDto(booking.id(), booking.eventId(), booking.userId(), booking.seats(),
                    booking.createdAt());
        }).toList();
    }

    @Override
    public List<ResponseBookingsDto> getAllBookings() {
        return bookings.entrySet().stream().map(entry -> {
            BookingsModel booking = entry.getValue();
            return new ResponseBookingsDto(
                    booking.id(), booking.eventId(), booking.userId(), booking.seats(), booking.createdAt());
        }).toList();

    }

    @Override
    public synchronized void deleteBooking(int id, int userId, boolean isAdmin) {
        BookingsModel model = this.bookings.get(id);
       

        if(model == null){
            throw new BookingNotFoundException(id);
        }
         int ownerBookingId = model.userId();
        if (userId == ownerBookingId || isAdmin) {

            bookings.remove(id);
        } else {
            throw new BookingNotFoundException(id);
        }
    }

}
