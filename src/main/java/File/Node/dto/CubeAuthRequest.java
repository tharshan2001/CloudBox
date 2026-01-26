package File.Node.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CubeAuthRequest {
    private String username;   // user owning the cube
    private String apiKey;     // cube API key
    private String apiSecret;  // encoded API secret (Base64)
}
