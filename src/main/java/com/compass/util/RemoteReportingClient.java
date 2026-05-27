package com.compass.util;

import com.compass.model.Complaint;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for remote complaint reporting via HttpURLConnection.
 */
public class RemoteReportingClient {
    private static final Logger logger = LoggerFactory.getLogger(RemoteReportingClient.class);
    private final Gson gson = new Gson();

    public boolean reportComplaint(Complaint complaint) {
        if (!AppConfig.getBoolean("remote.report.enabled", false)) {
            logger.debug("Remote reporting disabled");
            return true;
        }
        String url = AppConfig.get("remote.report.url", "");
        if (url.isBlank()) {
            return true;
        }
        int timeout = AppConfig.getInt("remote.report.timeout.seconds", 30) * 1000;
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String payload = gson.toJson(complaint);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            logger.info("Remote report response: {}", code);
            return code >= 200 && code < 300;
        } catch (Exception e) {
            logger.warn("Remote reporting failed (non-fatal): {}", e.getMessage());
            return false;
        }
    }
}
