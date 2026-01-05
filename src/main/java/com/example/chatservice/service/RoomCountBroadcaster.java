package com.example.chatservice.service;

import com.example.chatservice.dto.RoomCountDto;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.service.RoomParticipantServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCountBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomParticipantServiceImpl service;
    private final ChatRoomV2Repository chatRoomRepository;

    public void broadcast(String roomId) {
        int current = service.getCurrentCount(roomId);

        ChatRoomV2 room = chatRoomRepository.findById(roomId)
                .orElseThrow();

        RoomCountDto dto = new RoomCountDto(
                current,
                room.getMaxParticipants()
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/count",
                dto
        );
    }
}
