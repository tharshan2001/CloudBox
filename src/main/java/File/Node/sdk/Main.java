package File.Node.sdk;

import File.Node.sdk.FileSdkPublic;
import File.Node.dto.ResponseWrapper;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        String apiBaseUrl = "http://localhost:8090/api/files";
        String apiKey = "2e7edef1-1edb-4f90-8253-0490bc4391bf";

        FileSdkPublic sdk = new FileSdkPublic(apiBaseUrl, apiKey);

        // Upload a file
        File file = new File("/Users/antonabitharshan/Desktop/x1.png");
        ResponseWrapper<String> uploadRes = sdk.uploadFile(file);
        System.out.println(uploadRes.getMessage() + " -> " + uploadRes.getData());

        // List files
        sdk.listFiles().forEach(f -> System.out.println(f.getFilename()));

        // Delete file
        ResponseWrapper<String> deleteRes = sdk.deleteFile(uploadRes.getData());
        System.out.println(deleteRes.getMessage());
    }
}

