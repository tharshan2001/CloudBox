package File.Node.dto;

public class CubeInfoDTO {
    private String name;
    private String description;
    private String apiKey;

    public CubeInfoDTO() {}

    public CubeInfoDTO(String name, String description, String apiKey) {
        this.name = name;
        this.description = description;
        this.apiKey = apiKey;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
