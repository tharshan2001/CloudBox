package File.Node.storage.utils.File;

import File.Node.storage.model.FileMetadata;
import File.Node.storage.model.User;
import File.Node.storage.repository.FileMetadataRepository;
import File.Node.storage.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;
    private final FileConversionService conversionService;

    public FileUploadService(FileStorageService storageService,
                             FileMetadataRepository metadataRepository,
                             FileConversionService conversionService) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
        this.conversionService = conversionService;
    }

    public List<String> saveFiles(User user, File[] tempFiles, String[] originalNames) throws IOException, InterruptedException {
        List<String> urls = new ArrayList<>();

        for (int i = 0; i < tempFiles.length; i++) {
            String originalName = originalNames[i];
            String baseFilename = System.currentTimeMillis() + "_" + originalName;
            String convertedFilename = baseFilename + ".jpg";

            Path convertedPath = storageService.getFilePath(String.valueOf(user.getId()), convertedFilename);
            while (Files.exists(convertedPath)) {
                convertedFilename = System.currentTimeMillis() + "_" + originalName + ".jpg";
                convertedPath = storageService.getFilePath(String.valueOf(user.getId()), convertedFilename);
            }

            conversionService.convertFile(tempFiles[i], convertedPath);

            // save metadata
            String fileKey = UUID.randomUUID().toString();
            FileMetadata meta = new FileMetadata(originalName,
                    user.getId() + "/" + convertedFilename,
                    fileKey,
                    LocalDateTime.now(),
                    user
            );
            metadataRepository.save(meta);

            // generate URL
            String url = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/meta/")
                    .path(fileKey)
                    .toUriString();
            urls.add(url);
        }

        return urls;
    }
}
