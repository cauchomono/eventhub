package com.spring.eventhub.events.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.spring.eventhub.events.dto.EventCategory;

public record EventModel(
                UUID id,
                String title,
                String description,
                EventCategory category,
                Instant date,
                Instant createdAt,
                Integer capacity,
                BigDecimal price,
                Integer organizerId,
                boolean isPublished,
                String slug) {

        public EventModel withPublishEvent() {
        return new EventModel(this.id, this.title, this.description, this.category, this.date, this.createdAt, this.capacity, this.price, this.organizerId, true, this.slug);

        }
        
}
