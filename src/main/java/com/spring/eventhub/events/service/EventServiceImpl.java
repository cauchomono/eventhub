package com.spring.eventhub.events.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.function.Function;

import org.springframework.stereotype.Service;


import com.spring.eventhub.events.dto.CreateUpdateEventDto;
import com.spring.eventhub.events.dto.EventCategory;
import com.spring.eventhub.events.dto.EventResponseDto;
import com.spring.eventhub.events.dto.PatchEventDto;
import com.spring.eventhub.events.exceptions.EventNotFoundException;
import com.spring.eventhub.events.model.EventModel;


@Service
public class EventServiceImpl implements EventService {

        Map<UUID, EventModel> mockData = new ConcurrentHashMap<>();
        
        private static final Instant SAMPLE_BASE = Instant.parse("2026-10-01T12:00:00Z");

        public EventServiceImpl() {
                loadSampleData();
        }

        // Datos de ejemplo temporales: en la Fase 7 pasan a una migración de Flyway.
        private void loadSampleData() {
                // Publicados (8: más que el limit por defecto de 5, para probar page/limit)
                add(1, "Rock en el Parque", "Festival de rock al aire libre", EventCategory.MUSIC,
                                Instant.parse("2027-03-15T20:00:00Z"), 500, "45.50", true,
                                "rock-en-el-parque-2027");
                add(2, "Jazz Nocturno", null, EventCategory.MUSIC,
                                Instant.parse("2027-04-02T21:30:00Z"), 120, "30.00", true, null);
                add(3, "Conferencia Java", "Charlas sobre Java y Spring", EventCategory.TECH,
                                Instant.parse("2027-05-10T09:00:00Z"), 300, "99.99", true,
                                "conferencia-java-2027");
                add(4, "Hackathon Abierto", "24 horas construyendo proyectos", EventCategory.TECH,
                                Instant.parse("2027-06-05T08:00:00Z"), 20, "0", true,
                                "hackathon-abierto");
                add(5, "Maratón de la Ciudad", "10K y 21K", EventCategory.SPORTS,
                                Instant.parse("2027-07-20T06:30:00Z"), 2000, "25.00", true,
                                "maraton-ciudad-2027");
                add(6, "Torneo de Ajedrez", null, EventCategory.SPORTS,
                                Instant.parse("2027-08-14T10:00:00Z"), 64, "10.00", true, null);
                add(7, "Expo de Arte Moderno", "Obras de artistas locales", EventCategory.ART,
                                Instant.parse("2027-09-01T11:00:00Z"), 150, "15.75", true,
                                "expo-arte-moderno");
                add(8, "Noche de Museos", "Entrada libre a varios museos", EventCategory.ART,
                                Instant.parse("2027-10-09T18:00:00Z"), 1000, "0", true, null);

                // Sin publicar (para probar el filtro published)
                add(9, "Festival Electrónico", "Borrador, sin publicar", EventCategory.MUSIC,
                                Instant.parse("2027-11-12T22:00:00Z"), 800, "60.00", false,
                                "festival-electronico-2027");
                add(10, "Taller de Angular", null, EventCategory.TECH,
                                Instant.parse("2027-12-03T15:00:00Z"), 40, "20.00", false, null);
        }

        // UUID fijos y legibles (…0001, …0002) para poder repetir pruebas con curl.
        private void add(int n, String title, String description, EventCategory category, Instant date,
                        int capacity, String price, boolean isPublished, String slug) {
                UUID id = UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
                // createdAt = base + n/2 segundos: los eventos (2,3), (4,5), (6,7) y (8,9)
                // comparten instante a propósito,
                // para probar el desempate por id en el listado.
                Instant createdAt = SAMPLE_BASE.plusSeconds(n / 2);
                mockData.put(id, new EventModel(id, title, description, category, date, createdAt, capacity,
                                new BigDecimal(price), 0, isPublished, slug));
        }

