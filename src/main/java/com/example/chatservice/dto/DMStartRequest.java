package com.example.chatservice.dto;

import lombok.Data;

@Data
public class DMStartRequest {
    private String userA; // UUID
    private String userB; // Target UUID
}
