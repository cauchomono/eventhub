package com.spring.eventhub.events.service;

import java.util.List;
import java.util.UUID;

import com.spring.eventhub.events.dto.CreateUpdateEventDto;
import com.spring.eventhub.events.dto.EventCategory;
import com.spring.eventhub.events.dto.EventResponseDto;
import com.spring.eventhub.events.dto.PatchEventDto;

public interface EventService {

    List<EventResponseDto> getAllEvents(Integer page, Integer limit, EventCategory category, boolean published);

    List<EventResponseDto> getEventsById(List<UUID> eventIds);

    EventResponseDto getEventById(UUID eventId);

    EventResponseDto createEvent(CreateUpdateEventDto createEventDto, int userId);

    EventResponseDto editAllEvent(UUID eventId, CreateUpdateEventDto updateEventDto);

    EventResponseDto editFieldEvent(UUID eventId, PatchEventDto patchEventDto);

    boolean publishEvent(UUID eventId);

    void deleteEventById(UUID eventId);

}
