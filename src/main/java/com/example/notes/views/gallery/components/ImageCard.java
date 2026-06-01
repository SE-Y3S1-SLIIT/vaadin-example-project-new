package com.example.notes.views.gallery.components;

import com.example.notes.data.entity.GalleryImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Versatile image card supporting both grid-style and compact horizontal sidebar styles.
 * Store the compactContainer as a field so setSelected() can safely style it.
 */
public class ImageCard extends VerticalLayout {

    private final GalleryImage image;
    private final Consumer<GalleryImage> onDeleteCallback;
    private final Consumer<GalleryImage> onSelectCallback;
    private final boolean showFilename;
    private final boolean compact;

    // Stored reference to the inner container for compact mode, used by setSelected()
    private HorizontalLayout compactContainer;

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback) {
        this(image, onDeleteCallback, null, true, false);
    }

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback, Consumer<GalleryImage> onSelectCallback) {
        this(image, onDeleteCallback, onSelectCallback, true, false);
    }

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback, Consumer<GalleryImage> onSelectCallback, boolean showFilename) {
        this(image, onDeleteCallback, onSelectCallback, showFilename, false);
    }

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback, Consumer<GalleryImage> onSelectCallback, boolean showFilename, boolean compact) {
        this.image = Objects.requireNonNull(image, "image must not be null");
        this.onDeleteCallback = onDeleteCallback;
        this.onSelectCallback = onSelectCallback;
        this.showFilename = showFilename;
        this.compact = compact;

        if (compact) {
            buildCompactLayout();
        } else {
            buildCardLayout();
        }
    }

    // -------------------------------------------------------------------------
    // Grid-style card layout
    // -------------------------------------------------------------------------
    private void buildCardLayout() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        setMaxWidth("22rem");
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        if (onSelectCallback != null) {
            addClickListener(event -> onSelectCallback.accept(image));
            getStyle().set("cursor", "pointer");
        }

        getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("overflow", "hidden")
                .set("transition", "transform 0.2s, box-shadow 0.2s");

        Image preview = new Image(buildImageSrc(), buildAlt());
        preview.setWidthFull();
        preview.setHeight("14rem");
        preview.getStyle()
                .set("object-fit", "cover")
                .set("display", "block")
                .set("background", "var(--lumo-contrast-5pct)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.setWidthFull();
        content.setAlignItems(Alignment.STRETCH);
        content.getStyle().set("gap", "0.5rem");

        if (showFilename && image.getFilename() != null && !image.getFilename().isBlank()) {
            Span filename = new Span(image.getFilename());
            filename.getStyle()
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600")
                    .set("line-height", "1.3")
                    .set("word-break", "break-word");
            content.add(filename);
        }

        if (image.getTitle() != null && !image.getTitle().isBlank()) {
            Span title = new Span(image.getTitle());
            title.getStyle()
                    .set("font-size", "0.8rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("word-break", "break-word");
            content.add(title);
        }

        if (onDeleteCallback != null) {
            Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setWidthFull();
            deleteButton.addClickListener(event -> onDeleteCallback.accept(image));

            HorizontalLayout actions = new HorizontalLayout(deleteButton);
            actions.setPadding(false);
            actions.setSpacing(false);
            actions.setWidthFull();
            actions.setJustifyContentMode(JustifyContentMode.END);
            content.add(actions);
        }

        add(preview, content);
    }

    // -------------------------------------------------------------------------
    // Compact horizontal row layout (used in sidebar thumbnail list)
    // -------------------------------------------------------------------------
    private void buildCompactLayout() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        compactContainer = new HorizontalLayout();
        compactContainer.setWidthFull();
        compactContainer.setPadding(true);
        compactContainer.setSpacing(true);
        compactContainer.setAlignItems(Alignment.CENTER);
        compactContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out");

        if (onSelectCallback != null) {
            compactContainer.addClickListener(event -> onSelectCallback.accept(image));
        }

        Image preview = new Image(buildImageSrc(), buildAlt());
        preview.setWidth("3.5rem");
        preview.setHeight("3.5rem");
        preview.getStyle()
                .set("min-width", "3.5rem")
                .set("object-fit", "cover")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("background", "var(--lumo-contrast-5pct)");

        VerticalLayout textInfo = new VerticalLayout();
        textInfo.setPadding(false);
        textInfo.setSpacing(false);
        textInfo.getStyle()
                .set("overflow", "hidden")
                .set("flex-grow", "1");

        String displayTitle = (image.getTitle() != null && !image.getTitle().isBlank())
                ? image.getTitle()
                : image.getFilename();

        Span titleSpan = new Span(displayTitle);
        titleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        Span timeText = new Span(formatUploadTime(image.getUploadTime()));
        timeText.getStyle()
                .set("font-size", "var(--lumo-font-size-xxs)")
                .set("color", "var(--lumo-secondary-text-color)");

        textInfo.add(titleSpan, timeText);
        compactContainer.add(preview, textInfo);
        add(compactContainer);
    }

    // -------------------------------------------------------------------------
    // Selection highlight — uses stored compactContainer field (no stream needed)
    // -------------------------------------------------------------------------
    public void setSelected(boolean selected) {
        if (compact && compactContainer != null) {
            if (selected) {
                compactContainer.getStyle()
                        .set("border-color", "var(--lumo-primary-color)")
                        .set("background", "var(--lumo-primary-color-10pct)")
                        .set("box-shadow", "0 0 0 2px var(--lumo-primary-color)");
            } else {
                compactContainer.getStyle()
                        .set("border-color", "var(--lumo-contrast-10pct)")
                        .set("background", "var(--lumo-base-color)")
                        .remove("box-shadow");
            }
        } else {
            if (selected) {
                getStyle()
                        .set("border-color", "var(--lumo-primary-color)")
                        .set("box-shadow", "0 0 0 2px var(--lumo-primary-color)");
            } else {
                getStyle()
                        .set("border-color", "var(--lumo-contrast-10pct)")
                        .remove("box-shadow");
            }
        }
    }

    public GalleryImage getImage() {
        return image;
    }

    private String buildAlt() {
        return (image.getTitle() != null && !image.getTitle().isBlank())
                ? image.getTitle()
                : image.getFilename();
    }

    private String formatUploadTime(java.time.LocalDateTime uploadTime) {
        if (uploadTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        return uploadTime.format(formatter);
    }

    private String buildImageSrc() {
        String filename = image.getFilename();
        if (filename == null || filename.isBlank()) {
            return "";
        }
        return "/image-content/" + filename;
    }
}
