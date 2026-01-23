package File.Node.storage.utils.File;

import File.Node.storage.dto.FileDTO;
import File.Node.storage.model.FileMetadata;
import File.Node.storage.model.User;
import File.Node.storage.repository.FileMetadataRepository;
import File.Node.storage.service.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileManagementService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;

    public FileManagementService(FileStorageService storageService,
                                 FileMetadataRepository metadataRepository) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
    }

    public List<FileDTO> listFiles(User user) {
        return metadataRepository.findByUser(user).stream()
                .map(f -> new FileDTO(
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .collect(Collectors.toList());
    }

    public String deleteFile(User user, String fileKey) throws IOException {
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);

        if (meta == null)
            return "NOT_FOUND";

        if (!meta.getUser().getId().equals(user.getId()))
            return "UNAUTHORIZED";

        String[] pathParts = meta.getRelativePath().split("/", 2);
        storageService.deleteFile(pathParts[0], pathParts[1]);
        metadataRepository.delete(meta);

        return "OK";
    }
}

