package com.example.chatService.service;

import com.example.chatService.config.ImageUploadProperties;
import com.example.chatService.dto.ImageUploadResponse;
import com.example.chatService.exception.FileStorageException;
import com.example.chatService.exception.InvalidImageFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final ImageUploadProperties imageUploadProperties;

    public ImageUploadResponse storeProfileImage(String userId, MultipartFile image) {
        return storeImage(userId, image, "profile-images");
    }

    public ImageUploadResponse storeChatImage(String userId, MultipartFile image) {
        return storeImage(userId, image, "chat-images");
    }

    private ImageUploadResponse storeImage(String userId, MultipartFile image, String category) {
        validate(image);

        String extension = extractExtension(image.getContentType());
        String storedFileName = UUID.randomUUID() + "." + extension;
        Path uploadRoot = Path.of(imageUploadProperties.dir()).toAbsolutePath().normalize();
        Path userDirectory = uploadRoot.resolve(category).resolve(userId).normalize();
        Path targetFile = userDirectory.resolve(storedFileName).normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            throw new InvalidImageFileException("잘못된 파일 경로입니다.");
        }

        try {
            Files.createDirectories(userDirectory);
            Files.copy(image.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("이미지 저장에 실패했습니다.", e);
        }

        String imageUrl = buildPublicUrl(category, userId, storedFileName);
        return new ImageUploadResponse(
                imageUrl,
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSize()
        );
    }

    private void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageFileException("비어 있는 파일은 업로드할 수 없습니다.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidImageFileException("jpg, jpeg, png, gif, webp 이미지만 업로드할 수 있습니다.");
        }

        if (image.getSize() > imageUploadProperties.maxImageSizeBytes()) {
            throw new InvalidImageFileException("업로드 가능한 최대 파일 크기를 초과했습니다.");
        }
    }

    private String buildPublicUrl(String category, String userId, String storedFileName) {
        String prefix = imageUploadProperties.publicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/uploads";
        } else if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }

        return prefix + "/" + category + "/" + userId + "/" + storedFileName;
    }

    private String extractExtension(String contentType) {
        return switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> throw new InvalidImageFileException("지원하지 않는 이미지 형식입니다.");
        };
    }
}
