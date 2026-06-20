package com.example.chatService.dto;

public record ImageUploadResponse(
        String imageUrl,
        String originalFilename,
        String contentType,
        long size
) {
}
