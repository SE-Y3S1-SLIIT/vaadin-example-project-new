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

    private final Path uploadDirectory;

    public ImageContentController(@Value("${image.upload-dir:uploads}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/image-content/{filename:.+}")
    public ResponseEntity<Resource> getImageContent(@PathVariable String filename) {
        String safeFilename = Paths.get(filename).getFileName().toString();
        if (!safeFilename.equals(filename)) {
            return ResponseEntity.badRequest().build();
        }

        Path imagePath = uploadDirectory.resolve(safeFilename).normalize();
        if (!imagePath.startsWith(uploadDirectory) || !Files.exists(imagePath) || !Files.isReadable(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(imagePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imagePath);
            MediaType mediaType = contentType != null
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
