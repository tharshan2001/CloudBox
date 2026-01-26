package File.Node.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String username;   // unique login name
    private String name;       // display name
    private String email;
    private String password;
}
