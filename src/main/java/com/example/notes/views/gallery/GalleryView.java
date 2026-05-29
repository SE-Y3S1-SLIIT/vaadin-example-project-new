package com.example.notes.views.gallery;

import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;

    public GalleryView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
        this.imageService = imageService;
        this.currentUser = null; // Will fetch authenticated user details

        // Construct layout, add upload tools and responsive gallery grid
    }

    private void configureLayout() {
        // Prepare container alignment and spacing
    }

    private void refreshGallery() {
        // Load user images and populate grid/cards
    }
}
