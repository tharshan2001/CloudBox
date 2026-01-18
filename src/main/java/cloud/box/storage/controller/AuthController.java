package cloud.box.storage.controller;

import cloud.box.storage.model.User;
import cloud.box.storage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password){
        if(userRepository.findByUsername(username).isPresent())
            return ResponseEntity.badRequest().body("Username exists");
        User user = new User(username, password);
        userRepository.save(user);
        return ResponseEntity.ok("Registered! API Key: " + user.getApiKey());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password){
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .map(u -> ResponseEntity.ok("Login successful! API Key: " + u.getApiKey()))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
}