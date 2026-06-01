package com.example.notes.views.gallery.components;

import com.example.notes.data.entity.GalleryImage;
import com.example.notes.data.entity.User;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * A premium modal dialog for uploading images with optional titles.
 */
public class ImageUploadDialog extends Dialog {

    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final ImageService imageService;
    private final User currentUser;
    private final Consumer<GalleryImage> onUploadSuccessCallback;

    private final TextField titleField = new TextField("Image Title (Optional)");
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final Span helperText = new Span("PNG, JPG, and JPEG files only. Maximum size: 5 MB.");

    public ImageUploadDialog(ImageService imageService, User currentUser, Consumer<GalleryImage> onUploadSuccessCallback) {
        this.imageService = imageService;
        this.currentUser = currentUser;
        this.onUploadSuccessCallback = onUploadSuccessCallback;

        configureDialog();
        buildLayout();
        configureUploadEvents();
    }

    private void configureDialog() {
        setHeaderTitle("Upload New Image");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("450px");
        setMaxWidth("90vw");
    }

    private void buildLayout() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(true);
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(Alignment.STRETCH);

        titleField.setPlaceholder("Enter custom title or leave blank...");
        titleField.setWidthFull();
        titleField.setClearButtonVisible(true);

        helperText.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        upload.setWidthFull();
        upload.setDropAllowed(true);
        // Note: "image/jpg" is NOT a valid MIME type; browsers send "image/jpeg" for .jpg files.
        // Use ".jpg,.jpeg,.png,.gif,.webp" extensions plus MIME types for broadest compatibility.
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp", ".jpg", ".jpeg", ".png", ".gif", ".webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
        upload.setAutoUpload(true);

        contentLayout.add(titleField, helperText, upload);
        add(contentLayout);

        Button closeButton = new Button("Cancel", event -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(closeButton);
    }

    private void configureUploadEvents() {
        upload.addFileRejectedListener(event -> showErrorNotification("Only image files (PNG, JPG, JPEG, GIF, WEBP) up to 5 MB are allowed."));
        upload.addFailedListener(event -> showErrorNotification("Upload failed. Please try again."));
        upload.addSucceededListener(event -> {
            if (event.getContentLength() > MAX_FILE_SIZE_BYTES) {
                showErrorNotification("File is too large. Maximum allowed size is 5 MB.");
                return;
            }

            try (InputStream inputStream = buffer.getInputStream()) {
                GalleryImage savedImage = imageService.uploadImage(
                        event.getFileName(),
                        event.getMIMEType(),
                        event.getContentLength(),
                        inputStream,
                        currentUser
                );

                // Update title if custom title was provided
                String customTitle = titleField.getValue();
                if (customTitle != null && !customTitle.isBlank()) {
                    imageService.updateImageTitle(savedImage, customTitle);
                }

                showSuccessNotification("Image uploaded successfully.");
                
                // Fire event on session-scoped event bus to notify all active components
                ComponentUtil.fireEvent(UI.getCurrent(), new ImageChangedEvent(UI.getCurrent()));
                
                if (onUploadSuccessCallback != null) {
                    onUploadSuccessCallback.accept(savedImage);
                }
                
                close();
            } catch (Exception exception) {
                showErrorNotification("Upload failed: " + exception.getMessage());
            }
        });
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Position.TOP_END);
        notification.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 4000, Position.TOP_END);
        notification.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
    }
}
