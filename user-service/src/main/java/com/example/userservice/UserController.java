package com.example.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Récupérer tous les utilisateurs
    @GetMapping
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Récupérer un utilisateur par son ID
    @GetMapping("/{id}")
    public AppUser getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID : " + id));
    }

    // 3. Créer un nouvel utilisateur
    @PostMapping
    public AppUser createUser(@RequestBody AppUser user) {
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public AppUser login(@RequestBody AppUser loginRequest) {
        // Chercher par username (Ajouter findByUsername dans le Repo)
        AppUser user = userRepository.findByUsername(loginRequest.getUsername());

        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            return user; // Connexion réussie
        } else {
            throw new RuntimeException("Mauvais identifiants");
        }
    }
}