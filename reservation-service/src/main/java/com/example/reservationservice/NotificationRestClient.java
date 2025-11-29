package com.example.reservationservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationRestClient {

    @PostMapping("/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);
}