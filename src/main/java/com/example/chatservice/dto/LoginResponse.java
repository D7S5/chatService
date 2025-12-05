package com.example.chatservice.dto;

public record LoginResponse(String accessToken, String refreshToken, UserDto user) {
}
