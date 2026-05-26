package com.campus.service;

import com.campus.model.Complaint;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class RemoteReportingClient {

    private final HttpClient httpClient;
    private final String endpointUrl;
    private final int maxRetries;
    private final int timeoutSeconds;

    public RemoteReportingClient(String endpointUrl) {
        this(endpointUrl, 3, 30);
    }

    public RemoteReportingClient(String endpointUrl, int maxRetries, int timeoutSeconds) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        this.endpointUrl = endpointUrl;
        this.maxRetries = maxRetries;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String reportComplaint(Complaint complaint) throws IOException, InterruptedException {
        String payload = buildJsonPayload(complaint);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return sendWithRetry(request);
    }

    public CompletableFuture<String> reportComplaintAsync(Complaint complaint) {
        String payload = buildJsonPayload(complaint);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Async reporting failed: " + ex.getMessage());
                    return null;
                });
    }

    private String buildJsonPayload(Complaint complaint) {
        JSONObject json = new JSONObject();
        json.put("complaintId", complaint.getId());
        json.put("title", escapeJson(complaint.getTitle()));
        json.put("description", escapeJson(complaint.getDescription()));
        json.put("studentId", complaint.getStudentId());
        json.put("departmentId", complaint.getDepartmentId() != null ? complaint.getDepartmentId() : 0);
        json.put("status", complaint.getStatus());
        json.put("priority", complaint.getPriority());
        json.put("timestamp", complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : "");
        json.put("source", "Campus Complaint System");
        
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private String sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                } else if (attempt == maxRetries) {
                    throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                lastException = e;
                if (attempt == maxRetries) {
                    throw e;
                }
            }
            
            // Exponential backoff
            long delayMs = 1000L * (long) Math.pow(2, attempt - 1);
            Thread.sleep(delayMs);
        }
        
        throw new IOException("Failed after " + maxRetries + " attempts", lastException);
    }

    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }
}