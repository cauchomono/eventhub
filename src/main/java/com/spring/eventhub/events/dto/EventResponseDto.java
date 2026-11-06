package com.spring.eventhub.events.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponseDto(
        UUID id,
        String title,
        String description,
        EventCategory category,
        Instant date,
        Integer capacity,
        BigDecimal price,
        Integer organizerId,
        boolean isPublished,
        String slug) {

}
