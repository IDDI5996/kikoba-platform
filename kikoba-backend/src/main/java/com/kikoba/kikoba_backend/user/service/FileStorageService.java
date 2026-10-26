package com.kikoba.kikoba_backend.user.service;

import com.kikoba.kikoba_backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2 MB

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    };

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Saves a profile avatar and returns its public URL.
     *
     * @param userId the user's UUID (used in the filename to avoid collisions)
     * @param file   the uploaded file
     * @return the public URL path (e.g., "/uploads/avatars/uuid-1234567890.jpg")
     */
    public String saveAvatar(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BadRequestException("File size exceeds 2 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowed(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, or WEBP images are allowed");
        }

        try {
            Path avatarsDir = Paths.get(uploadDir, "avatars").toAbsolutePath();
            Files.createDirectories(avatarsDir);

            String ext = getExtension(contentType);
            String filename = userId + "-" + System.currentTimeMillis() + ext;
            Path target = avatarsDir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/avatars/" + filename;
            log.info("Avatar saved for user {}: {}", userId, url);
            return url;

        } catch (IOException e) {
            log.error("Failed to save avatar for user {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to save file: " + e.getMessage());
        }
    }

    private boolean isAllowed(String contentType) {
        for (String type : ALLOWED_CONTENT_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    private String getExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Saves a chat attachment (image, pdf, video) and returns its public URL.
     */
    public String saveChatAttachment(java.util.UUID senderId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new BadRequestException("File size exceeds 10 MB");
        }

        try {
            java.nio.file.Path chatDir = java.nio.file.Paths
                    .get(uploadDir, "chat").toAbsolutePath();
            java.nio.file.Files.createDirectories(chatDir);

            String originalName = file.getOriginalFilename();
            String ext = ".bin";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String filename = senderId + "-" + System.currentTimeMillis() + ext;
            java.nio.file.Path target = chatDir.resolve(filename);

            java.nio.file.Files.copy(
                    file.getInputStream(),
                    target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            String url = "/uploads/chat/" + filename;
            log.info("Chat attachment saved for user {}: {}", senderId, url);
            return url;

        } catch (java.io.IOException e) {
            log.error("Failed to save chat attachment: {}", e.getMessage());
            throw new BadRequestException("Failed to save file: " + e.getMessage());
        }
    }
}