package com.example.reservationservice;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestTemplate restTemplate; // Celui qu'on a configuré avec @LoadBalanced

    // On utilise le NOM du service enregistré dans Eureka
    private final String EVENT_SERVICE_URL = "http://event-service/events/";

    @PostMapping
    public Reservation reserver(@RequestBody Reservation reservation) {

        // 1. Appel Synchrone : On demande à event-service les infos de l'événement
        // On concatène l'URL : http://event-service/events/1
        EventDTO event = restTemplate.getForObject(EVENT_SERVICE_URL + reservation.getEventId(), EventDTO.class);

        // 2. Vérifications
        if (event == null) {
            throw new RuntimeException("Événement introuvable !");
        }

        if (event.getNombreTicketsDisponibles() < reservation.getNombrePlaces()) {
            throw new RuntimeException("Désolé, plus assez de places !");
        }

        // 3. Créer la réservation
        reservation.setStatus("PENDING");
        Reservation savedReservation = reservationRepository.save(reservation);

        // 4. (Bonus) Mettre à jour le stock dans l'autre service via un appel PUT
        // On appelle l'endpoint qu'on a créé tout à l'heure : /events/{id}/decrement?count=X
        restTemplate.put(EVENT_SERVICE_URL + reservation.getEventId() + "/decrement?count=" + reservation.getNombrePlaces(), null);

        return savedReservation;
    }

    // Endpoint appelé par le Payment-Service via OpenFeign
    @PutMapping("/{id}/confirm")
    public void confirmReservation(@PathVariable Long id) {
        // 1. Chercher la réservation
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));

        // 2. Changer le statut
        reservation.setStatus("CONFIRMED");

        // 3. Sauvegarder
        reservationRepository.save(reservation);
    }

    // Endpoint pour consulter une réservation par son ID
    @GetMapping("/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));
    }
}