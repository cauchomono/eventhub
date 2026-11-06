package com.spring.eventhub.events.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spring.eventhub.common.validator.FutureDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatchEventDto(
        @Size(min = 3, max = 80, message = "The title must be between 3 and 80 characters") String title,
        String description,
        EventCategory category,
        @FutureDate Instant date,
        @Min(value = 1, message = "The event capacity must be at least 1") Integer capacity,
        @PositiveOrZero(message = "Price must be 0 or greater") BigDecimal price,

        @Pattern(regexp = "^[a-z0-9]++(?:-[a-z0-9]++)*+$", message = "The slug only accepts hyphens, lowercase letters and digits") @Size(max = 80) String slug) {

}
