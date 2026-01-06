package com.example.chatservice.component;

import com.example.chatservice.repository.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final RoomParticipantRepository repository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getUser().getName();
            System.out.println("stomp userId" + userId);
            String roomId = accessor.getFirstNativeHeader("roomId");

            if (repository.existsByRoomIdAndUserIdAndIsBannedTrue(roomId, userId)) {
                throw new MessagingException("BANNED");
            }
        }
        return message;
    }
}
