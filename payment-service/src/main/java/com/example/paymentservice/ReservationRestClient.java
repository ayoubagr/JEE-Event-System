package com.example.paymentservice; // Ajuste selon ton package

import com.example.paymentservice.ReservationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "reservation-service")
public interface ReservationRestClient {

    // 1. Nouvelle méthode pour LIRE les infos (et le prix)
    @GetMapping("/reservations/{id}")
    ReservationDTO getReservation(@PathVariable Long id);

    // 2. Méthode existante pour CONFIRMER
    @PutMapping("/reservations/{id}/confirm")
    void confirmReservation(@PathVariable Long id);
}