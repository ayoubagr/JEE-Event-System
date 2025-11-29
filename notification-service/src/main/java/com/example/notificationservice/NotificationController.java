package com.example.notificationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Slf4j // Permet d'utiliser 'log.info' directement
public class NotificationController {

    @PostMapping("/send")
    public String sendNotification(@RequestBody NotificationRequest request) {
        // Simulation d'envoi d'email (Logs dans la console)
        log.info("=====================================================");
        log.info("📧 ENVOI EMAIL DESTINATAIRE : {}", request.getEmail());
        log.info("📝 MESSAGE : {}", request.getMessage());
        log.info("✅ Statut : Envoyé avec succès");
        log.info("=====================================================");

        return "Notification reçue et traitée !";
    }
}