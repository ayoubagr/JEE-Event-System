package com.example.reservationservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/reservations")
@CrossOrigin(origins = "http://localhost:4200") // <--- AJOUTEZ CETTE LIGNE
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

        // 1. Récupérer l'événement
        EventDTO event = restTemplate.getForObject(EVENT_SERVICE_URL + reservation.getEventId(), EventDTO.class);
        if (event == null) throw new RuntimeException("Événement inconnu");

        // 2. RÈGLE MÉTIER : Vérification Stock
        if (event.getNombreTicketsDisponibles() < reservation.getNombrePlaces()) {
            throw new RuntimeException("Plus assez de places !");
        }

        // 3. RÈGLE MÉTIER : Max 4 tickets par personne
        int dejaReserve = reservationRepository.countPlacesByUserAndEvent(reservation.getUserId(), reservation.getEventId());

        if (dejaReserve + reservation.getNombrePlaces() > 4) {
            throw new RuntimeException("Limite atteinte ! Vous avez déjà " + dejaReserve + " places. Max 4 autorisées.");
        }

        // 4. CALCUL AUTOMATIQUE DU PRIX
        double total = event.getPrix() * reservation.getNombrePlaces();
        reservation.setMontantTotal(total);

        // 5. Sauvegarde et Décrémentation
        reservation.setStatus("PENDING");
        Reservation saved = reservationRepository.save(reservation);

        restTemplate.put(EVENT_SERVICE_URL + reservation.getEventId() + "/decrement?count=" + reservation.getNombrePlaces(), null);

        return saved;
    }

    // ---------------------------------------------------------
    // 2. MÉTHODE DE SECOURS (FALLBACK)
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // 2. MÉTHODE DE SECOURS (FALLBACK) - VERSION INTELLIGENTE
    // ---------------------------------------------------------
    public Reservation fallbackReserver(Reservation reservation, Throwable t) {

        // CAS 1 : Si c'est une règle métier qu'on a levée nous-mêmes (ex: Limite, Stock...)
        // On relance l'erreur pour que Postman l'affiche (Erreur 500)
        if (t.getMessage() != null && (t.getMessage().contains("Limite atteinte") || t.getMessage().contains("Plus assez de places") || t.getMessage().contains("inconnu"))) {
            throw new RuntimeException(t.getMessage());
        }

        // CAS 2 : C'est une vraie panne technique (Event-Service est down)
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