package File.Node.service.cube;

import File.Node.dto.CubeDTO;
import File.Node.dto.FileDTO;
import File.Node.dto.OwnerDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileUploadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CubeService {

    private final CubeRepository cubeRepository;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;

    public CubeService(CubeRepository cubeRepository,
                       FileUploadService uploadService,
                       FileManagementService managementService) {
        this.cubeRepository = cubeRepository;
        this.uploadService = uploadService;
        this.managementService = managementService;
    }

    // CREATE CUBE
    public Cube createCube(String name, String description, User owner) {
        Cube cube = new Cube();
        cube.setName(name);
        cube.setDescription(description);
        cube.setOwner(owner);

        cube.setApiKey(UUID.randomUUID().toString());
        cube.setApiSecret(UUID.randomUUID().toString());

        return cubeRepository.save(cube);
    }

    // LIST USER CUBES
    public List<Cube> listUserCubes(User owner) {
        return cubeRepository.findByOwner(owner);
    }

    // GET RAW ENTITY
    public Cube getCubeEntity(Long cubeId, User owner) {
        return cubeRepository.findById(cubeId)
                .filter(c -> c.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Cube not found or unauthorized"));
    }

    // FIND BY API KEY
    public Cube getCubeByApiKey(String apiKey) {
        return cubeRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Cube not found for API key"));
    }

    // CONVERT TO DTO
    public CubeDTO toDTO(Cube cube) {
        CubeDTO dto = new CubeDTO();
        dto.setId(cube.getId());
        dto.setName(cube.getName());
        dto.setDescription(cube.getDescription());
        dto.setApiKey(cube.getApiKey());
        dto.setApiSecret(cube.getApiSecret());

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
}
