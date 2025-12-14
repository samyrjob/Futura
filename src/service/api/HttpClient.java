package service.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Generic HTTP Client - Reusable for ANY API
 */
public class HttpClient {
    
    private static final int TIMEOUT = 10000;
    private static final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create();
    
    private String baseUrl;
    private String username;
    
    public HttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GENERIC HTTP METHODS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * GET request - returns parsed object
     */
    public <T> T get(String path, Class<T> responseType) {
        try {
            String response = sendRequest("GET", path, null);
            return gson.fromJson(response, responseType);
        } catch (Exception e) {
            System.err.println("[HTTP] GET " + path + " failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * GET request - returns list
     */
    public <T> java.util.List<T> getList(String path, Class<T[]> arrayType) {
        try {
            String response = sendRequest("GET", path, null);
            T[] array = gson.fromJson(response, arrayType);
            return array != null ? java.util.Arrays.asList(array) : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println("[HTTP] GET " + path + " failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * POST request - returns parsed object
     */
    public <T> T post(String path, Object body, Class<T> responseType) {
        try {
            String jsonBody = gson.toJson(body);
            String response = sendRequest("POST", path, jsonBody);
            return gson.fromJson(response, responseType);
        } catch (Exception e) {
            System.err.println("[HTTP] POST " + path + " failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * POST request - returns boolean success
     */
    public boolean postSuccess(String path, Object body) {
        try {
            String jsonBody = gson.toJson(body);
            String response = sendRequest("POST", path, jsonBody);
            ApiResponse result = gson.fromJson(response, ApiResponse.class);
            return result != null && result.success;
        } catch (Exception e) {
            System.err.println("[HTTP] POST " + path + " failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * DELETE request
     */
    public boolean delete(String path) {
        try {
            sendRequest("DELETE", path, null);
            return true;
        } catch (Exception e) {
            System.err.println("[HTTP] DELETE " + path + " failed: " + e.getMessage());
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CORE HTTP LOGIC (single place!)
    // ═══════════════════════════════════════════════════════════
    
    private String sendRequest(String method, String path, String body) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // Configure
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Username", username != null ? username : "");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        
        // Send body if present
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        // Read response
        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            return readStream(conn.getInputStream());
        } else {
            String error = readStream(conn.getErrorStream());
            throw new IOException("HTTP " + code + ": " + error);
        }
    }
    
    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
    
    // Simple response wrapper
    private static class ApiResponse {
        boolean success;
        String message;
    }
}