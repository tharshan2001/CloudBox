package File.Node.service.File;

import File.Node.entity.FileMetadata;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileStreamingService {

    private final FileStorageService storageService;
    private final FileMetadataService metadataService;

    public FileStreamingService(FileStorageService storageService,
                                FileMetadataService metadataService) {
        this.storageService = storageService;
        this.metadataService = metadataService;
    }

    public void streamFile(String fileKey, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataService.getFileMetadata(fileKey);
        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path filePath = storageService.getFilePath(
                meta.getUser().getId(),
                meta.getCube().getId(),
                fileKey + getExtension(meta.getFilename())
        );

        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex) : "";
    }
}
