package File.Node.storage.utils.File;

import File.Node.storage.model.FileMetadata;
import File.Node.storage.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Service
public class FileStreamingService {

    private final FileStorageService storageService;
    private final FileMetadataService metadataService;

    public FileStreamingService(FileStorageService storageService,
                                FileMetadataService metadataService) {
        this.storageService = storageService;
        this.metadataService = metadataService;
    }

    /**
     * Streams a file with optional resizing and quality control.
     * URL example: /meta/{fileKey}?w=400&h=300&q=80&format=jpg
     */
    public void streamFile(String fileKey, Integer w, Integer h, Integer q, String format, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataService.getFileMetadata(fileKey);
        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] pathParts = meta.getRelativePath().split("/", 2);
        String userId = pathParts[0];
        String filename = pathParts[1];

        Path filePath = storageService.getFilePath(userId, filename);
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (q == null) q = 85;
        if (format == null) format = "jpg";

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        if (contentType.startsWith("image/")) {
            // Image resizing
            BufferedImage originalImage = ImageIO.read(filePath.toFile());
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            if (w != null && h == null) h = (w * originalHeight) / originalWidth;
            if (h != null && w == null) w = (h * originalWidth) / originalHeight;
            if (w == null) { w = originalWidth; h = originalHeight; }

            BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(originalImage, 0, 0, w, h, null);
            g.dispose();

            response.setContentType("image/" + format);
            response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
            addCacheHeaders(meta, response);

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(response.getOutputStream())) {
                ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                if ("jpg".equalsIgnoreCase(format) && param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(q / 100f);
                    param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
                }

                writer.write(null, new IIOImage(resizedImage, null, null), param);
                writer.dispose();
            }

        } else {
            // Non-image files → stream as-is
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
            addCacheHeaders(meta, response);
            Files.copy(filePath, response.getOutputStream());
            response.flushBuffer();
        }
    }

    private void addCacheHeaders(FileMetadata meta, HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 year
        response.setHeader("ETag", meta.getFileKey());
        response.setHeader("Last-Modified", meta.getUploadedAt().toString());
    }
}
