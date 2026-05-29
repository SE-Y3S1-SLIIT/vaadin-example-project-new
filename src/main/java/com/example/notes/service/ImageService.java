package com.example.notes.service;

import com.example.notes.data.entity.GalleryImage;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.GalleryImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );

    private final GalleryImageRepository galleryImageRepository;
    private final Path uploadDirectory;

    public ImageService(
            GalleryImageRepository galleryImageRepository,
            @Value("${image.upload-dir:data/uploads}") String uploadDir
    ) {
        this.galleryImageRepository = galleryImageRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<GalleryImage> getImagesByUser(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        return galleryImageRepository.findByUser(user).stream()
                .sorted((left, right) -> right.getUploadTime().compareTo(left.getUploadTime()))
                .collect(Collectors.toList());
    }

    @Transactional
    public GalleryImage uploadImage(String originalFilename, String contentType, long size, InputStream inputStream, User user) {
        validateUploadRequest(originalFilename, contentType, size, inputStream, user);
        createUploadDirectoryIfNeeded();

        String sanitizedFilename = sanitizeFilename(originalFilename);
        String generatedFilename = buildGeneratedFilename(sanitizedFilename);
        Path targetPath = uploadDirectory.resolve(generatedFilename).normalize();

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store uploaded image locally", exception);
        }

        GalleryImage image = new GalleryImage(
                generatedFilename,
                stripExtension(sanitizedFilename),
                contentType,
                targetPath.toString(),
                LocalDateTime.now(),
                user
        );

        return galleryImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(GalleryImage image) {
        if (image == null) {
            return;
        }

        String filePath = image.getFilepath();
        if (filePath != null && !filePath.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to delete stored image file", exception);
            }
        }

        galleryImageRepository.delete(image);
    }

    private void validateUploadRequest(String originalFilename, String contentType, long size, InputStream inputStream, User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("A valid user is required to upload an image");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Image content is required");
        }
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type is required");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Image size must be greater than zero");
        }
        if (!isAllowedImageType(originalFilename, contentType)) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private void createUploadDirectoryIfNeeded() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create upload directory: " + uploadDirectory, exception);
        }
    }

    private boolean isAllowedImageType(String originalFilename, String contentType) {
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT).trim();
        if (ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            return true;
        }

        String extension = getFileExtension(originalFilename);
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp", "bmp" -> true;
            default -> false;
        };
    }

    private String buildGeneratedFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return extension.isBlank() ? uuid : uuid + "." + extension;
    }

    private String sanitizeFilename(String filename) {
        return Paths.get(filename).getFileName().toString();
    }

    private String stripExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex <= 0) {
            return filename;
        }
        return filename.substring(0, extensionIndex);
    }

    private String getFileExtension(String filename) {
        String sanitizedFilename = sanitizeFilename(filename);
        int extensionIndex = sanitizedFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == sanitizedFilename.length() - 1) {
            return "";
        }
        return sanitizedFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }
}
