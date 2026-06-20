package com.example.chatService.service;

import com.example.chatService.config.ImageUploadProperties;
import com.example.chatService.dto.ChatMessageType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatMessageValidatorTest {

    private final ChatMessageValidator validator = new ChatMessageValidator(
            new ImageUploadProperties("uploads", "/uploads", 10_485_760)
    );

    @Test
    void defaultsMissingTypeToText() {
        assertThat(validator.validate(null, "hello", null, "user-1"))
                .isEqualTo(ChatMessageType.TEXT);
    }

    @Test
    void acceptsOwnedUploadedImage() {
        assertThat(validator.validate(
                ChatMessageType.IMAGE,
                null,
                "/uploads/chat-images/user-1/image.png",
                "user-1"
        )).isEqualTo(ChatMessageType.IMAGE);
    }

    @Test
    void rejectsAnotherUsersImage() {
        assertThatThrownBy(() -> validator.validate(
                ChatMessageType.IMAGE,
                null,
                "/uploads/chat-images/user-2/image.png",
                "user-1"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
