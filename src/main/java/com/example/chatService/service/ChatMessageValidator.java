package com.example.chatService.service;

import com.example.chatService.config.ImageUploadProperties;
import com.example.chatService.dto.ChatMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageValidator {

    private final ImageUploadProperties imageUploadProperties;

    public ChatMessageType validate(
            ChatMessageType messageType,
            String content,
            String imageUrl,
            String senderId) {
        ChatMessageType resolvedType = messageType == null ? ChatMessageType.TEXT : messageType;

        if (resolvedType == ChatMessageType.TEXT && (content == null || content.isBlank())) {
            throw new IllegalArgumentException("텍스트 메시지 내용이 필요합니다.");
        }

        if (resolvedType == ChatMessageType.IMAGE) {
            String publicPrefix = imageUploadProperties.publicUrlPrefix();
            if (publicPrefix == null || publicPrefix.isBlank()) {
                publicPrefix = "/uploads";
            } else if (!publicPrefix.startsWith("/")) {
                publicPrefix = "/" + publicPrefix;
            }
            String expectedPrefix = publicPrefix + "/chat-images/" + senderId + "/";
            if (imageUrl == null || !imageUrl.startsWith(expectedPrefix)) {
                throw new IllegalArgumentException("먼저 본인 계정으로 이미지를 업로드해야 합니다.");
            }
        }

        return resolvedType;
    }
}
