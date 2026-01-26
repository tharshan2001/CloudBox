package File.Node.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CubeApiSecretUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Encode a plain API secret (store this hash somewhere, e.g., DB or config)
    public String encode(String rawSecret) {
        return encoder.encode(rawSecret);
    }

    // Validate a provided API secret against stored hash
    public boolean matches(String rawSecret, String encodedSecret) {
        return encoder.matches(rawSecret, encodedSecret);
    }
}
