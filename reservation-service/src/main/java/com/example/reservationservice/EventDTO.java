package com.example.reservationservice;

import lombok.Data;

@Data
public class EventDTO {
    private Long id;
    private String nom;
    private int nombreTicketsDisponibles;
    private Double prix; // <-- Ajoute ça
}