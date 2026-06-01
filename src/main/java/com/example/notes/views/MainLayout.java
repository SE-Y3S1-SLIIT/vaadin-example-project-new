package com.example.notes.views;

import com.example.notes.data.entity.GalleryImage;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.gallery.GalleryView;
import com.example.notes.views.gallery.components.ImageChangedEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;
    private final ImageService imageService;
    private final UserRepository userRepository;

    private final VerticalLayout quickGalleryList = new VerticalLayout();

    public MainLayout(AuthenticationContext authContext, ImageService imageService, UserRepository userRepository) {
        this.authContext = authContext;
        this.imageService = imageService;
        this.userRepository = userRepository;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Image Upload Gallery");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        Button logout = new Button("Log out", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(true);
        drawerLayout.setSpacing(true);
        drawerLayout.getStyle().set("background", "var(--lumo-contrast-2pct)");

        // App Navigation Links
        RouterLink galleryLink = new RouterLink("Gallery", GalleryView.class);
        galleryLink.getStyle().set("font-weight", "600");

        RouterLink notesLink = new RouterLink("Notes", NotesView.class);
        notesLink.getStyle().set("font-weight", "600");

        drawerLayout.add(galleryLink, notesLink);

        // Divider
        Span divider = new Span();
        divider.setWidthFull();
        divider.getStyle()
                .set("height", "1px")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("margin", "0.5rem 0");
        drawerLayout.add(divider);

        // Quick Gallery Header
        H4 quickGalleryHeader = new H4("Quick Gallery");
        quickGalleryHeader.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em");
        drawerLayout.add(quickGalleryHeader);

        // Quick Gallery Grid of Miniature Rounded Thumbnails
        quickGalleryList.setWidthFull();
        quickGalleryList.setPadding(false);
        quickGalleryList.setSpacing(false);
        quickGalleryList.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("flex-direction", "row")
                .set("gap", "0.5rem")
                .set("align-content", "start");

        Scroller scroller = new Scroller(quickGalleryList);
        scroller.setSizeFull();
        scroller.getStyle().set("flex-grow", "1");

        drawerLayout.add(scroller);
        addToDrawer(drawerLayout);

        // Subscribe to session-scoped UI event bus to refresh drawer thumbnails dynamically
        ComponentUtil.addListener(UI.getCurrent(), ImageChangedEvent.class, event -> {
            refreshDrawerGallery();
        });

        refreshDrawerGallery();
    }

    private void refreshDrawerGallery() {
        quickGalleryList.removeAll();

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElse(null);

        if (username == null) {
            return;
        }

        userRepository.findByUsername(username).ifPresent(user -> {
            List<GalleryImage> images = imageService.getImagesByUser(user);
            if (images.isEmpty()) {
                Span emptyMsg = new Span("No uploads yet.");
                emptyMsg.getStyle()
                        .set("font-size", "var(--lumo-font-size-xs)")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("font-style", "italic");
                quickGalleryList.add(emptyMsg);
                return;
            }

            images.forEach(image -> {
                Image miniThumb = new Image("/uploads/" + image.getFilename(), image.getTitle());
                miniThumb.setWidth("3.25rem");
                miniThumb.setHeight("3.25rem");
                miniThumb.getStyle()
                        .set("object-fit", "cover")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("background", "var(--lumo-contrast-5pct)")
                        .set("cursor", "pointer")
                        .set("border", "1px solid var(--lumo-contrast-10pct)")
                        .set("transition", "transform 0.15s, border-color 0.15s, box-shadow 0.15s");

                // Premium Hover Micro-Animations
                miniThumb.getElement().addEventListener("mouseover", e -> {
                    miniThumb.getStyle()
                            .set("transform", "scale(1.08)")
                            .set("border-color", "var(--lumo-primary-color)")
                            .set("box-shadow", "var(--lumo-box-shadow-s)");
                });
                miniThumb.getElement().addEventListener("mouseout", e -> {
                    miniThumb.getStyle()
                            .set("transform", "scale(1.0)")
                            .set("border-color", "var(--lumo-contrast-10pct)")
                            .remove("box-shadow");
                });

                // Deep-link navigate to GalleryView with direct selection
                miniThumb.addClickListener(event -> {
                    UI.getCurrent().navigate(GalleryView.class,
                            new QueryParameters(Map.of("image", List.of(String.valueOf(image.getId()))))
                    );
                });

                quickGalleryList.add(miniThumb);
            });
        });
    }
}
