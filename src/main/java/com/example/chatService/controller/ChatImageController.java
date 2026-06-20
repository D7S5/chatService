package com.example.chatService.controller;

import com.example.chatService.dto.ImageUploadResponse;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping("/images")
    public ResponseEntity<ImageUploadResponse> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(imageStorageService.storeChatImage(principal.getId(), image));
    }
}
