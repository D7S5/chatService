package com.example.chatService.controller;

import com.example.chatService.dto.ImageUploadResponse;
import com.example.chatService.dto.NicknameDto;
import com.example.chatService.dto.NicknameRequest;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.ImageStorageService;
import com.example.chatService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ImageStorageService imageStorageService;

    @PostMapping("/set-nickname")
    public ResponseEntity<?> setNickname(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NicknameRequest request) {
            String trimmed = userService.setNickname(principal.getId(), request.nickname());
        return ResponseEntity.ok(new NicknameDto(trimmed));
    }

    @PostMapping("/profile-image")
    public ResponseEntity<ImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image) {
        ImageUploadResponse response = imageStorageService.storeProfileImage(principal.getId(), image);
        return ResponseEntity.ok(response);
    }
}
