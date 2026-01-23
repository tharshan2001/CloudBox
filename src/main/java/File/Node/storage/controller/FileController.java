package File.Node.storage.controller;

import File.Node.storage.dto.FileDTO;
import File.Node.storage.model.User;

import File.Node.storage.utils.File.FileManagementService;
import File.Node.storage.utils.File.FileStreamingService;
import File.Node.storage.utils.File.FileUploadService;
import File.Node.storage.utils.user.UserResolverService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class FileController {

    private final UserResolverService userResolverService;
    private final FileUploadService fileUploadService;
    private final FileStreamingService fileStreamingService;
    private final FileManagementService fileManagementService;

    public FileController(UserResolverService userResolverService,
                          FileUploadService fileUploadService,
                          FileStreamingService fileStreamingService,
                          FileManagementService fileManagementService) {
        this.userResolverService = userResolverService;
        this.fileUploadService = fileUploadService;
        this.fileStreamingService = fileStreamingService;
        this.fileManagementService = fileManagementService;
    }

    // =============================
    // UPLOAD FILES
    // =============================
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadMultiple(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String apiKey,
            Authentication auth) throws IOException, InterruptedException {

        if (files == null || files.length == 0)
            return ResponseEntity.badRequest().body(List.of("No files uploaded"));

        if (files.length > 10)
            return ResponseEntity.badRequest().body(List.of("Max 10 files allowed"));

        User user = userResolverService.resolveUser(auth, apiKey);

        // Convert MultipartFile to temporary File[]Ï
        File[] tempFiles = new File[files.length];
        String[] originalNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            tempFiles[i] = File.createTempFile("upload-", null);
            files[i].transferTo(tempFiles[i]);
            originalNames[i] = files[i].getOriginalFilename();
        }

        List<String> urls = fileUploadService.saveFiles(user, tempFiles, originalNames);

        for (File f : tempFiles) f.delete();

        return ResponseEntity.ok(urls);
    }

    // =============================
    // STREAM FILE
    // =============================
    @GetMapping("/meta/{fileKey}")
    public void streamFile(
            @PathVariable String fileKey,
            @RequestParam(required = false) Integer w,
            @RequestParam(required = false) Integer h,
            @RequestParam(required = false) Integer q,
            @RequestParam(required = false, defaultValue = "jpg") String format,
            HttpServletResponse response
    ) throws IOException {
        fileStreamingService.streamFile(fileKey, w, h, q, format, response);
    }



    // =============================
    // LIST FILES
    // =============================
    @GetMapping("/my-files")
    public List<FileDTO> listFiles(@RequestParam(required = false) String apiKey, Authentication auth) {
        User user = userResolverService.resolveUser(auth, apiKey);
        return fileManagementService.listFiles(user);
    }

    // =============================
    // DELETE FILE
    // =============================
    @DeleteMapping("/delete/{fileKey}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileKey,
                                             @RequestParam(required = false) String apiKey,
                                             Authentication auth) {

        User user = userResolverService.resolveUser(auth, apiKey);

        try {
            String result = fileManagementService.deleteFile(user, fileKey);
            return switch (result) {
                case "OK" -> ResponseEntity.ok("File deleted");
                case "UNAUTHORIZED" -> ResponseEntity.status(403).body("Unauthorized");
                default -> ResponseEntity.status(404).body("File not found");
            };
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Delete failed");
        }
    }
}
