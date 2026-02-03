package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomCountDto {
    private int current;
    private int max;
}
