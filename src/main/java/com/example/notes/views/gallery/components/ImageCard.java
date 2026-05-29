package com.example.notes.views.gallery.components;

import com.example.notes.data.entity.GalleryImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Objects;
import java.util.function.Consumer;

public class ImageCard extends VerticalLayout {

    private final GalleryImage image;
    private final Consumer<GalleryImage> onDeleteCallback;
    private final boolean showFilename;

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback) {
        this(image, onDeleteCallback, true);
    }

    public ImageCard(GalleryImage image, Consumer<GalleryImage> onDeleteCallback, boolean showFilename) {
        this.image = Objects.requireNonNull(image, "image must not be null");
        this.onDeleteCallback = onDeleteCallback;
        this.showFilename = showFilename;

        buildCardLayout();
    }

    private void buildCardLayout() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        setMaxWidth("22rem");
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("overflow", "hidden");

        Image preview = new Image(buildImageSrc(), image.getTitle() != null ? image.getTitle() : image.getFilename());
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
            content.add(actions);
        }

        add(preview, content);
    }

    private String buildImageSrc() {
        String filename = image.getFilename();
        if (filename == null || filename.isBlank()) {
            return "";
        }

        return "/uploads/" + filename;
    }
}
