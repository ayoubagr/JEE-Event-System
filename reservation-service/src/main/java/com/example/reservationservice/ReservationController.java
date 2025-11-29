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

    // Injection du Client Feign pour les notifications (BONUS)
    @Autowired
    private NotificationRestClient notificationRestClient;

    @Autowired
    private UserRestClient userRestClient;

    // URL du service événement (via Eureka)
    private final String EVENT_SERVICE_URL = "http://event-service/events/";

    // ---------------------------------------------------------
    // 1. CRÉER UNE RÉSERVATION (AVEC CIRCUIT BREAKER)
    // ---------------------------------------------------------
    @PostMapping
    @CircuitBreaker(name = "eventService", fallbackMethod = "fallbackReserver")
    public Reservation reserver(@RequestBody Reservation reservation) {

        // ÉTAPE A : Appel au Micro-service Event
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
    public Reservation fallbackReserver(Reservation reservation, Throwable t) {
        Reservation r = new Reservation();
        r.setUserId(reservation.getUserId());
        r.setEventId(reservation.getEventId());
        r.setNombrePlaces(reservation.getNombrePlaces());
        r.setStatus("FAILED_EVENT_SERVICE_DOWN");
        return r;
    }

    // ---------------------------------------------------------
    // 3. CONFIRMER UNE RÉSERVATION + NOTIFICATION (BONUS)
    // ---------------------------------------------------------
    @PutMapping("/{id}/confirm")
    public void confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));

        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);

        // --- CODE NOTIFICATION DYNAMIQUE ---
        try {
            // A. On appelle le User-Service pour récupérer l'email réel
            AppUser user = userRestClient.getUserById(reservation.getUserId());

            // B. On prépare la notification
            NotificationRequest req = new NotificationRequest();

            // C. On utilise l'email récupéré du User-Service !
            req.setEmail(user.getEmail());

            req.setMessage("Bonjour " + user.getUsername() + ", votre réservation N°" + reservation.getId() + " est validée !");

            // D. On envoie
            notificationRestClient.sendNotification(req);

        } catch (Exception e) {
            System.err.println("Erreur notification : " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // 4. CONSULTER UNE RÉSERVATION
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable !"));
    }
}