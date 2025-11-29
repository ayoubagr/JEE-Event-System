package com.example.reservationservice;

import lombok.Data;

@Data
public class AppUser {
    private Long id;
    private String username;
    private String email; // C'est ce champ qui nous intéresse !
}