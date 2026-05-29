package com.example.notes.service;

import com.example.notes.data.entity.GalleryImage;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.GalleryImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.List;

@Service
public class ImageService {

    private final GalleryImageRepository galleryImageRepository;

    public ImageService(GalleryImageRepository galleryImageRepository) {
        this.galleryImageRepository = galleryImageRepository;
    }

    public List<GalleryImage> getImagesByUser(User user) {
        // Retrieve all uploaded images for the user
        return null;
    }

    @Transactional
    public GalleryImage uploadImage(String originalFilename, String contentType, long size, InputStream inputStream, User user) {
        // Validate, write to storage, save metadata, and return the new entity
        return null;
    }

    @Transactional
    public void deleteImage(GalleryImage image) {
        // Remove file from storage and delete database entry
    }
}
