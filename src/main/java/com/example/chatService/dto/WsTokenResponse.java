package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WsTokenResponse {
    private String wsToken;
    private int expiresIn;
}
