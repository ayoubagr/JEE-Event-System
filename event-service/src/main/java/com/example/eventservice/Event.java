package com.example.eventservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String lieu;
    private int nombreTicketsDisponibles;

    // --- NOUVEAUX CHAMPS ---
    private Double prix;           // Indispensable pour le calcul du total
    private String organisateur;   // Ex: "La Ligue"
    private String participants;   // Ex: "Raja vs Wydad" (ou une List<String> avec @ElementCollection)
}