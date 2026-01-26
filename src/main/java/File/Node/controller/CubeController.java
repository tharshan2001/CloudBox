package File.Node.controller;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.service.cube.CubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cubes")
public class CubeController {

    private final CubeService cubeService;

    public CubeController(CubeService cubeService) {
        this.cubeService = cubeService;
    }

    // ✅ CREATE CUBE
    @PostMapping
    public ResponseEntity<?> createCube(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                cubeService.createCube(name, description, user)
        );
    }

    // ✅ LIST CUBES
    @GetMapping
    public ResponseEntity<?> listCubes(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                cubeService.listUserCubes(user)
                        .stream()
                        .map(c -> cubeService.toDTO(c, null))
                        .toList()
        );
    }

    // ✅ REGENERATE SECRET (THIS WAS MISSING / WRONG)
    @PostMapping("/{cubeId}/secret")
    public ResponseEntity<String> regenerateSecret(
            @PathVariable Long cubeId,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        Cube cube = cubeService.getCubeEntity(cubeId, user);
        String newSecret = cubeService.regenerateSecret(cube);

        return ResponseEntity.ok(newSecret);
    }
}
