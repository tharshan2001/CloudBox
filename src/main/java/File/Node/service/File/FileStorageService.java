package File.Node.service.File;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Save original uploaded file
     */
    void saveFile(MultipartFile file, Long userId, Long cubeId, String filename) throws IOException;

    /**
     * Get path to stored original file
     */
    Path getFilePath(Long userId, Long cubeId, String filename);

    /**
     * Delete a stored file
     */
    void deleteFile(Long userId, Long cubeId, String filename) throws IOException;

    /**
     * ✅ NEW: Get cached converted image path
     * Used for resized / optimized image streaming
     */
    Path getCachedImagePath(
            String fileKey,
            Integer width,
            Integer height,
            Integer quality,
            String format
    );
}
