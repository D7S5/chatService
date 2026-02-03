package com.example.chatService.service;

import com.example.chatService.dto.AdminChangedResponse;
import com.example.chatService.dto.ParticipantDto;
import com.example.chatService.dto.RoomRole;
import com.example.chatService.entity.RoomParticipant;

import java.util.List;

public interface RoomParticipantService {

    void joinRoom(String roomId, String userId);

    void leaveRoom(String roomId, String userId);

    void reconnect(String roomId, String userId);

    void kick(String roomId, String targetUserId, String byUserId);

    void ban(String roomId, String targetUserId, String byUserId, String reason);

    AdminChangedResponse toggleAdmin(String roomId, String requesterId, String targetUserId);

    void changeRole(String roomId, String targetUserId, RoomRole role, String byUserId);

    void transferOwnership(String roomId, String newOwnerId, String byUserId);

    int getCurrentCount(String roomId);

    List<RoomParticipant> getActiveParticipants(String roomId);

    List<ParticipantDto> getParticipants(String roomId);

    void broadcast(String roomId);

    boolean isParticipant(String roomId, String userId);
}
