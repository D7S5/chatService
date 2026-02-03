package com.example.chatService.dto;

public record LoginResponse(String accessToken, UserDto user) {
}
