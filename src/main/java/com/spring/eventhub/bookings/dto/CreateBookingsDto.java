package com.spring.eventhub.bookings.dto;



import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingsDto(
        @Min(1) @Max(5) @NotNull(message = "Seats must have a value") Integer seats
    ) {

}
