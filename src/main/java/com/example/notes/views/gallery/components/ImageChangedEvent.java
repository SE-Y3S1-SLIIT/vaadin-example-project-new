package com.example.notes.views.gallery.components;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

/**
 * Event fired when a gallery image is uploaded or deleted.
 * Used to notify interested UI components (like GalleryView and MainLayout) to refresh their views.
 */
public class ImageChangedEvent extends ComponentEvent<UI> {
    
    public ImageChangedEvent(UI source) {
        super(source, false);
    }
}
