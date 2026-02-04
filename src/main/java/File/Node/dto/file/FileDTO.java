package File.Node.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {

    private Long id;
    private String filename;
    private String relativePath;
    private String fileKey;
    private LocalDateTime uploadedAt;
}
