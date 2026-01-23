package File.Node.storage.utils.File;

import File.Node.storage.model.FileMetadata;
import File.Node.storage.repository.FileMetadataRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataService {

    private final FileMetadataRepository metadataRepository;

    public FileMetadataService(FileMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    /**
     * Fetches file metadata by fileKey with caching.
     */
    @Cacheable(value = "fileMetadataCache", key = "#fileKey")
    public FileMetadata getFileMetadata(String fileKey) {
        return metadataRepository.findByFileKey(fileKey).orElse(null);
    }
}
