package File.Node.controller;

import File.Node.dto.CreateCubeRequest;
import File.Node.dto.CubeDTO;
import File.Node.dto.CubeInfoDTO;
import File.Node.entity.User;
import File.Node.security.CurrentUser;
import File.Node.service.cube.CubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cubes")
public class CubeController {

    private final CubeService cubeService;

    public CubeController(CubeService cubeService) {
        this.cubeService = cubeService;
    }

    // ============================
    // CREATE CUBE
    // ============================
    @PostMapping
    public ResponseEntity<CubeDTO> createCube(
            @RequestBody CreateCubeRequest request,
            @CurrentUser User user
    ) {
        return ResponseEntity.ok(
                cubeService.createCube(request.getName(), request.getDescription(), user)
        );
    }

    // ============================
    // LIST ALL USER CUBES
    // ============================
    @GetMapping
    public ResponseEntity<List<CubeInfoDTO>> listUserCubes(@CurrentUser User user) {
        List<CubeInfoDTO> cubes = cubeService.listUserCubesInfo(user);
        return ResponseEntity.ok(cubes);
    }

    // ============================
    // GET CUBE INFO BY NAME
    // ============================
    @GetMapping("/{cubeName}")
    public ResponseEntity<CubeInfoDTO> getCubeInfo(
            @PathVariable String cubeName,
            @CurrentUser User user
    ) {
        CubeInfoDTO cubeInfo = cubeService.getCubeInfoByNameForUser(cubeName, user);
        return ResponseEntity.ok(cubeInfo);
    }
}
