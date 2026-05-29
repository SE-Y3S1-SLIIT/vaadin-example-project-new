package com.example.notes.views.gallery;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.example.notes.views.MainLayout;

/**
 * Responsibilities:
 * - Host the gallery UI and compose reusable components.
 * - Wire `ImageService` to load images and handle navigation.
 */
@Route(value = "gallery", layout = MainLayout.class)
public class ImageGalleryView extends VerticalLayout {

    public ImageGalleryView() {
        // TODO: inject services and assemble components
    }
}
