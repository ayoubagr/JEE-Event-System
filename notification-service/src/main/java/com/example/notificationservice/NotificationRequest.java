package com.example.notificationservice;

import lombok.Data;

@Data
public class NotificationRequest {
    private String email;
    private String message;
}