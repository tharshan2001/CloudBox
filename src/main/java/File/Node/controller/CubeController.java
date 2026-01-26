package File.Node.controller;

import File.Node.dto.*;
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

    // CREATE CUBE
    @PostMapping
    public ResponseEntity<ResponseWrapper<CubeDTO>> createCube(
            @RequestBody CreateCubeRequest request,
            @CurrentUser User user
    ) {
        try {
            CubeDTO cube = cubeService.createCube(request.getName(), request.getDescription(), user);
            return ResponseEntity.ok(
                    new ResponseWrapper<CubeDTO>(true, "Cube created successfully", cube)
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    new ResponseWrapper<CubeDTO>(false, ex.getMessage(), null)
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(
                    new ResponseWrapper<CubeDTO>(false, "An unexpected error occurred", null)
            );
        }
    }


    // LIST ALL USER CUBES
    @GetMapping
    public ResponseEntity<List<CubeInfoDTO>> listUserCubes(@CurrentUser User user) {
        List<CubeInfoDTO> cubes = cubeService.listUserCubesInfo(user);
        return ResponseEntity.ok(cubes);
    }

    // GET CUBE INFO BY NAME
    @GetMapping("/{cubeName}")
    public ResponseEntity<CubeInfoDTO> getCubeInfo(
            @PathVariable String cubeName,
            @CurrentUser User user
    ) {
        CubeInfoDTO cubeInfo = cubeService.getCubeInfoByNameForUser(cubeName, user);
        return ResponseEntity.ok(cubeInfo);
    }



    // ============================
// GET RAW API SECRET
// ============================
    @GetMapping("/{cubeName}/secret")
    public ResponseEntity<ResponseWrapper<String>> getCubeApiSecret(
            @PathVariable String cubeName,
            @CurrentUser User user
    ) {
        try {
            // fetch cube for user
            var cube = cubeService.getCubeByNameForUser(cubeName, user);

            // regenerate and return raw secret
            String rawSecret = cubeService.regenerateSecret(cube);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "API secret retrieved successfully", rawSecret)
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404)
                    .body(new ResponseWrapper<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(new ResponseWrapper<>(false, "Unexpected error occurred", null));
        }
    }

}
