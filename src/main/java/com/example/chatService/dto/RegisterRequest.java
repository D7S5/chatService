package com.example.chatService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "아이디는 필수입니다") String username,
        @NotBlank(message = "비밀번호는 필수입니다") String password,
        @NotBlank(message = "이메일은 필수입니다") @Email(message = "유효한 이메일 형식이 아닙니다") String email
) {
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", password=****, email=" + email + "]";
    }
}