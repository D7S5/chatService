package com.example.chatService.service;

import com.example.chatService.config.ImageUploadProperties;
import com.example.chatService.dto.ImageUploadResponse;
import com.example.chatService.exception.InvalidImageFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesProfileImageAndReturnsPublicUrl() throws Exception {
        ImageStorageService service = new ImageStorageService(
                new ImageUploadProperties(tempDir.toString(), "/uploads", 1_024_000)
        );
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "avatar.png",
                "image/png",
                "png-data".getBytes()
        );

        ImageUploadResponse response = service.storeProfileImage("user-1", file);

        assertThat(response.imageUrl()).startsWith("/uploads/profile-images/user-1/");
        assertThat(response.originalFilename()).isEqualTo("avatar.png");
        assertThat(response.contentType()).isEqualTo("image/png");
        assertThat(response.size()).isEqualTo(file.getSize());

        Path storedDirectory = tempDir.resolve("profile-images").resolve("user-1");
        assertThat(Files.exists(storedDirectory)).isTrue();
        assertThat(Files.list(storedDirectory).count()).isEqualTo(1);
    }

    @Test
    void rejectsNonImageFile() {
        ImageStorageService service = new ImageStorageService(
                new ImageUploadProperties(tempDir.toString(), "/uploads", 1_024_000)
        );
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> service.storeProfileImage("user-1", file))
                .isInstanceOf(InvalidImageFileException.class)
                .hasMessageContaining("이미지");
    }

    @Test
    void storesChatImageAndReturnsChatImageUrl() throws Exception {
        ImageStorageService service = new ImageStorageService(
                new ImageUploadProperties(tempDir.toString(), "/uploads", 1_024_000)
        );
        MockMultipartFile file = new MockMultipartFile(
                "image", "message.webp", "image/webp", "webp-data".getBytes()
        );

        ImageUploadResponse response = service.storeChatImage("user-1", file);

        assertThat(response.imageUrl()).startsWith("/uploads/chat-images/user-1/");
        assertThat(Files.list(tempDir.resolve("chat-images/user-1")).count()).isEqualTo(1);
    }
}
