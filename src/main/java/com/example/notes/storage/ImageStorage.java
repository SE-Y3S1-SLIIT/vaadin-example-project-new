package com.example.notes.storage;

import java.io.InputStream;

/**
 * Storage abstraction for image binary persistence.
 * Responsibilities:
 * - Save image bytes and return a storage path/identifier.
 * - Stream image bytes for delivery.
 * - Delete stored image by identifier.
 */
public interface ImageStorage {
    String store(String filename, String contentType, InputStream stream) throws Exception;
    InputStream load(String storagePath) throws Exception;
    void delete(String storagePath) throws Exception;
}
