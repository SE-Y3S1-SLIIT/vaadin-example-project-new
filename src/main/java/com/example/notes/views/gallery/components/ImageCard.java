package com.example.notes.views.gallery.components;

import com.example.notes.data.entity.GalleryImage;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;

public class ImageCard extends VerticalLayout {

    private final GalleryImage image;
    private final Consumer<GalleryImage> onDeleteCallback;

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback) {
        this.image = image;
        this.onDeleteCallback = onDeleteCallback;

        // Initialize UI components (Image, Title, Date, Delete Button)
        // Add styling for card feel (e.g. shadows, borders, transitions)
    }

    private void buildCardLayout() {
        // Construct the hierarchical component tree
    }
}
