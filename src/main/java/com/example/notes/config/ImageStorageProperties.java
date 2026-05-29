package com.example.notes.config;

/**
 * Configuration properties for image storage.
 * Responsibilities:
 * - Bind `application.properties` keys (e.g. `image.storage.path`).
 */
public class ImageStorageProperties {
    private String storagePath = "data/uploads";
    private long maxUploadBytes = 10 * 1024 * 1024; // 10 MB default

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public long getMaxUploadBytes() {
        return maxUploadBytes;
    }

    public void setMaxUploadBytes(long maxUploadBytes) {
        this.maxUploadBytes = maxUploadBytes;
    }
}
