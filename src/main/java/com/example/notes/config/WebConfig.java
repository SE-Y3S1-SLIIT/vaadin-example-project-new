package com.example.notes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirectory = Paths.get("uploads").toAbsolutePath().normalize();

        // Ensure the directory exists so Spring can register the resource handler
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create uploads directory on startup: " + e.getMessage());
        }

        // Build a proper file:/// URI with forward slashes (required on Windows too)
        // toUri().toString() produces: file:///D:/path/to/uploads
        // We append "/" to tell Spring it's a directory resource location
        String uploadPath = uploadDirectory.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}