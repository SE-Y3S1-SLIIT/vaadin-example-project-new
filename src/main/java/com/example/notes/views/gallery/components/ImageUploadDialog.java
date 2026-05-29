package com.example.notes.views.gallery.components;

import com.vaadin.flow.component.dialog.Dialog;
import java.io.InputStream;
import java.util.function.BiConsumer;

public class ImageUploadDialog extends Dialog {

    private final BiConsumer<String, InputStream> onUploadSuccessCallback;

    public ImageUploadDialog(BiConsumer<String, InputStream> onUploadSuccessCallback) {
        this.onUploadSuccessCallback = onUploadSuccessCallback;

        // Initialize Vaadin's Upload component
        // Set drag-and-drop zones, file size/extension limits
        // Build modern inputs (e.g. title text field)
    }

    private void configureUploadEvents() {
        // Handle file receiver, upload successful, and error callbacks
    }
}
