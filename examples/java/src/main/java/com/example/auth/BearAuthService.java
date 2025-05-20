package com.example.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling authentication with the Bear Robotics API.
 * This includes reading credentials from a file and obtaining JWT tokens.
 */
public class BearAuthService {
    private static final Logger logger = Logger.getLogger(BearAuthService.class.getName());
    private static final String AUTH_URL = "https://api-auth.bearrobotics.ai/authorizeApiAccess";
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Token refresh scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Token validity period in milliseconds (1 hour by default)
    private static final long TOKEN_VALIDITY_PERIOD = 60 * 60 * 1000; // 1 hour
    
    // How often to refresh the token (30 minutes)
    private static final long TOKEN_REFRESH_INTERVAL = 30 * 60 * 1000; // 30 minutes
    
    private String apiKey;
    private String secret;
    private String scope;
    private String jwtToken;
    private long tokenExpirationTime;
    
    // Token listeners to notify when token is refreshed
    private TokenRefreshListener tokenListener;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    
    /**
     * Interface for components that need to be notified when the token is refreshed.
     */
    public interface TokenRefreshListener {
        void onTokenRefreshed(String newToken);
    }
    
    /**
     * Initializes the auth service by loading credentials from a file.
     * 
     * @param credentialsFilePath Path to the credentials JSON file
     * @throws IOException If there's an error reading the credentials file
     */
    public BearAuthService(String credentialsFilePath) throws IOException {
        loadCredentials(credentialsFilePath);
        // Get initial token
        fetchNewToken();
        // Start token refresh scheduler
        startTokenRefreshScheduler();
    }
    
    /**
     * Sets a listener to be notified when the token is refreshed.
     * 
     * @param listener The listener to notify
     */
    public void setTokenRefreshListener(TokenRefreshListener listener) {
        this.tokenListener = listener;
    }
    
    /**
     * Loads API credentials from the specified file path.
     * 
     * @param filePath Path to the credentials JSON file
     * @throws IOException If there's an error reading the file
     */
    private void loadCredentials(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Credentials file not found: " + filePath);
        }
        
        try (InputStream is = Files.newInputStream(path)) {
            JsonNode credentials = objectMapper.readTree(is);
            this.apiKey = credentials.get("api_key").asText();
            this.secret = credentials.get("secret").asText();
            this.scope = credentials.get("scope").asText();
            
            logger.info("Credentials loaded successfully");
        }
    }
    
    /**
     * Gets a valid JWT token for API authentication.
     * If the token is expired or not yet obtained, a new one will be fetched.
     * 
     * @return The JWT token
     * @throws IOException If there's an error fetching the token
     */
    public synchronized String getJwtToken() throws IOException {
        // Check if we need to fetch a new token
        if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            fetchNewToken();
        }
        
        return jwtToken;
    }
    
    /**
     * Fetches a new JWT token from the authentication API.
     * 
     * @throws IOException If there's an error in the API call
     */
    private synchronized void fetchNewToken() throws IOException {
        // Create request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("api_key", apiKey);
        requestBody.put("secret", secret);
        requestBody.put("scope", scope);
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        
        // Build the request
        RequestBody body = RequestBody.create(requestBodyJson, JSON);
        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(body)
                .build();
        
        logger.info("Fetching new JWT token...");
        
        // Execute the request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to obtain JWT token: " + 
                        response.code() + " " + response.message());
            }
            
            if (response.body() == null) {
                throw new IOException("Empty response received from auth API");
            }
            
            // The response body is a plain string containing the JWT token
            String newToken = response.body().string();
            
            // Update token and expiration time
            this.jwtToken = newToken;
            // Set expiration time to current time + token validity period
            this.tokenExpirationTime = System.currentTimeMillis() + TOKEN_VALIDITY_PERIOD;
            
            logger.info("JWT token obtained successfully, valid for 1 hour. Will refresh in 30 minutes.");
            
            // Notify listener if registered
            if (tokenListener != null) {
                tokenListener.onTokenRefreshed(newToken);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching JWT token", e);
            throw new IOException("Failed to obtain JWT token", e);
        }
    }
    
    /**
     * Starts a background scheduler to automatically refresh the token every 30 minutes.
     */
    private void startTokenRefreshScheduler() {
        // Schedule token refresh at a fixed rate of 30 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Scheduled token refresh triggered (every 30 minutes)");
                fetchNewToken();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Scheduled token refresh failed", e);
            }
        }, TOKEN_REFRESH_INTERVAL, TOKEN_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
        
        logger.info("Token refresh scheduler started - will refresh token every 30 minutes");
    }
    
    /**
     * Shuts down the token refresh scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Token refresh scheduler stopped");
    }
}