package com.example.chatService.service;

import com.example.chatService.dto.ParticipantDto;

public interface ParticipantEventPublisher {
    void broadcastJoin(String roomId, ParticipantDto participant);

    void broadcastLeave(String roomId, ParticipantDto participant);

    void broadcastLeave(String roomId, ParticipantDto participant, String reason);

    void broadcastOwnerChanged(String roomId, String newOwnerId);
}
