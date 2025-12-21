package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantDto {
    private String userId;
    private String username;
}
