package com.compass.service;

import com.compass.model.Complaint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RemoteReportingClient {
    private final HttpClient httpClient;
    private final URI endpoint;
    private final int maxRetries;

    public RemoteReportingClient(String endpointUrl) {
        this(endpointUrl, 3);
    }

    public RemoteReportingClient(String endpointUrl, int maxRetries) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.endpoint = URI.create(endpointUrl);
        this.maxRetries = Math.max(1, maxRetries);
    }

    public String reportComplaint(Complaint complaint) throws IOException, InterruptedException {
        String payload = buildJsonPayload(complaint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return sendWithRetry(request);
    }

    private String buildJsonPayload(Complaint complaint) {
        return "{" +
                "\"complaintId\":" + complaint.getId() + "," +
                "\"title\":\"" + escapeJson(complaint.getTitle()) + "\"," +
                "\"description\":\"" + escapeJson(complaint.getDescription()) + "\"," +
                "\"studentId\":" + complaint.getStudentId() + "," +
                "\"departmentId\":" + (complaint.getDepartmentId() == null ? 0 : complaint.getDepartmentId()) + "," +
                "\"status\":\"" + escapeJson(complaint.getStatus()) + "\"" +
                "}";
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        int attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }
            } catch (IOException | InterruptedException exception) {
                if (attempt >= maxRetries) {
                    throw exception;
                }
            }
            Thread.sleep(1000L * attempt);
        }
        throw new IOException("Failed to report complaint after " + maxRetries + " attempts.");
    }
}
