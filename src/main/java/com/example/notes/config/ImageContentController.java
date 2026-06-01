package com.example.notes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImageContentController {

    private static final String IMAGE_CONTENT_ROUTE = "/image-content/{filename:.+}";

    private final Path uploadDirectory;

    public ImageContentController(@Value("${image.upload-dir:uploads}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping(IMAGE_CONTENT_ROUTE)
    public ResponseEntity<Resource> getImageContent(@PathVariable String filename) {
        Path imagePath = resolveSafeImagePath(filename);
        if (imagePath == null || !Files.exists(imagePath) || !Files.isReadable(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(imagePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

                MediaType mediaType = resolveMediaType(imagePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Path resolveSafeImagePath(String filename) {
        String safeFilename = Paths.get(filename).getFileName().toString();
        if (!safeFilename.equals(filename)) {
            return null;
        }

        Path imagePath = uploadDirectory.resolve(safeFilename).normalize();
        return imagePath.startsWith(uploadDirectory) ? imagePath : null;
    }

    private MediaType resolveMediaType(Path imagePath) throws IOException {
        String contentType = Files.probeContentType(imagePath);
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
