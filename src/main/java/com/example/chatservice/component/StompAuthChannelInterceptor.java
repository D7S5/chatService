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

        if (accessor == null) return message;

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            if (destination == null) return message;

            if (destination.startsWith("/topic/chat/")) {
                String roomId = destination.substring("/topic/chat/".length());
                String userId = accessor.getUser().getName();
                System.out.println("userId = " + userId);

                boolean isParticipant =
                        repository.existsByRoomIdAndUserId(roomId, userId);

                if (!isParticipant) {
                    throw new MessagingException("NOT_PARTICIPANTS");
                }

                boolean banned =
                        repository.existsByRoomIdAndUserIdAndIsBannedTrue(roomId, userId);

                if (banned) {
                    throw new MessagingException("BANNED");
                }
            }
        }
        return message;
    }
}
