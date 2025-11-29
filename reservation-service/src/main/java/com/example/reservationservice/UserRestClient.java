package com.example.reservationservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserRestClient {

    @GetMapping("/users/{id}")
    AppUser getUserById(@PathVariable Long id);
}