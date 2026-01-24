package File.Node.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileDTO {
    private Long id;               // file id
    private String filename;
    private String relativePath;
    private String fileKey;
    private LocalDateTime uploadedAt;

    // Default constructor
    public FileDTO() {
    }
}
