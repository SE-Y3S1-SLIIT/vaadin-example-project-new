package com.example.notes.endpoint;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Responsibilities:
 * - Expose REST/Hilla endpoints for upload, listing and streaming images.
 * - Delegate to `ImageService` and `ImageStorage`.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {
    // TODO: inject ImageService and expose endpoints like upload, list, stream, delete
}
