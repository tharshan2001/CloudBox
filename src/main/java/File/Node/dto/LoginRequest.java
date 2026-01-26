package File.Node.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    /**
     * User identifier: can be username or email
     */
    @NotBlank(message = "Identifier is required")
    private String identifier;

    /**
     * Password for login
     */
    @NotBlank(message = "Password is required")
    private String password;
}
