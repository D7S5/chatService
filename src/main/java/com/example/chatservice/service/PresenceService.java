package com.example.chatservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void addOnlineUser(String userId) {
        onlineUsers.add(userId);
    }

    public void removeOnlineUser(String userId) {
        onlineUsers.remove(userId);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}
