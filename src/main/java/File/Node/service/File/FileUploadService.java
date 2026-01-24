package File.Node.service.File;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;

    public FileUploadService(FileStorageService storageService,
                             FileMetadataRepository metadataRepository) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
    }

    public List<String> saveFiles(Cube cube, User user, MultipartFile[] files) throws IOException {
        List<String> fileKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null) originalName = "unknown";

            String ext = "";
            int dotIndex = originalName.lastIndexOf(".");
            if (dotIndex != -1) ext = originalName.substring(dotIndex);

            String fileKey = UUID.randomUUID().toString();
            String filename = fileKey + ext;

            // Save file under storage/userId/cubeId/filename
            storageService.saveFile(file, user.getId(), cube.getId(), filename);

            // Save metadata
            FileMetadata meta = new FileMetadata();
            meta.setFilename(originalName);
            meta.setRelativePath(user.getId() + "/" + cube.getId() + "/" + filename);
            meta.setFileKey(fileKey);
            meta.setUploadedAt(LocalDateTime.now());
            meta.setUser(user);
            meta.setCube(cube);

            metadataRepository.save(meta);
            fileKeys.add(fileKey);
        }

        return fileKeys;
    }
}
