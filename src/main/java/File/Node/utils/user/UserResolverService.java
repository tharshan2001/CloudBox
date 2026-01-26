package File.Node.utils.user;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserResolverService {

    private final CubeRepository cubeRepository;

    public UserResolverService(CubeRepository cubeRepository) {
        this.cubeRepository = cubeRepository;
    }

    /**
     * Resolves a User entity based on either JWT authentication or Cube API key.
     *
     * @param auth   the Spring Security Authentication object (from JWT)
     * @param cubeApiKey optional API key for cube access
     * @return User entity owning the cube or authenticated user
     * @throws RuntimeException if no valid user is found
     */
    public User resolveUser(Authentication auth, String cubeApiKey) {

        // 1️⃣ If Cube API key is provided, find cube and return its owner
        if (cubeApiKey != null && !cubeApiKey.isBlank()) {
            Cube cube = cubeRepository.findByApiKey(cubeApiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid Cube API key"));
            return cube.getOwner();
        }

        // 2️⃣ Otherwise, resolve via authenticated user
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            // Assuming login via username OR email
            throw new RuntimeException("Authentication-based user resolution not implemented yet. Use cube API key instead.");
        }

        // 3️⃣ Neither worked
        throw new RuntimeException("Authentication or Cube API key required");
    }
}
