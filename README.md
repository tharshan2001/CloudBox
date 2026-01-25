# MiniStreamBox

**MiniStreamBox** – Lightweight media handling & streaming system.

A self-hosted Java/Spring Boot application for uploading, storing, streaming, and converting media files with per-user isolation and flexible image/video optimization. Ideal for experimentation, prototyping, or small-scale personal use.

---

## 📋 Table of Contents
- [✨ Features](#✨-features)
- [🛠 Tech Stack](#🛠-tech-stack)
- [⚡ Setup](#⚡-setup)
- [⚙️ Configuration](#⚙️-configuration)
- [📂 File Storage Structure](#📂-file-storage-structure)
- [🔐 Security](#🔐-security)
- [🚀 Future Improvements](#🚀-future-improvements)
- [📝 Notes](#📝-notes)
- [📬 Contact](#📬-contact)

---

## ✨ Features
- **Multi-file Upload**: Upload multiple files (up to 10 per request).
- **Per-user Isolation**: Ensures each user’s files are private.
- **Secure Naming**: Unique filenames to prevent collisions.
- **Database Metadata**: Efficiently stored metadata including fileKey, original filename, upload date.
- **File Streaming**: Stream images, videos, and documents via API.
- **Dynamic Image Optimization**: Resize, convert format, adjust JPEG quality on-the-fly.
- **Caching**: Converted images are cached for faster retrieval.
- **Video Streaming**: HTTP Range support for smooth playback.
- **Ownership Validation**: Only file owners can access or delete files.
- **REST API Ready**: Easily integrate with frontend or mobile apps.

---

## 🛠 Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: Spring Data JPA (H2 / MySQL / PostgreSQL)
- **Security**: Spring Security with JWT
- **Tools**: Lombok, Maven / Gradle
- **Architecture**: REST API

---

## ⚡ Setup
 git clone https://github.com/tharshan2001/FileNode.git
 - cd filenode
 - ./mvnw clean install
 - ./mvnw spring-boot:run
 
## ⚙️Configuration
### Authentication
- **POST** `/auth/register` – Register a new user
- **POST** `/auth/login` – Login and receive JWT token

### File Operations
#### Upload
- **Endpoint**: `POST /api/files/{cubeId}`
- **Form Data**: `files[]` (max 10 files)
- **Response**: List of fileKeys

#### Stream / Download
- **Endpoint**: `GET /meta/{fileKey}?w=&h=&q=&format=`

**Query Parameters:**

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `w` | int | Desired image width in pixels | Original width |
| `h` | int | Desired image height in pixels | Original height |
| `q` | int | JPEG quality (0–100) | 85 |
| `format` | string | Output image format (jpg, png) | jpg |

#### Delete 
- **Endpoint**: `DELETE /meta/{fileKey}`
- **Description**: Deletes file if owned by the requesting user

## 📂 File Storage Structure


**Key Points:**
- Files stored outside public directory
- Unique filenames prevent collisions
- Access controlled via API

## 🔐 Security

- **JWT-based authentication** - Secure token-based authentication system
- **Files tied to specific users** - Each file is associated with its owner
- **Only owners can access/delete files** - Strict ownership validation
- **No public folder exposure** - Files are not directly accessible from the web
- **Supports optional signed URLs and rate-limiting for future improvements** - Scalable security features
## 🚀 Future Improvements

- **File size/type validation** - Enhanced upload validation
- **Pagination and filtering for file listing** - Better file management
- **Shareable temporary links** - Secure file sharing capabilities
- **Logging and monitoring** - Comprehensive system observability
- **Integration tests** - Improved test coverage
- **Storage clustering (Buckets / Nodes)** - Distributed storage architecture
- **Optional S3/Cloudinary integration for cloud storage** - Cloud storage compatibility

## 📝 Notes

- **Maximum 10 files per upload** - Upload batch limitation
- **Files stored with unique names** - Prevents filename collisions
- **Metadata required for secure streaming** - Database-driven access control
- **Designed for backend integration; not for direct public hosting** - API-first architecture

## 📬 Contact

For issues, suggestions, or improvements: 
📧 **Email**: aptharshan@gmail.com