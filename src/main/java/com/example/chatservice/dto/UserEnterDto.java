package com.example.chatservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEnterDto {
    private String uuid;
    private String username;
}