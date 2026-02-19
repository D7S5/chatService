package com.example.chatService.service;

import com.example.chatService.dto.ChatMessageResponse;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.entity.GroupMessageEntity;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupRoomService {

    private final ChatRoomV2Repository chatRoomV2Repository;
    private final GroupMessageRepository groupMessageRepository;

    public List<ChatMessageResponse> messages(String roomId,  int limit) {
        int size = Math.min(limit, 200); // abuse 방지

        List<GroupMessageEntity> entities =
                groupMessageRepository.findRecent(
                        roomId,
                        PageRequest.of(0, size)
                );

        Collections.reverse(entities);

        return entities.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    public List<RoomResponse> getRoomsWithCount() {
        List<RoomResponse> res = chatRoomV2Repository.findAll().stream()
                .map(room ->
                        RoomResponse.from(room)
                )
                .toList();

        return res;

    }
}