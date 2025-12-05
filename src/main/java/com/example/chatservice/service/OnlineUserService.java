package com.example.chatservice.service;

import com.example.chatservice.dto.OnlineUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {
    private final Map<String, OnlineUser> onlineUsers = new ConcurrentHashMap<>();

    public void addUser(String uuid, String username) {
        onlineUsers.put(uuid, new OnlineUser(uuid, username));
    }

    public void removeUser(String uuid) {
        onlineUsers.remove(uuid);
    }

    public Collection<OnlineUser> getOnlineUsers() {
        return onlineUsers.values();
    }
}
