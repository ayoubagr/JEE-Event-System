package com.example.paymentservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRestClient reservationRestClient; // On injecte le client Feign

    @PostMapping
    public Payment payer(@RequestBody Payment payment) {
        // 1. On enregistre le paiement dans la base locale
        payment.setStatus("SUCCESS");
        Payment savedPayment = paymentRepository.save(payment);

        // 2. On appelle le micro-service Réservation pour valider le ticket
        // (Communication Synchrone via OpenFeign)
        reservationRestClient.confirmReservation(payment.getReservationId());

        return savedPayment;
    }
}