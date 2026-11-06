package com.spring.eventhub.events.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.spring.eventhub.common.decorators.RequestTimeout;
import com.spring.eventhub.events.dto.CreateUpdateEventDto;
import com.spring.eventhub.events.dto.EventCategory;
import com.spring.eventhub.events.dto.EventResponseDto;
import com.spring.eventhub.events.dto.PatchEventDto;
import com.spring.eventhub.events.service.EventService;
import com.spring.eventhub.users.dto.UserResponseDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("events")
public class EventController {

    EventService eventService;

    EventController(final EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @RequestTimeout(1000)
     Callable<ResponseEntity<List<EventResponseDto>>> getAllEvents(
            @RequestParam(value = "page", defaultValue = "1", required = false) @Min(value = 1, message = "The min page  1") @Max(value=1000, message = "The page max is 1000") int page,
            @RequestParam(value = "limit", defaultValue = "5", required = false) @Min(value = 1, message = "The min limit is  1") @Max(value=50, message = "The page max is 50") int limit,
            @RequestParam(value = "category", required = false) EventCategory category,
            @RequestParam(value = "published",defaultValue = "true", required = false) boolean published) throws InterruptedException {
    
    return () ->{
        List<EventResponseDto> response = this.eventService.getAllEvents(page, limit, category,published);
        return ResponseEntity.ok(response);
    
    };

       
              
    
    }

    @GetMapping("by-ids")
    ResponseEntity<List<EventResponseDto>> getEventsById(@RequestParam("ids")  List<UUID> ids){
        List<EventResponseDto> response = this.eventService.getEventsById(ids);

        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    ResponseEntity<EventResponseDto> getEventById(@PathVariable ("id") UUID id ){
        EventResponseDto response = this.eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody CreateUpdateEventDto createEvent, @AuthenticationPrincipal UserResponseDto user){
        EventResponseDto response = this.eventService.createEvent(createEvent,user.id());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping ("{id}")
    ResponseEntity<EventResponseDto> editAllEvent(@Valid @RequestBody  CreateUpdateEventDto updateEvent, @PathVariable("id") UUID id){

        EventResponseDto response = this.eventService.editAllEvent(id, updateEvent);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("{id}")
    ResponseEntity<EventResponseDto> editFieldsEvent( @Valid @RequestBody  PatchEventDto updateEvent, @PathVariable("id") UUID id){

        EventResponseDto response = this.eventService.editFieldEvent(id, updateEvent);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("{id}/publish")
    ResponseEntity<Boolean> publishEvent( @PathVariable ("id") UUID id){

        boolean response = this.eventService.publishEvent(id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteEvent(@PathVariable  ("id") UUID id){
        this.eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }
}
