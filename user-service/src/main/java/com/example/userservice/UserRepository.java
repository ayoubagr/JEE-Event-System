package com.example.userservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    // Rien à ajouter ici, Spring Data JPA fait tout le travail magique !
}