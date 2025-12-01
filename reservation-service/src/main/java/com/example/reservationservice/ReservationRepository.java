package com.example.reservationservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Requête pour compter la somme des places réservées par un utilisateur pour un événement donné
    // (Attention : retourne null si aucune réservation, à gérer dans le code)
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r WHERE r.userId = :userId AND r.eventId = :eventId")
    int countPlacesByUserAndEvent(Long userId, Long eventId);
}