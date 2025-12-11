package com.example.chatservice.dto;

public record LoginResponse(String accessToken, UserDto user) {
}
