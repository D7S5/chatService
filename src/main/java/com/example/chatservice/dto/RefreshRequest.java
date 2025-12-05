package com.example.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
public record RefreshRequest(@NotBlank String refreshToken) {

}