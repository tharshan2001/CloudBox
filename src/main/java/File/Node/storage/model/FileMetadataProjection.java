package File.Node.storage.model;

import java.time.LocalDateTime;

public interface FileMetadataProjection {
    String getFilename();
    String getRelativePath();
    String getFileKey();
    LocalDateTime getUploadedAt();
}

