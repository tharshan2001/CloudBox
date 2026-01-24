package File.Node.controller;

import File.Node.dto.CubeDTO;
import File.Node.dto.FileDTO;
import File.Node.dto.OwnerDTO;
import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileStreamingService;
import File.Node.service.File.FileUploadService;
import File.Node.service.cube.CubeService;
import File.Node.utils.user.UserResolverService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/cubes")
public class CubeController {

    private final UserResolverService userResolverService;
    private final CubeService cubeService;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;
    private final FileStreamingService streamingService;

    public CubeController(UserResolverService userResolverService,
                          CubeService cubeService,
                          FileUploadService uploadService,
                          FileManagementService managementService,
                          FileStreamingService streamingService) {
        this.userResolverService = userResolverService;
        this.cubeService = cubeService;
        this.uploadService = uploadService;
        this.managementService = managementService;
        this.streamingService = streamingService;
    }

    // CREATE CUBE
    @PostMapping
    public ResponseEntity<CubeDTO> createCube(@RequestParam String name,
                                              @RequestParam(required = false) String description,
                                              Authentication auth) throws IOException {
        User user = userResolverService.resolveUser(auth, null);

        boolean exists = cubeService.listUserCubes(user).stream()
                .anyMatch(c -> c.getName().equals(name));
        if (exists) return ResponseEntity.badRequest().body(null);

        Cube cube = cubeService.createCube(name, description, user);

        // Create folder for cube: storage/userId/cubeId
        Path cubePath = Path.of("storage", "users", String.valueOf(user.getId()), cube.getId().toString());
        Files.createDirectories(cubePath);

        CubeDTO dto = cubeService.toDTO(cube);
        return ResponseEntity.ok(dto);
    }

    // LIST USER CUBES
    @GetMapping
    public List<CubeDTO> listCubes(Authentication auth) {
        User user = userResolverService.resolveUser(auth, null);
        return cubeService.listUserCubes(user).stream()
                .map(cubeService::toDTO)
                .toList();
    }

    // UPLOAD FILES TO CUBE
    @PostMapping("/{cubeId}/files")
    public ResponseEntity<List<String>> uploadFiles(@PathVariable Long cubeId,
                                                    @RequestParam("files") MultipartFile[] files,
                                                    Authentication auth) throws IOException {
        User user = userResolverService.resolveUser(auth, null);
        Cube cube = cubeService.getCubeEntity(cubeId, user);

        List<String> fileKeys = uploadService.saveFiles(cube, user, files);
        return ResponseEntity.ok(fileKeys);
    }

    // LIST FILES IN CUBE
    @GetMapping("/{cubeId}/files")
    public List<FileDTO> listFiles(@PathVariable Long cubeId, Authentication auth) {
        User user = userResolverService.resolveUser(auth, null);
        Cube cube = cubeService.getCubeEntity(cubeId, user);

        return managementService.listFiles(cube).stream()
                .map(f -> {
                    FileDTO dto = new FileDTO();
                    dto.setId(f.getId());
                    dto.setFilename(f.getFilename());
                    dto.setFileKey(f.getFileKey());
                    dto.setRelativePath(f.getRelativePath());
                    dto.setUploadedAt(f.getUploadedAt());
                    return dto;
                })
                .toList();
    }

    // STREAM FILE BY FILEKEY
    @GetMapping("/files/{fileKey}")
    public void streamFile(@PathVariable String fileKey, HttpServletResponse response) throws IOException {
        streamingService.streamFile(fileKey, response);
    }

    // DELETE FILE BY FILEKEY
    @DeleteMapping("/files/{fileKey}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileKey, Authentication auth) throws IOException {
        User user = userResolverService.resolveUser(auth, null);
        String result = managementService.deleteFile(user, fileKey);

        return switch (result) {
            case "OK" -> ResponseEntity.ok("File deleted");
            case "UNAUTHORIZED" -> ResponseEntity.status(403).body("Unauthorized");
            default -> ResponseEntity.status(404).body("File not found");
        };
    }
}
