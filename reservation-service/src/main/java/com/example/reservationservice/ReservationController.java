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

        // Mise à jour du statut
        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);

        // --- DÉBUT CODE BONUS : ENVOI NOTIFICATION ---
        try {
            // Création du message
            NotificationRequest req = new NotificationRequest();
            req.setEmail("client_user_" + reservation.getUserId() + "@gmail.com");
            req.setMessage("Félicitations ! Votre réservation N°" + reservation.getId() + " est validée.");

            // Appel au micro-service Notification via OpenFeign
            notificationRestClient.sendNotification(req);

        } catch (Exception e) {
            // Important : On met un try-catch pour ne pas bloquer la confirmation
            // si le service de notification est en panne.
            System.err.println("Erreur lors de l'envoi de la notification : " + e.getMessage());
        }
        // --- FIN CODE BONUS ---
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