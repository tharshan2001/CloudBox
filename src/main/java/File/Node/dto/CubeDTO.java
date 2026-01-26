package File.Node.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CubeDTO {
    private String name;
    private String description;
    private String apiKey;
    private String apiSecret;

}
