package com.example.chatservice.service;

import org.springframework.stereotype.Service;
;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    private final ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();

    public void add(String username, String sessionId) {
        userSessions.put(username, sessionId);
    }

    public void removeBySessionId(String sessionId) {
        userSessions.entrySet().removeIf(e -> e.getValue().equals(sessionId));
    }

    public void removeByUsername(String username) {
        userSessions.remove(username);
    }

    public Set<String> getOnlineUsernames() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }
    public String getSessionId(String username) {
        return userSessions.get(username);
    }
}