package File.Node.storage.utils.File;

import File.Node.storage.utils.FileConvertor.*;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileConversionService {

    public Path convertFile(File inputFile, Path targetPath) throws IOException, InterruptedException {
        WebOptimizedConverter converter = new ImageWebConverter("jpg", 0.95f); // 95% quality
        Files.createDirectories(targetPath.getParent());
        converter.convert(inputFile, targetPath.toFile());
        return targetPath;
    }
}
