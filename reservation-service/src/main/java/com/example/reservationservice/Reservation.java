package com.example.reservationservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;    // L'ID de l'utilisateur qui réserve
    private Long eventId;   // L'ID de l'événement
    private int nombrePlaces;
    private String status;  // EX: PENDING, CONFIRMED
}