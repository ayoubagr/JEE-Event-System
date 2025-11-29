package com.example.notificationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private JavaMailSender mailSender; // L'outil de Spring pour envoyer des mails

    @PostMapping("/send")
    public String sendNotification(@RequestBody NotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // 👇 METS TON VRAI EMAIL ICI (Le même que dans application.yml) 👇
            message.setFrom("ayoubstage.it@gmail.com");

            message.setTo(request.getEmail());
            message.setSubject("Confirmation de Réservation - JEE Event System");
            message.setText(request.getMessage());

            mailSender.send(message);

            log.info("✅ Email envoyé avec succès à {}", request.getEmail());
            return "Email envoyé avec succès !";

        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage());
            return "Erreur";
        }
    }
}