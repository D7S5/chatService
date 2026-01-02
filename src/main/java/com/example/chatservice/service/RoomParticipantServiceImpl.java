package com.example.chatservice.service;

import com.example.chatservice.dto.RoomRole;
import com.example.chatservice.entity.RoomParticipant;
import com.example.chatservice.repository.RoomParticipantRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoomParticipantServiceImpl implements RoomParticipantService{

    private final RoomParticipantRepository repository;
    private final StringRedisTemplate redis;
    @Override
    public void joinRoom(String roomId, String userId) {

    }

    @Override
    public void leaveRoom(String roomId, String userId) {

    }

    @Override
    public void reconnect(String roomId, String userId) {

    }

    @Override
    public void kick(String roomId, String targetUserId, String byUserId) {

    }

    @Override
    public void ban(String roomId, String targetUserId, String byUserId, String reason) {

    }

    @Override
    public void changeRole(String roomId, String targetUserId, RoomRole role, String byUserId) {

    }

    @Override
    public void transferOwnership(String roomId, String newOwnerId, String byUserId) {

    }

    @Override
    public List<RoomParticipant> getActiveParticipants(String roomId) {
        return null;
    }
}
