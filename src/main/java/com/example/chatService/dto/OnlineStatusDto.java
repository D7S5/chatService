package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OnlineStatusDto {
    private String userId;
    private String username;
    private boolean online;

}