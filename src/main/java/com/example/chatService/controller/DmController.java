package com.example.chatService.controller;

import com.example.chatService.entity.DMMessage;
import com.example.chatService.service.DMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
    public class DmController {

    private final DMService dmService;

    @PostMapping("/start")
    public ResponseEntity<?> startDM(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(
                dmService.startOrGetRoom(payload.get("userA"), payload.get("userB")));
    }
    @GetMapping("/messages/{roomId}")
    public List<DMMessage> getMessages(@PathVariable String roomId) {
        return dmService.getMessages(roomId);
    }

    @GetMapping("/list/{userId}")
        public ResponseEntity<?> getUserDMs(@PathVariable String userId) {
            return ResponseEntity.ok(dmService.getUserRoomsWithUnread(userId));
    }

    @PutMapping("/messages/{roomId}/read")
    public ResponseEntity<?> markRead(@PathVariable String roomId, @RequestParam String userId) {
        dmService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }
}