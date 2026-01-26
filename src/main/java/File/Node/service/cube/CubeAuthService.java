package File.Node.service.cube;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.UserRepository;
import File.Node.security.CubeApiSecretUtil;
import org.springframework.stereotype.Service;

@Service
public class CubeAuthService {

    private final CubeService cubeService;
    private final CubeApiSecretUtil secretUtil;
    private final UserRepository userRepository;

    public CubeAuthService(CubeService cubeService,
                           CubeApiSecretUtil secretUtil,
                           UserRepository userRepository) {
        this.cubeService = cubeService;
        this.secretUtil = secretUtil;
        this.userRepository = userRepository;
    }

    /**
     * Authenticate SDK request using username, apiKey, and apiSecret.
     * Returns the Cube entity if authentication passes.
     */
    public Cube authenticate(String username, String apiKey, String apiSecret) {
        // 1️⃣ Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        // 2️⃣ Find cube by API key
        Cube cube = cubeService.getCubeByApiKey(apiKey);

        // 3️⃣ Verify cube belongs to user
        if (!cube.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Cube does not belong to user");
        }

        // 4️⃣ Validate provided secret against stored hashed secret
        if (!secretUtil.matches(apiSecret, cube.getApiSecret())) {
            throw new RuntimeException("Invalid API secret");
        }

        // ✅ Auth successful
        return cube;
    }
}
