package com.example.notes.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String filepath;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public GalleryImage() {}

    public GalleryImage(String filename, String title, String contentType, String filepath, LocalDateTime uploadTime, User user) {
        this.filename = filename;
        this.title = title;
        this.contentType = contentType;
        this.filepath = filepath;
        this.uploadTime = uploadTime;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getFilepath() { return filepath; }
    public void setFilepath(String filepath) { this.filepath = filepath; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
