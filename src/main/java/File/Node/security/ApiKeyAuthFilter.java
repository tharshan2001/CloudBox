package File.Node.security;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final CubeRepository cubeRepository;

    public ApiKeyAuthFilter(CubeRepository cubeRepository) {
        this.cubeRepository = cubeRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        // Only try auth if header is present and not already authenticated
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load cube + owner in one query to avoid LazyInitializationException
            Cube cube = cubeRepository.findByApiKeyWithOwner(apiKey).orElse(null);

            if (cube != null && cube.getOwner() != null) {
                User user = cube.getOwner();

                // Create an authentication token with the full User object
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.emptyList() // authorities, you can add roles if needed
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
