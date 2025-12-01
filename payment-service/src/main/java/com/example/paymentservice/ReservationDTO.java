package com.example.paymentservice; // Ajuste selon ton package

import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;
    private Long userId;
    private Long eventId;
    private int nombrePlaces;
    private String status;
    private Double montantTotal; // C'est ce champ qui nous intéresse pour la vérif !
}