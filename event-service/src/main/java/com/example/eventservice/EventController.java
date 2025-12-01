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

    // Modification d'un événement
    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Event event = eventRepository.findById(id).orElseThrow();

        event.setNom(eventDetails.getNom());
        event.setLieu(eventDetails.getLieu());
        event.setPrix(eventDetails.getPrix());
        event.setOrganisateur(eventDetails.getOrganisateur());
        event.setParticipants(eventDetails.getParticipants());
        // On ne touche pas au stock ici, c'est géré par les réservations

        return eventRepository.save(event);
    }

    // Suppression
    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
    }
}
