# FileNode - Secure File Storage Backend

FileNode is a secure, self-hosted file storage backend built with **Spring Boot**. It provides JWT-authenticated file uploads, per-user file isolation, and secure file streaming for modern applications.

Designed to be used as a backend service, FileNode allows applications to store and manage user files safely without exposing the filesystem directly.

---

## 📋 Table of Contents
* [Features](#-features)
* [Tech Stack](#-tech-stack)
* [Setup](#-setup)
* [API Endpoints](#-api-endpoints)
* [File Storage Structure](#-file-storage-structure)
* [Security](#-security)
* [Future Improvements](#-future-improvements)
* [Notes](#-notes)
* [Contact](#-contact--support)

---

## ✨ Features
* **Multi-file Upload:** Upload multiple files (up to 10 per request).
* **Isolation:** Per-user file isolation ensuring data privacy.
* **Secure Naming:** Secure file storage with unique filenames to prevent collisions.
* **Database Metadata:** File metadata is stored efficiently in the database.
* **Streaming:** Securely stream or download files via API.
* **Safe Listing:** List user files using safe DTOs.
* **Ownership Validation:** Users can only delete or access files they own.
* **Security:** Robust JWT-based authentication.
* **Integration Ready:** REST-based architecture ready for frontend or mobile apps.

---

## 🛠 Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security (JWT)
* **Database:** Spring Data JPA with H2 / MySQL / PostgreSQL (Configurable)
* **Tools:** Lombok, Maven / Gradle
* **Architecture:** REST API

---

## ⚡ Setup

### 1. Clone the repository
```bash
git clone [https://github.com/tharshan2001/FileNode.git](https://github.com/tharshan2001/FileNode.git)
cd filenode

2. Build the project
   bash
   ./mvnw clean install
3. Run the application
   bash
   ./mvnw spring-boot:run
   ⚙️ Configuration
   Authentication
   POST /auth/register – Register a new user

POST /auth/login – Login and receive JWT token

File Operations
Upload Files
Endpoint: POST /upload

Form Data: files[] (max 10 files)

Response: List of file stream URLs

Stream / Download File
Endpoint: GET /meta/{fileKey}

Description: Streams the file (Can be protected with JWT)

List User Files
Endpoint: GET /my-files

Returns:

filename

fileKey

relative path

uploadedAt

Delete File
Endpoint: DELETE /delete/{fileKey}

Description: Deletes the file only if owned by the user

📁 File Storage Structure
text
/storage
├── 1                  # userId = 1
│   ├── 1674132123123_doc.pdf
│   └── 1674132123456_image.jpg
└── 2                  # userId = 2
└── 1674132136789_photo.png
✔ Files are stored outside the public directory

✔ Access is controlled through the API

✔ Filenames are unique to avoid collisions

🔐 Security
JWT-based authentication

Files are associated with a specific user

Only file owners can delete or access files

No public folder exposure

Streaming can be protected using JWT

Designed to be extended with:

Signed URLs

Rate limiting

File validation

🚀 Future Improvements
File size & type validation

Pagination for file listing

Shareable temporary links

Search & filtering

Logging and monitoring

Integration tests

Storage clustering (Buckets / Nodes)

S3 / MinIO support

📝 Notes
Maximum 10 files per upload

Files are stored with unique names

Metadata is required for secure file streaming

Designed for backend integration, not direct public hosting

📬 Contact / Support
For issues, suggestions, or improvements:

📧 Email: aptharshan@gmail.com

📂 File Handling & Streaming Services

This project includes robust file management utilities for uploading, converting, and streaming files.

1. File Streaming API

Streams files directly to clients, with optional resizing and format conversion for images.
Supports inline browser rendering (no forced downloads).

Endpoint:

GET /meta/{fileKey}


Query Parameters:

Parameter	Type	Description	Default
w	int	Desired image width in pixels	Original width
h	int	Desired image height in pixels	Original height
q	int	JPEG quality (0–100)	85
format	string	Output image format (jpg, png)	jpg

Example Usage:

GET /meta/abc123?w=400&h=300&q=80&format=jpg


Images are resized proportionally if only one dimension is provided.

Videos, audio, and documents are streamed as-is.

Caching headers are added for better performance:

ETag

Last-Modified

Cache-Control: public, max-age=31536000 (1 year)

2. File Upload Service

Handles user file uploads, stores files in user-specific directories, and automatically generates metadata for each file.

3. File Conversion Service

Converts files between different formats: image, audio, video, and documents.

Supports Web-optimized conversions to reduce file size and improve performance.

File converters now reside in utils/FileConvertor package:

AudioWebConverter

VideoWebConverter

ImageWebConverter

DocumentWebConverter

WebOptimizedConverter

WebOptimizedConverterFactory

4. File Management Service

Central service for managing file metadata, retrieval, deletion, and storage path resolution.

5. User Resolver Service

Resolves the currently logged-in user for file operations to ensure files are stored and retrieved in a user-specific context.

6. Browser & Postman Rendering

Images now render correctly both in browser and Postman.

Inline rendering is supported without forcing a file download.

PNG images work without issue; JPEG images now respect quality and format settings.

7. Package Structure Changes

File utilities are now organized under utils/File and utils/FileConvertor for better maintainability.

Improved separation of responsibilities:

Streaming → FileStreamingService

Upload → FileUploadService

Conversion → FileConversionService

Metadata/Management → FileManagementService