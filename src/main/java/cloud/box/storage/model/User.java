package cloud.box.storage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String apiKey;

    public User() {}
    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.apiKey = UUID.randomUUID().toString();
    }

    // getters & setters
}