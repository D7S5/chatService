package com.example.chatservice.service;

import com.example.chatservice.dto.ParticipantDto;

public interface ParticipantEventPublisher {
    void broadcastJoin(String roomId, ParticipantDto participant);

    void broadcastLeave(String roomId, ParticipantDto participant);

    void broadcastLeave(String roomId, ParticipantDto participant, String reason);

    void broadcastOwnerChanged(String roomId, String newOwnerId);
}
