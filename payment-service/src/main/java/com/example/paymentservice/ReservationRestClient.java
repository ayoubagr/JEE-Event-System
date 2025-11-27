package com.example.paymentservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// name = nom exact du service réservation dans Eureka
@FeignClient(name = "reservation-service")
public interface ReservationRestClient {

    // On dit : "Va appeler la méthode confirm sur ce service"
    @PutMapping("/reservations/{id}/confirm")
    void confirmReservation(@PathVariable Long id);
}