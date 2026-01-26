package File.Node.controller;

import File.Node.dto.FileDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.security.CurrentUser;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileStreamingService;
import File.Node.service.File.FileUploadService;
import File.Node.service.cube.CubeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final CubeService cubeService;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;
    private final FileStreamingService streamingService;

    public FileController(
            CubeService cubeService,
            FileUploadService uploadService,
            FileManagementService managementService,
            FileStreamingService streamingService
    ) {
        this.cubeService = cubeService;
        this.uploadService = uploadService;
        this.managementService = managementService;
        this.streamingService = streamingService;
    }

    // ============================
    // UPLOAD SINGLE FILE BY CUBE NAME OR API KEY
    // ============================
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(
            @RequestParam(required = false) String cubeName,
            @RequestParam(required = false) String apiKey,
            @RequestPart("file") MultipartFile file,
            @CurrentUser User user
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is missing");
        }

        Cube cube;

        if (apiKey != null && !apiKey.isEmpty()) {
            cube = cubeService.getCubeByApiKeyForUser(apiKey, user);
        } else if (cubeName != null && !cubeName.isEmpty()) {
            cube = cubeService.getCubeByNameForUser(cubeName, user);
        } else {
            return ResponseEntity.badRequest().body("Either cubeName or apiKey must be provided");
        }

        // Save file using cube ID internally to avoid conflicts
        String fileKey = uploadService.saveFile(cube, user, file);

        return ResponseEntity.ok(fileKey);
    }

    // ============================
    // LIST FILES BY CUBE NAME OR API KEY
    // ============================
    @GetMapping("/list")
    public ResponseEntity<List<FileDTO>> listFiles(
            @RequestParam(required = false) String cubeName,
            @RequestParam(required = false) String apiKey,
            @CurrentUser User user
    ) {
        Cube cube;

        if (apiKey != null && !apiKey.isEmpty()) {
            cube = cubeService.getCubeByApiKeyForUser(apiKey, user);
        } else if (cubeName != null && !cubeName.isEmpty()) {
            cube = cubeService.getCubeByNameForUser(cubeName, user);
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<FileDTO> files = managementService.listFiles(cube)
                .stream()
                .map(f -> new FileDTO(
                        f.getId(),
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .toList();

        return ResponseEntity.ok(files);
    }

    // ============================
    // STREAM FILE
    // ============================
    @GetMapping("/meta/{fileKey}")
    public void streamFile(
            @PathVariable String fileKey,
            @RequestParam(required = false) Integer w,
            @RequestParam(required = false) Integer h,
            @RequestParam(required = false) Integer q,
            @RequestParam(required = false) String format,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        streamingService.streamFile(fileKey, w, h, q, format, request, response);
    }

    // ============================
    // DELETE FILE
    // ============================
    @DeleteMapping("/meta/{fileKey}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileKey,
            @CurrentUser User user
    ) throws IOException {
        String result = managementService.deleteFile(user, fileKey);

        return switch (result) {
            case "OK" -> ResponseEntity.ok("File deleted");
            case "UNAUTHORIZED" -> ResponseEntity.status(403).body("Unauthorized");
            default -> ResponseEntity.status(404).body("File not found");
        };
    }
}