        public List<EventResponseDto> getAllEvents(Integer page, Integer limit, EventCategory category, boolean published) {
                int pageZeroIndexed = page - 1;
                Stream<EventModel> eventsStream = this.mockData.values().stream().filter(event -> event.isPublished() == published);

                if (category != null) {
                        eventsStream = eventsStream.filter(event -> event.category().equals(category));
                }

                eventsStream = eventsStream
                                .sorted(Comparator.comparing(EventModel::createdAt).reversed()
                                                .thenComparing(EventModel::id))
                                .skip((long) pageZeroIndexed * limit).limit(limit);

                return eventsStream
                                .map(event -> new EventResponseDto(event.id(), event.title(), event.description(),
                                                event.category(),
                                                event.date(), event.capacity(), event.price(), event.organizerId(),
                                                event.isPublished(),
                                                event.slug()))
                                .toList();
        }

        public List<EventResponseDto> getEventsById(List<UUID> eventIds) {
                return eventIds.stream().map(event -> this.mockData.get(event)).filter(Objects::nonNull).map(this::mapFromEventToEventResponse).toList();

        }

        public EventResponseDto getEventById(UUID eventId) {
                EventModel event = this.mockData.get(eventId);
                if (event == null) {
                        throw new EventNotFoundException(eventId);
                }
                return mapFromEventToEventResponse(event);
        }

        public EventResponseDto createEvent(CreateUpdateEventDto createEventDto, int userId) {

                UUID id = UUID.randomUUID();
                EventModel event = new EventModel(
                                id,
                                createEventDto.title(),
                                createEventDto.description(),
                                createEventDto.category(),
                                createEventDto.date(),
                                Instant.now(),
                                createEventDto.capacity(),
                                createEventDto.price(),
                                userId,
                                false,
                                createEventDto.slug());

                this.mockData.put(id, event);

                return mapFromEventToEventResponse(event);

        }

        public EventResponseDto editAllEvent(UUID eventId, CreateUpdateEventDto updateEventDto) {
                EventModel eventToreplace = getEventModel(eventId, event -> mapUpdateFromCreateUpdateDtoToEventModel(event,updateEventDto));
                 return mapFromEventToEventResponse(eventToreplace);

        }

        public EventResponseDto editFieldEvent(UUID eventId, PatchEventDto updateEventDto) {
                EventModel eventToReplace = getEventModel(eventId, event -> mapPatchFromPatchEventDtoToEventModel(event,updateEventDto));
                 return mapFromEventToEventResponse(eventToReplace);
        }

        private EventModel getEventModel(UUID eventId, Function<EventModel,EventModel> function){
                 EventModel model =  this.mockData.computeIfPresent(eventId, (id, event) -> function.apply(event));
                if (model == null){
                        throw new EventNotFoundException(eventId);

                }
                return model;
        }

        public boolean publishEvent(UUID eventId) {
                EventModel updatedEvent = this.mockData.computeIfPresent(eventId, (id, currentEvent) -> currentEvent.withPublishEvent());
                        if (updatedEvent == null){
                                throw new EventNotFoundException(eventId);
                        }

                return updatedEvent.isPublished();
        }       

        public void deleteEventById(UUID eventId) {
               EventModel  event =  this.mockData.remove(eventId);
                if(event == null){
                        throw new EventNotFoundException(eventId);
                }
        }

        private EventResponseDto mapFromEventToEventResponse(EventModel event) {
                return new EventResponseDto(event.id(), event.title(), event.description(), event.category(),
                                event.date(), event.capacity(), event.price(), event.organizerId(), event.isPublished(),
                                event.slug());

        }

        private EventModel mapUpdateFromCreateUpdateDtoToEventModel(EventModel event, CreateUpdateEventDto model){

                return new EventModel(
                        event.id(),
                        model.title(),
                        model.description(),
                        model.category(),
                        model.date(),
                        event.createdAt(),
                        model.capacity(),
                        model.price(),
                        event.organizerId(),
                        event.isPublished(),
                        model.slug()
                );
                

        }


        private EventModel mapPatchFromPatchEventDtoToEventModel(EventModel event, PatchEventDto model){

                return new EventModel(
                        event.id(),
                        model.title() != null ? model.title():event.title(),
                        model.description() != null ? model.description() : event.description(),
                        model.category() != null ? model.category() : event.category(),
                        model.date() != null ? model.date() : event.date(),
                        event.createdAt(),
                        model.capacity() != null ? model.capacity() : event.capacity(),
                        model.price() != null ? model.price(): event.price(),
                        event.organizerId(),
                        event.isPublished(),
                        model.slug() != null ? model.slug() : event.slug()
                );
                

        }


}
