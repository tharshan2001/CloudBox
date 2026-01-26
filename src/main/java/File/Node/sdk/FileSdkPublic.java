package File.Node.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import File.Node.dto.FileDTO;
import File.Node.dto.ResponseWrapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

public class FileSdkPublic {

    private final String apiBaseUrl; // e.g., https://api.example.com/api/files
    private final String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public FileSdkPublic(String apiBaseUrl, String apiKey) {
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
    }

    // ============================
    // UPLOAD FILE
    // ============================
    public ResponseWrapper<String> uploadFile(File file) throws IOException {
        String boundary = "----SDKBoundary" + System.currentTimeMillis();

        URL url = new URL(apiBaseUrl + "/" + "?apikey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true)) {

            // File part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append("\r\n\r\n");
            writer.flush();

            Files.copy(file.toPath(), out);
            out.flush();
            writer.append("\r\n").flush();

            writer.append("--").append(boundary).append("--\r\n").flush();
        }

        int status = conn.getResponseCode();
        InputStream responseStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        ResponseWrapper<String> resp = mapper.readValue(responseStream, new TypeReference<>() {});
        conn.disconnect();
        return resp;
    }

    // ============================
    // LIST FILES
    // ============================
    public List<FileDTO> listFiles() throws IOException {
        URL url = new URL(apiBaseUrl + "/" + "?apikey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        InputStream responseStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

        List<FileDTO> files = mapper.readValue(responseStream, new TypeReference<>() {});
        conn.disconnect();
        return files;
    }

    // ============================
    // DELETE FILE
    // ============================
    public ResponseWrapper<String> deleteFile(String fileKey) throws IOException {
        URL url = new URL(apiBaseUrl + "/meta/" + fileKey + "?apikey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        int status = conn.getResponseCode();
        InputStream responseStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        ResponseWrapper<String> resp = mapper.readValue(responseStream, new TypeReference<>() {});
        conn.disconnect();
        return resp;
    }
}
