package com.example.eventservice;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    // Endpoint nécessaire pour le TP avec RestTemplate (décrémenter les places)
    @PutMapping("/{id}/decrement")
    public void decrementTickets(@PathVariable Long id, @RequestParam int count) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setNombreTicketsDisponibles(event.getNombreTicketsDisponibles() - count);
        eventRepository.save(event);
    }
}
