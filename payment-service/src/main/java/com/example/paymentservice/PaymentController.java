package com.example.paymentservice; // Ajuste selon ton package

import com.example.paymentservice.ReservationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:4200") // <--- AJOUTEZ CETTE LIGNE
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRestClient reservationRestClient;

    @PostMapping
    public Payment payer(@RequestBody Payment payment) {

        // 1. Récupérer les détails de la réservation via OpenFeign
        ReservationDTO reservation = reservationRestClient.getReservation(payment.getReservationId());

        // 2. Vérification de sécurité (si la réservation n'existe pas, Feign lancera une erreur 404)
        if (reservation == null) {
            throw new RuntimeException("Réservation introuvable !");
        }

        // 3. LOGIQUE MÉTIER : Vérifier le montant
        // On compare le montant envoyé par l'utilisateur avec le montant total calculé par la réservation
        if (payment.getMontant() < reservation.getMontantTotal()) {
            throw new RuntimeException("Erreur de paiement : Montant insuffisant ! Total requis : " + reservation.getMontantTotal());
        }

        // 4. Si tout est OK, on enregistre le paiement
        payment.setStatus("SUCCESS");
        Payment savedPayment = paymentRepository.save(payment);

        // 5. On confirme la réservation dans l'autre service
        reservationRestClient.confirmReservation(payment.getReservationId());

        return savedPayment;
    }
}