package com.example.notes.storage;

import java.io.InputStream;

/**
 * Simple filesystem-backed ImageStorage implementation template.
 * Responsibilities:
 * - Implement `ImageStorage` using configurable filesystem path.
 * - Handle filename collision and basic validation.
 */
public class FilesystemImageStorage implements ImageStorage {

    private final String basePath;

    public FilesystemImageStorage(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String store(String filename, String contentType, InputStream stream) throws Exception {
        // TODO: write stream to filesystem and return storage path
        return null;
    }

    @Override
    public InputStream load(String storagePath) throws Exception {
        // TODO: return input stream for stored file
        return null;
    }

    @Override
    public void delete(String storagePath) throws Exception {
        // TODO: delete file
    }
}
