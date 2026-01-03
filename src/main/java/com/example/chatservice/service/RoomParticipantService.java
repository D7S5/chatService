package com.example.chatservice.service;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomRole;
import com.example.chatservice.entity.RoomParticipant;

import java.util.List;

public interface RoomParticipantService {

    void joinRoom(String roomId, String userId);

    void leaveRoom(String roomId, String userId);

    void reconnect(String roomId, String userId);

    void kick(String roomId, String targetUserId, String byUserId);

    void ban(String roomId, String targetUserId, String byUserId, String reason);

    void changeRole(String roomId, String targetUserId, RoomRole role, String byUserId);

    void transferOwnership(String roomId, String newOwnerId, String byUserId);

    List<RoomParticipant> getActiveParticipants(String roomId);

    List<ParticipantDto> getParticipants(String roomId);
}
