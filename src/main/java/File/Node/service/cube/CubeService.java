package File.Node.service.cube;

import File.Node.dto.CubeDTO;
import File.Node.dto.FileDTO;
import File.Node.dto.OwnerDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileUploadService;
import File.Node.security.CubeApiSecretUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CubeService {

    private final CubeRepository cubeRepository;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;
    private final CubeApiSecretUtil secretUtil;

    public CubeService(CubeRepository cubeRepository,
                       FileUploadService uploadService,
                       FileManagementService managementService,
                       CubeApiSecretUtil secretUtil) {
        this.cubeRepository = cubeRepository;
        this.uploadService = uploadService;
        this.managementService = managementService;
        this.secretUtil = secretUtil;
    }

    // CREATE CUBE, RETURN DTO WITH RAW SECRET
    public CubeDTO createCube(String name, String description, User owner) {
        Cube cube = new Cube();
        cube.setName(name);
        cube.setDescription(description);
        cube.setOwner(owner);
        cube.setApiKey(UUID.randomUUID().toString());

        // Generate raw secret
        String rawSecret = UUID.randomUUID().toString();

        // Store only hashed secret
        cube.setApiSecret(secretUtil.encode(rawSecret));

        cube = cubeRepository.save(cube);

        return toDTO(cube, rawSecret); // raw secret returned in DTO
    }

    // LIST USER CUBES
    public List<Cube> listUserCubes(User owner) {
        return cubeRepository.findByOwner(owner);
    }

    // GET CUBE ENTITY
    public Cube getCubeEntity(Long cubeId, User owner) {
        return cubeRepository.findById(cubeId)
                .filter(c -> c.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Cube not found or unauthorized"));
    }

    // FIND CUBE BY API KEY
    public Cube getCubeByApiKey(String apiKey) {
        return cubeRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Cube not found for API key"));
    }

    // REGENERATE SECRET AND RETURN RAW
    public String regenerateSecret(Cube cube) {
        String rawSecret = UUID.randomUUID().toString();
        cube.setApiSecret(secretUtil.encode(rawSecret));
        cubeRepository.save(cube);
        return rawSecret;
    }

    // CONVERT TO DTO
    public CubeDTO toDTO(Cube cube, String rawSecret) {
        CubeDTO dto = new CubeDTO();
        dto.setId(cube.getId());
        dto.setName(cube.getName());
        dto.setDescription(cube.getDescription());
        dto.setApiKey(cube.getApiKey());
        dto.setApiSecret(rawSecret); // only exposed at creation or regeneration

        OwnerDTO ownerDTO = new OwnerDTO();
        ownerDTO.setId(cube.getOwner().getId());
        ownerDTO.setName(cube.getOwner().getName());
        ownerDTO.setEmail(cube.getOwner().getEmail());
        dto.setOwner(ownerDTO);

        dto.setFiles(cube.getFiles().stream().map(f -> {
            FileDTO fDto = new FileDTO();
            fDto.setId(f.getId());
            fDto.setFilename(f.getFilename());
            fDto.setFileKey(f.getFileKey());
            fDto.setRelativePath(f.getRelativePath());
            fDto.setUploadedAt(f.getUploadedAt());
            return fDto;
        }).toList());

        return dto;
    }

    public CubeApiSecretUtil getSecretUtil() {
        return secretUtil;
    }
}
