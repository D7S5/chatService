package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminChangedResponse {
    private String userId;
    private String role;
}
