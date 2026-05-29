package com.example.notes.views.gallery;

import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.data.entity.GalleryImage;
import com.example.notes.service.ImageService;
import com.example.notes.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.InputStream;
import java.util.List;

import com.example.notes.views.gallery.components.ImageCard;

@Route(value = "", layout = MainLayout.class)
@RouteAlias("gallery")
@PageTitle("Gallery | Image Upload Gallery")
@PermitAll
public class GalleryView extends VerticalLayout {

    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final ImageService imageService;
    private final User currentUser;
    private final FlexLayout galleryGrid = new FlexLayout();
    private final Span emptyGalleryState = new Span("No images uploaded yet. Use the upload section above to add your first image.");

    public GalleryView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
        this.imageService = imageService;
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        configureLayout();
        add(createUploadSection(), createGallerySection());
        refreshGallery();
    }

    private void configureLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void refreshGallery() {
        galleryGrid.removeAll();

        List<GalleryImage> images = imageService.getImagesByUser(currentUser);
        if (images.isEmpty()) {
            galleryGrid.add(emptyGalleryState);
            return;
        }

        images.forEach(image -> galleryGrid.add(new ImageCard(image, deletedImage -> {
            imageService.deleteImage(deletedImage);
            refreshGallery();
        })));
    }

    private Component createGallerySection() {
        VerticalLayout gallerySection = new VerticalLayout();
        gallerySection.setPadding(true);
        gallerySection.setSpacing(true);
        gallerySection.setWidthFull();
        gallerySection.setMaxWidth("72rem");
        gallerySection.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H3 heading = new H3("Your Gallery");
        heading.getStyle().set("margin-bottom", "0");

        galleryGrid.setWidthFull();
        galleryGrid.setWrapMode(FlexLayout.WrapMode.WRAP);
        galleryGrid.setFlexDirection(FlexLayout.FlexDirection.ROW);
        galleryGrid.setJustifyContentMode(JustifyContentMode.START);
        galleryGrid.setAlignItems(Alignment.START);
        galleryGrid.getStyle().set("gap", "1rem");

        emptyGalleryState.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("padding", "1rem 0");

        gallerySection.add(heading, galleryGrid);
        return gallerySection;
    }

    private Component createUploadSection() {
        VerticalLayout uploadSection = new VerticalLayout();
        uploadSection.setPadding(true);
        uploadSection.setSpacing(true);
        uploadSection.setWidthFull();
        uploadSection.setMaxWidth("42rem");
        uploadSection.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        uploadSection.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 title = new H3("Upload Image");
        Span helperText = new Span("PNG, JPG, and JPEG files only. Maximum size: 5 MB.");
        helperText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setDropAllowed(true);
        upload.setAcceptedFileTypes("image/png", "image/jpg", "image/jpeg");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
        upload.setAutoUpload(true);

        upload.addFileRejectedListener(event -> showErrorNotification("Only PNG, JPG, and JPEG files up to 5 MB are allowed."));
        upload.addFailedListener(event -> showErrorNotification("Upload failed. Please try again."));
        upload.addSucceededListener(event -> handleUploadSuccess(buffer, event));

        uploadSection.add(title, helperText, upload);
        return uploadSection;
    }

    private void handleUploadSuccess(MemoryBuffer buffer, SucceededEvent event) {
        if (event.getContentLength() > MAX_FILE_SIZE_BYTES) {
            showErrorNotification("File is too large. Maximum allowed size is 5 MB.");
            return;
        }

        try (InputStream inputStream = buffer.getInputStream()) {
            imageService.uploadImage(
                    event.getFileName(),
                    event.getMIMEType(),
                    event.getContentLength(),
                    inputStream,
                    currentUser
            );
            refreshGallery();
            showSuccessNotification("Image uploaded successfully.");
        } catch (Exception exception) {
            showErrorNotification("Upload failed: " + exception.getMessage());
        }
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
