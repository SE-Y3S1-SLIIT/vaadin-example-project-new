package com.example.notes.views.gallery;

import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.data.entity.GalleryImage;
import com.example.notes.service.ImageService;
import com.example.notes.views.MainLayout;
import com.example.notes.views.gallery.components.ImageCard;
import com.example.notes.views.gallery.components.ImageUploadDialog;
import com.example.notes.views.gallery.components.ImageChangedEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "", layout = MainLayout.class)
@RouteAlias("gallery")
@PageTitle("Gallery | Image Upload Gallery")
@PermitAll
public class GalleryView extends HorizontalLayout implements BeforeEnterObserver {

    private final ImageService imageService;
    private final User currentUser;
    
    // Core Layout Panels
    private final VerticalLayout sidebarList = new VerticalLayout();
    private final Scroller sidebarScroller = new Scroller(sidebarList);
    private final VerticalLayout detailsArea = new VerticalLayout();
    
    // Components
    private final Span emptyGalleryState = new Span("No images uploaded yet.");
    private final VerticalLayout previewCard = new VerticalLayout();
    private final Image largeImagePreview = new Image();
    private final H3 imageTitle = new H3();
    
    // Details Metadata Rows
    private final Span metaFilename = new Span();
    private final Span metaSize = new Span();
    private final Span metaType = new Span();
    private final Span metaUploaded = new Span();
    
    // Actions toolbar components
    private final HorizontalLayout actionToolbar = new HorizontalLayout();
    private final Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
    private final Button downloadButton = new Button("Download", new Icon(VaadinIcon.DOWNLOAD));
    private final Anchor downloadAnchor = new Anchor();

    private Long selectedImageId;
    private final List<ImageCard> sidebarCardsList = new ArrayList<>();

    public GalleryView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
        this.imageService = imageService;
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        configureLayout();
        buildSidebar();
        buildDetailsArea();

        // Listen for gallery modifications in the session and refresh instantly
        ComponentUtil.addListener(UI.getCurrent(), ImageChangedEvent.class, event -> {
            refreshGallery();
        });

