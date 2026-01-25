package File.Node.service.File;

import File.Node.entity.FileMetadata;
import File.Node.utils.FileConvertor.WebOptimizedConverter;
import File.Node.utils.FileConvertor.WebOptimizedConverterFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class FileStreamingService {

    private final FileStorageService storageService;
    private final FileMetadataService metadataService;
    private final WebOptimizedConverterFactory converterFactory;

    public FileStreamingService(FileStorageService storageService,
                                FileMetadataService metadataService,
                                WebOptimizedConverterFactory converterFactory) {
        this.storageService = storageService;
        this.metadataService = metadataService;
        this.converterFactory = converterFactory;
    }

    /**
     * Stream files by fileKey with optional image parameters:
     * w, h, q, format
     */
    public void streamFile(String fileKey,
                           Integer width,
                           Integer height,
                           Integer quality,
                           String format,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        // 1️⃣ Metadata lookup
        FileMetadata meta = metadataService.getFileMetadata(fileKey);
        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Long userId = meta.getUser().getId();
        Long cubeId = meta.getCube().getId();
        String storedFilename = meta.getRelativePath().substring(meta.getRelativePath().lastIndexOf("/") + 1);
        Path originalFile = storageService.getFilePath(userId, cubeId, storedFilename);

        if (!Files.exists(originalFile)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(originalFile);
        if (mimeType == null) mimeType = "application/octet-stream";

        // 2️⃣ Handle images dynamically
        if (!mimeType.startsWith("video/")) {

            Optional<WebOptimizedConverter> converterOpt = converterFactory.getConverter(mimeType);
            if (converterOpt.isPresent()) {

                WebOptimizedConverter converter = converterOpt.get();
                String targetFormat = (format != null && !format.isEmpty())
                        ? format
                        : converter.getTargetExtension();

                // Convert only if width/height/quality/format is specified
                if ((width != null && width > 0) || (height != null && height > 0) ||
                        (quality != null) || (format != null)) {

                    File tmpOutput = File.createTempFile("stream-", "." + targetFormat);
                    try {
                        converter.convert(originalFile.toFile(), tmpOutput, width, height, quality);
                        response.setContentType("image/" + targetFormat);
                        response.setHeader("Content-Disposition",
                                "inline; filename=\"" + getFilenameWithFormat(meta.getFilename(), targetFormat) + "\"");
                        streamFile(tmpOutput.toPath(), response);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Image conversion interrupted", e);
                    } finally {
                        tmpOutput.delete();
                    }
                    return;
                }
            }

            // Stream original if no conversion requested
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
            streamFile(originalFile, response);
            return;
        }

        // 3️⃣ Video streaming with Range support
        Path webmPath = storageService.getFilePath(userId, cubeId, fileKey + ".webm");
        Path toStream = Files.exists(webmPath) ? webmPath : originalFile;

        response.setContentType(Files.probeContentType(toStream));
        response.setHeader("Accept-Ranges", "bytes");

        long fileLength = Files.size(toStream);
        String range = request.getHeader("Range");
        long start = 0;
        long end = fileLength - 1;

        if (range != null && range.startsWith("bytes=")) {
            String[] parts = range.replace("bytes=", "").split("-");
            try {
                start = Long.parseLong(parts[0]);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                }
            } catch (NumberFormatException ignored) {}
            if (start > end) start = 0;

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        }

        response.setHeader("Content-Length", String.valueOf(end - start + 1));

        try (RandomAccessFile raf = new RandomAccessFile(toStream.toFile(), "r");
             OutputStream out = response.getOutputStream()) {

            raf.seek(start);
            byte[] buffer = new byte[8192];
            long remaining = end - start + 1;
            int bytesRead;

            while (remaining > 0) {
                bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                out.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            out.flush();
        }
    }

    /** Buffered streaming utility */
    private void streamFile(Path path, HttpServletResponse response) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

    /** Utility for filenames with format */
    private String getFilenameWithFormat(String original, String format) {
        int dot = original.lastIndexOf('.');
        String name = (dot != -1) ? original.substring(0, dot) : original;
        return name + "." + format;
    }
}
