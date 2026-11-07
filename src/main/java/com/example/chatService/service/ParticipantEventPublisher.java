package com.example.chatService.service;

import com.example.chatService.dto.ParticipantDto;

public interface ParticipantEventPublisher {
    void broadcastOwnerChanged(String roomId, String newOwnerId);
}
