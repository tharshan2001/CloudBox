package File.Node.service.File;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;
    private final VideoConversionService videoConversionService;

    public FileUploadService(
            FileStorageService storageService,
            FileMetadataRepository metadataRepository,
            VideoConversionService videoConversionService
    ) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
        this.videoConversionService = videoConversionService;
    }

    /**
     * Save a single uploaded file.
     * Returns fileKey.
     */
    public String saveFile(Cube cube, User user, MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "unknown";

        String fileKey = UUID.randomUUID().toString();
        String uniqueFilename = appendRandomSuffix(originalName);

        // Save file
        storageService.saveFile(file, user.getId(), cube.getId(), uniqueFilename);

        // Save metadata
        FileMetadata meta = new FileMetadata();
        meta.setFilename(originalName);
        meta.setRelativePath(user.getId() + "/" + cube.getId() + "/" + uniqueFilename);
        meta.setFileKey(fileKey);
        meta.setUploadedAt(LocalDateTime.now());
        meta.setUser(user);
        meta.setCube(cube);

        metadataRepository.save(meta);

        // Convert video if needed
        if (file.getContentType() != null &&
                file.getContentType().startsWith("video/")) {

            Path originalPath =
                    storageService.getFilePath(user.getId(), cube.getId(), uniqueFilename);

            Path webmPath =
                    storageService.getFilePath(user.getId(), cube.getId(), fileKey + ".webm");

            videoConversionService.convertVideoAsync(
                    originalPath.toFile(),
                    webmPath.toFile()
            );
        }

        return fileKey;
    }

    private String appendRandomSuffix(String originalName) {
        String baseName;
        String ext = "";

        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = originalName.substring(0, dotIndex);
            ext = originalName.substring(dotIndex);
        } else {
            baseName = originalName;
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return baseName + "_" + suffix + ext;
    }
}
