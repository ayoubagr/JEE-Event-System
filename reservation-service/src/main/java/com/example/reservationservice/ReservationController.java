package com.example.reservationservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestTemplate restTemplate;

    // URL du service événement (via Eureka)
    private final String EVENT_SERVICE_URL = "http://event-service/events/";

    // ---------------------------------------------------------
    // 1. CRÉER UNE RÉSERVATION (AVEC CIRCUIT BREAKER)
    // ---------------------------------------------------------
    @PostMapping
    @CircuitBreaker(name = "eventService", fallbackMethod = "fallbackReserver")
    public Reservation reserver(@RequestBody Reservation reservation) {

        // ÉTAPE A : Appel au Micro-service Event (C'est ici que ça peut casser !)
        // On récupère les infos de l'événement
        EventDTO event = restTemplate.getForObject(EVENT_SERVICE_URL + reservation.getEventId(), EventDTO.class);

        // ÉTAPE B : Vérifications Métier
        if (event == null) {
            throw new RuntimeException("Événement introuvable !");
        }

        if (event.getNombreTicketsDisponibles() < reservation.getNombrePlaces()) {
            throw new RuntimeException("Désolé, plus assez de places !");
        }

        // ÉTAPE C : Sauvegarde en base locale (PENDING)
        reservation.setStatus("PENDING");
        Reservation savedReservation = reservationRepository.save(reservation);

        // ÉTAPE D : Mise à jour du stock dans l'autre service (Appel PUT)
        restTemplate.put(EVENT_SERVICE_URL + reservation.getEventId() + "/decrement?count=" + reservation.getNombrePlaces(), null);

        return savedReservation;
    }

    // ---------------------------------------------------------
    // 2. MÉTHODE DE SECOURS (FALLBACK)
    // ---------------------------------------------------------
    // Cette méthode est appelée automatiquement si "event-service" est en panne.
    // Elle DOIT avoir la même signature que la méthode 'reserver' + un paramètre Throwable.
    public Reservation fallbackReserver(Reservation reservation, Throwable t) {
        Reservation r = new Reservation();
        r.setUserId(reservation.getUserId());
        r.setEventId(reservation.getEventId());
        r.setNombrePlaces(reservation.getNombrePlaces());

        // On indique clairement que ça a échoué à cause du service tiers
        r.setStatus("FAILED_EVENT_SERVICE_DOWN");

        // On retourne cet objet "vide" pour ne pas planter l'application avec une erreur 500
        return r;
    }

    // ---------------------------------------------------------
    // 3. CONFIRMER UNE RÉSERVATION (Appelé par Payment-Service)
    // ---------------------------------------------------------
    @PutMapping("/{id}/confirm")
    public void confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));

        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);
    }

    // ---------------------------------------------------------
    // 4. CONSULTER UNE RÉSERVATION (Pour vérifier le statut)
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));
    }
}