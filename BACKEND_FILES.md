# Backend Files Overview

This document lists backend folders and files in the project and gives a concise explanation for each (purpose, important methods, notes).

**Project root**
- `src/main/java/com/example/notes/` — Java backend source root.
- `src/main/resources/application.properties` — runtime configuration (DB, upload dir, etc.).

**Configuration**
- **`config/ImageContentController.java`**: REST endpoint `GET /image-content/{filename}`. Safely resolves filenames in the configured uploads directory, probes file MIME type, returns `Resource` with proper `Content-Type` and caching headers. Protects against path traversal and handles missing/unreadable files.
- **`config/WebConfig.java`**: Adds a `ResourceHandler` for static `/uploads/**` (maps filesystem `uploads/` to `/uploads/` URL). Ensures upload directory exists on startup.

**Data / Entities**
- **`data/entity/User.java`**: JPA entity for application users (`id`, `username`, `passwordHash`). Persisted in `application_user` table.
- **`data/entity/GalleryImage.java`**: JPA entity representing uploaded images with fields: `filename` (generated, served to clients), `title`, `contentType`, `filepath` (absolute path on disk), `uploadTime`, and owning `User`.
- **`data/entity/Note.java`**: Simple JPA entity for textual notes (`content`, owning `User`).

**Repositories**
- **`data/repository/UserRepository.java`**: `JpaRepository<User,Long>` with `findByUsername(String)` used for authentication and user lookups.
- **`data/repository/GalleryImageRepository.java`**: `JpaRepository<GalleryImage,Long>` with `findByUser(User)` for listing user images.
- **`data/repository/NoteRepository.java`**: `JpaRepository<Note,Long>` with `findByUser(User)`.

**Security**
- **`security/SecurityConfig.java`**: Extends `VaadinWebSecurity`. Configures authentication and allowed endpoints (previously permitted `/uploads/**` and `/register`), sets the Vaadin `LoginView` and exposes a `PasswordEncoder` bean.
- **`security/UserDetailsServiceImpl.java`**: Loads `User` from DB and adapts to Spring Security `UserDetails` (username + password hash) for authentication.

**Services (business logic)**
- **`service/ImageService.java`**: Handles image uploads, persistence, and deletion. Key responsibilities:
  - Validate upload requests (type, size, presence of user and input stream).
  - Create uploads directory if missing.
  - Sanitize original filename and generate a UUID-based filename to store on disk.
  - Copy bytes to disk (configured `image.upload-dir`, default `uploads`).
  - Create and save `GalleryImage` entity with `filepath` and metadata.
  - Delete image file from disk and remove DB record on deletion.
  - Methods: `uploadImage(...)`, `deleteImage(...)`, `getImagesByUser(User)`, `updateImageTitle(...)`.
- **`service/UserService.java`**: Registers new users (checks uniqueness, encodes password using `PasswordEncoder`, saves `User`).
- **`service/NoteService.java`**: Thin CRUD wrapper for `Note` entities (get, save, delete).

**Vaadin Views and UI components (server-side)**
- **`views/MainLayout.java`**: Application shell — header, drawer, quick gallery thumbnails. Subscribes to `ImageChangedEvent` to refresh quick thumbnails. (Note: quick thumbnails currently used `/uploads/{filename}` — consider switching to `/image-content/{filename}` for consistency.)
- **`views/LoginView.java`**: Vaadin `LoginForm` view; integrates with Spring Security.
- **`views/RegistrationView.java`**: Registration form calling `UserService.registerUser(...)`.
- **`views/NotesView.java`**: Simple notes UI using `NoteService` to list/create/delete notes.

**Gallery UI (server-side Vaadin components)**
- **`views/gallery/GalleryView.java`**: Main gallery view. Responsibilities:
  - Lists images for the current user in a sidebar (uses `ImageCard`).
  - Shows large preview, metadata (filename, size, type, upload time) and actions (download, delete).
  - Uses `ImageService.getImagesByUser(...)` to fetch images.
  - Builds image URLs using `buildImageContentUrl(filename)` which points to `/image-content/{filename}`.
- **`views/gallery/components/ImageUploadDialog.java`**: Modal upload dialog using Vaadin `Upload` + `MemoryBuffer`. Enforces allowed types & max size, calls `ImageService.uploadImage(...)`, optionally updates title, fires `ImageChangedEvent` on success.
- **`views/gallery/components/ImageCard.java`**: Reusable thumbnail/card component used in grid and compact sidebar modes. Builds image `src` via `IMAGE_CONTENT_BASE_PATH` (`/image-content/`) and provides selection styling and optional delete action.
- **`views/gallery/components/ImageChangedEvent.java`**: Lightweight Vaadin event used to notify UI components to refresh when images are added or removed.

**Application bootstrap**
- **`NotesApplication.java`**: Standard Spring Boot `main()` bootstrap class.

**Additional notes & recommendations**
- The app stores uploaded files on disk under `uploads/` by default; that location can be changed with property `image.upload-dir`.
- For reliable image delivery the project now uses `ImageContentController` (`/image-content/{filename}`) instead of relying solely on `WebConfig` `/uploads/**` static mapping — this avoids VAADIN route interference and ensures correct `Content-Type` headers.
- Security: If you want anonymous access to served images, either ensure the security rules permit `/image-content/**` or annotate controller endpoints appropriately.
- Suggested follow-up: Replace remaining `"/uploads/"` usages (e.g., in `MainLayout`) with `/image-content/{filename}` for consistency.

---
Generated on: 2026-06-01
