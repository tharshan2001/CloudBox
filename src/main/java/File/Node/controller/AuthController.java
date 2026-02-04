package File.Node.controller;

import File.Node.dto.cube.ApiResponse;
import File.Node.dto.auth.LoginRequest;
import File.Node.dto.auth.RegisterRequest;
import File.Node.entity.User;
import File.Node.repository.UserRepository;
import File.Node.security.utils.CurrentUser;
import File.Node.security.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import File.Node.dto.auth.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse("Email already exists"));
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse("Username already taken"));
        }

        User user = new User(request.getUsername(), request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {

        String identifier = request.getIdentifier(); // username or email

        User user = userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier)).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(new ApiResponse("Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getEmail());

        Cookie cookie = new Cookie("FILENODE_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true if HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(new ApiResponse("Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("FILENODE_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(new ApiResponse("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@CurrentUser User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponse userResponse = new UserResponse(user.getUsername(), user.getName(), user.getEmail());

        return ResponseEntity.ok(userResponse);
    }


}
