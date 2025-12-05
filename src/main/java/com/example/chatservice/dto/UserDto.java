package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private boolean online;

    public UserDto(String id, String username) {
        this.id = id;
        this.username = username;
    }
}
