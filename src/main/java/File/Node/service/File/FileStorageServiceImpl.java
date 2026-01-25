package File.Node.service.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storagePath;

    public FileStorageServiceImpl(@Value("${storage.location}") String storageLocation) throws IOException {
        this.storagePath = Paths.get(storageLocation);
        Files.createDirectories(storagePath);
    }

    @Override
    public void saveFile(MultipartFile file, Long userId, Long cubeId, String filename) throws IOException {
        Path folder = storagePath.resolve(userId.toString()).resolve(cubeId.toString());
        Files.createDirectories(folder);
        Files.copy(file.getInputStream(), folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Path getFilePath(Long userId, Long cubeId, String filename) {
        return storagePath.resolve(userId.toString())
                .resolve(cubeId.toString())
                .resolve(filename);
    }

    @Override
    public void deleteFile(Long userId, Long cubeId, String filename) throws IOException {
        Files.deleteIfExists(getFilePath(userId, cubeId, filename));
    }

    public Path getCachedImagePath(
            String fileKey,
            Integer width,
            Integer height,
            Integer quality,
            String format
    ) {
        String cacheDir = "cache/images";
        String fileName = fileKey +
                "_w" + (width != null ? width : "auto") +
                "_h" + (height != null ? height : "auto") +
                "_q" + (quality != null ? quality : "auto") +
                "." + format;

        Path path = Path.of(cacheDir, fileName);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ignored) {}

        return path;
    }

}
