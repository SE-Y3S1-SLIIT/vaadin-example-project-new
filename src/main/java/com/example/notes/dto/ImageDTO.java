package com.example.notes.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object for image metadata used by UI or API.
 */
public class ImageDTO {
    private Long id;
    private String filename;
    private String url;
    private String contentType;
    private LocalDateTime uploadedAt;

    public ImageDTO() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