        refreshGallery();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Support direct deep linking or navigation parameters to select an image from drawer
        Optional<String> imageParam = event.getLocation().getQueryParameters()
                .getSingleParameter("image");
        if (imageParam.isPresent()) {
            try {
                this.selectedImageId = Long.parseLong(imageParam.get());
                refreshGallery();
            } catch (NumberFormatException e) {
                // Ignore invalid parameter
            }
        }
    }

    private void configureLayout() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "var(--lumo-contrast-5pct)");
    }

    private void buildSidebar() {
        sidebarList.setPadding(true);
        sidebarList.setSpacing(true);
        sidebarList.setWidthFull();
        sidebarList.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        VerticalLayout sidebarContainer = new VerticalLayout();
        sidebarContainer.setWidth("24rem");
        sidebarContainer.setHeightFull();
        sidebarContainer.setPadding(false);
        sidebarContainer.setSpacing(false);
        sidebarContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-right", "1px solid var(--lumo-contrast-10pct)");

        HorizontalLayout sidebarHeader = new HorizontalLayout();
        sidebarHeader.setWidthFull();
        sidebarHeader.setPadding(true);
        sidebarHeader.setAlignItems(Alignment.CENTER);
        sidebarHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        sidebarHeader.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-bottom", "0.75rem")
                .set("margin-bottom", "0.5rem");

        H3 sidebarTitle = new H3("Your Uploads");
        sidebarTitle.getStyle().set("margin", "0");
        
        Button openUploadDialogBtn = new Button("Upload Image", new Icon(VaadinIcon.UPLOAD));
        openUploadDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        openUploadDialogBtn.addClickListener(event -> {
            new ImageUploadDialog(imageService, currentUser, uploadedImage -> {
                selectedImageId = uploadedImage.getId();
                refreshGallery();
            }).open();
        });

        sidebarHeader.add(sidebarTitle, openUploadDialogBtn);

        sidebarScroller.setSizeFull();
        sidebarScroller.getStyle().set("padding", "0");

        sidebarContainer.add(sidebarHeader, sidebarScroller);
        add(sidebarContainer);
    }

    private void buildDetailsArea() {
        detailsArea.setSizeFull();
        detailsArea.setPadding(true);
        detailsArea.setSpacing(true);
        detailsArea.setAlignItems(Alignment.CENTER);
        detailsArea.setJustifyContentMode(JustifyContentMode.CENTER);

        Scroller detailsScroller = new Scroller(detailsArea);
        detailsScroller.setSizeFull();
        detailsScroller.getStyle().set("flex-grow", "1");

        // Styling the central preview container
        previewCard.setWidthFull();
        previewCard.setMaxWidth("52rem");
        previewCard.setPadding(true);
        previewCard.setSpacing(true);
        previewCard.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        previewCard.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "1.5rem");

        largeImagePreview.setWidthFull();
        largeImagePreview.setHeight("28rem");
        largeImagePreview.getStyle()
                .set("object-fit", "contain")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "zoom-in");
        
        // Click full-size image to view in modal lightbox
        largeImagePreview.addClickListener(event -> {
            if (selectedImageId != null) {
                imageService.getImagesByUser(currentUser).stream()
                        .filter(image -> image.getId().equals(selectedImageId))
                        .findFirst()
                        .ifPresent(this::openLightboxDialog);
            }
        });

        imageTitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("word-break", "break-word");

        // Metadata grid layout
        VerticalLayout metaInfoSection = new VerticalLayout();
        metaInfoSection.setPadding(false);
        metaInfoSection.setSpacing(false);
        metaInfoSection.setWidthFull();
        metaInfoSection.getStyle()
                .set("gap", "0.35rem")
                .set("padding-top", "1rem")
                .set("border-top", "1px solid var(--lumo-contrast-5pct)");

        metaInfoSection.add(
                createMetaRow("Filename:", metaFilename),
                createMetaRow("Size:", metaSize),
                createMetaRow("Type:", metaType),
                createMetaRow("Uploaded:", metaUploaded)
        );

        // Buttons
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteButton.addClickListener(event -> confirmAndExecuteDeletion());

        downloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        downloadAnchor.add(downloadButton);
        downloadAnchor.getElement().setAttribute("download", true);

        actionToolbar.setPadding(false);
        actionToolbar.setSpacing(true);
        actionToolbar.setWidthFull();
        actionToolbar.setJustifyContentMode(JustifyContentMode.END);
        actionToolbar.add(downloadAnchor, deleteButton);

        previewCard.add(largeImagePreview, imageTitle, metaInfoSection, actionToolbar);
        
        // Configure empty gallery state
        emptyGalleryState.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("text-align", "center")
                .set("padding", "3rem 0");

        detailsArea.add(previewCard, emptyGalleryState);
        add(detailsScroller);
    }

    private HorizontalLayout createMetaRow(String labelText, Span valueSpan) {
        Span label = new Span(labelText);
        label.getStyle()
                .set("font-weight", "600")
                .set("width", "7rem")
                .set("min-width", "7rem")
                .set("color", "var(--lumo-secondary-text-color)");

        valueSpan.getStyle()
                .set("word-break", "break-all")
                .set("color", "var(--lumo-body-text-color)");

        HorizontalLayout row = new HorizontalLayout(label, valueSpan);
        row.setWidthFull();
        row.setSpacing(false);
        row.setPadding(false);
        return row;
    }

    private void refreshGallery() {
        sidebarList.removeAll();
        sidebarCardsList.clear();

        List<GalleryImage> images = imageService.getImagesByUser(currentUser);
        if (images.isEmpty()) {
            sidebarList.add(new Span("No images uploaded."));
            showEmptyPreviewState();
            return;
        }

        // Highlight selected image, fallback to first in list if invalid
        GalleryImage activeImage = images.stream()
                .filter(img -> img.getId() != null && img.getId().equals(selectedImageId))
                .findFirst()
                .orElse(images.get(0));

        selectedImageId = activeImage.getId();
        updateSelectedImagePreview(activeImage);

        // Render scrollable list of compact cards
        images.forEach(image -> {
            ImageCard card = new ImageCard(image, null, this::selectImage, false, true);
            card.setSelected(image.getId().equals(selectedImageId));
            sidebarCardsList.add(card);
            sidebarList.add(card);
        });
    }

    private void selectImage(GalleryImage image) {
        if (image != null && image.getId() != null) {
            selectedImageId = image.getId();
            
            // Instantly update highlights in the sidebar list without redrawing components
            sidebarCardsList.forEach(card -> {
                card.setSelected(card.getImage().getId().equals(selectedImageId));
            });

            updateSelectedImagePreview(image);
        }
    }

    private void updateSelectedImagePreview(GalleryImage image) {
        if (image == null) {
            showEmptyPreviewState();
            return;
        }

        previewCard.setVisible(true);
        emptyGalleryState.setVisible(false);

        largeImagePreview.setSrc("/image-content/" + image.getFilename());
        largeImagePreview.setAlt(image.getTitle());

        imageTitle.setText(image.getTitle());
        metaFilename.setText(image.getFilename());
        metaType.setText(image.getContentType());
        
        // Format uploaded date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        metaUploaded.setText(image.getUploadTime().format(formatter));

        // Dynamically compute exact file size from disk
        File file = new File(image.getFilepath());
        if (file.exists()) {
            metaSize.setText(formatFileSize(file.length()));
        } else {
            metaSize.setText("Unknown size");
        }

        downloadAnchor.setHref("/image-content/" + image.getFilename());
    }

    private void showEmptyPreviewState() {
        previewCard.setVisible(false);
        emptyGalleryState.setVisible(true);
    }

    private void openLightboxDialog(GalleryImage image) {
        Dialog lightbox = new Dialog();
        lightbox.setCloseOnEsc(true);
        lightbox.setCloseOnOutsideClick(true);
        lightbox.setWidth("90vw");
        lightbox.setHeight("90vh");

        Image fullImage = new Image("/image-content/" + image.getFilename(), image.getTitle());
        fullImage.setSizeFull();
        fullImage.getStyle().set("object-fit", "contain");

        lightbox.add(fullImage);
        lightbox.open();
    }

    private void confirmAndExecuteDeletion() {
        if (selectedImageId == null) return;

        imageService.getImagesByUser(currentUser).stream()
                .filter(img -> img.getId().equals(selectedImageId))
                .findFirst()
                .ifPresent(image -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.setHeaderTitle("Delete Image");
                    
                    VerticalLayout dialogBody = new VerticalLayout(new Span("Are you sure you want to permanently delete this image?"));
                    dialogBody.setPadding(true);
                    confirmDialog.add(dialogBody);

                    Button deleteConfirmBtn = new Button("Delete Permanently", e -> {
                        imageService.deleteImage(image);
                        selectedImageId = null;
                        refreshGallery();
                        confirmDialog.close();
                        
                        // Fire event on session-scoped event bus to update all layouts
                        ComponentUtil.fireEvent(UI.getCurrent(), new ImageChangedEvent(UI.getCurrent()));
                    });
                    deleteConfirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

                    Button cancelBtn = new Button("Cancel", e -> confirmDialog.close());
                    cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                    confirmDialog.getFooter().add(cancelBtn, deleteConfirmBtn);
                    confirmDialog.open();
                });
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
