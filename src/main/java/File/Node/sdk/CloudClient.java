package File.Node.sdk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CloudClient {

    private final String username;
    private final String apiKey;
    private final String rawSecret;
    private final String baseUrl; // Add base URL

    public CloudClient(String username, String apiKey, String rawSecret) {
        this.username = username;
        this.apiKey = apiKey;
        this.rawSecret = rawSecret;
        this.baseUrl = "http://localhost:8080/api"; // must match Spring Boot controller
    }

    // Upload multiple files
    public List<String> upload(Long cubeId, File... files) throws IOException {
        List<String> uploadedFileKeys = new ArrayList<>();

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        URL url = new URL(baseUrl + "/files/" + cubeId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-USERNAME", username);
        conn.setRequestProperty("X-API-KEY", apiKey);
        conn.setRequestProperty("X-API-SECRET", rawSecret);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {

            for (File file : files) {
                if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());

                // File part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"")
                        .append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n");
                writer.flush();

                // File bytes
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
                writer.append("\r\n");
                writer.flush();
            }

            // End boundary
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();

        if (is == null) throw new IOException("No response from server (HTTP " + status + ")");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);

            if (status >= 400) {
                System.err.println("Upload failed: HTTP " + status + " -> " + response);
                throw new IOException("Upload failed with status " + status);
            }

            // Simple parse: assume JSON array of strings: ["key1","key2"]
            String resp = response.toString().trim();
            if (resp.startsWith("[") && resp.endsWith("]")) {
                resp = resp.substring(1, resp.length() - 1); // remove brackets
                for (String key : resp.split(",")) {
                    uploadedFileKeys.add(key.trim().replaceAll("^\"|\"$", "")); // remove quotes
                }
            } else {
                uploadedFileKeys.add(resp); // fallback
            }
        }

        return uploadedFileKeys;
    }
}
