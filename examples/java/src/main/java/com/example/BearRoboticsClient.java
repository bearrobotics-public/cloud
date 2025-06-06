package com.example;

import com.example.auth.BearAuthService;
import com.example.auth.JwtCredentials;
import com.example.streaming.StreamingClient;
import com.example.streaming.StreamingRpcMethod;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import bearrobotics.api.v1.services.cloud.APIServiceGrpc;

/**
 * Generic client for Bear Robotics Cloud API.
 * Provides authenticated gRPC connections and streaming capabilities.
 */
public class BearRoboticsClient {
    private static final Logger logger = Logger.getLogger(BearRoboticsClient.class.getName());
    private static final String CREDENTIALS_PATH = "src/main/resources/credentials.json";

    private final ManagedChannel channel;
    private final BearAuthService authService;
    private final JwtCredentials credentials;
    private final AtomicBoolean running = new AtomicBoolean(true);

    // Stub instances
    private final APIServiceGrpc.APIServiceStub asyncStub;
    private final APIServiceGrpc.APIServiceBlockingStub blockingStub;

    /**
     * Initialize the gRPC client with TLS and JWT authentication.
     *
     * @param host The host to connect to
     * @param port The port to connect to
     * @throws IOException If there's an error initializing authentication
     */
    public BearRoboticsClient(String host, int port) throws IOException {
        this(host, port, CREDENTIALS_PATH);
    }

    /**
     * Initialize the gRPC client with TLS and JWT authentication using custom credentials path.
     *
     * @param host The host to connect to
     * @param port The port to connect to
     * @param credentialsPath Path to the credentials JSON file
     * @throws IOException If there's an error initializing authentication
     */
    public BearRoboticsClient(String host, int port, String credentialsPath) throws IOException {
        // Initialize auth service
        this.authService = new BearAuthService(credentialsPath);

        // Create the JWT credentials
        this.credentials = new JwtCredentials(authService);

        // Build the channel with TLS and keep-alive options for long-running connections
        channel = ManagedChannelBuilder.forAddress(host, port)
                .useTransportSecurity() // Use TLS for secure connection
                .keepAliveTime(30, TimeUnit.SECONDS) // Send keepalive ping every 30 seconds
                .keepAliveTimeout(10, TimeUnit.SECONDS) // Wait 10 seconds for ping ack before considering connection dead
                .build();

        // Create the stubs with authentication
        asyncStub = APIServiceGrpc.newStub(channel).withCallCredentials(this.credentials);
        blockingStub = APIServiceGrpc.newBlockingStub(channel).withCallCredentials(this.credentials);

        logger.info("Bear Robotics gRPC client initialized with TLS and JWT authentication using v1 API");
    }

    /**
     * Get the async stub for making asynchronous calls.
     *
     * @return The async stub
     */
    public APIServiceGrpc.APIServiceStub getAsyncStub() {
        return asyncStub;
    }

    /**
     * Get the blocking stub for making synchronous calls.
     *
     * @return The blocking stub
     */
    public APIServiceGrpc.APIServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    /**
     * Get the JWT credentials for error handling.
     *
     * @return The JWT credentials
     */
    public JwtCredentials getCredentials() {
        return credentials;
    }

    /**
     * Create a streaming client for any streaming RPC.
     *
     * @param <TRequest> The request type
     * @param <TResponse> The response type
     * @return A StreamingClient builder
     */
    public <TRequest, TResponse> StreamingClient.Builder<TRequest, TResponse> createStreamingClient() {
        return new StreamingClient.Builder<TRequest, TResponse>().credentials(credentials);
    }

    /**
     * Create and start a streaming client with the provided configuration.
     *
     * @param rpcMethod The streaming RPC method to call
     * @param request The request to send
     * @param observer Stream observer for handling responses, errors, and completion
     * @param streamName Name of the stream for logging
     * @param <TRequest> The request type
     * @param <TResponse> The response type
     * @return The configured and started streaming client
     * @throws InterruptedException If interrupted while starting the stream
     */
    public <TRequest, TResponse> StreamingClient<TRequest, TResponse> startStream(
            StreamingRpcMethod<TRequest, TResponse> rpcMethod,
            TRequest request,
            StreamObserver<TResponse> observer,
            String streamName) throws InterruptedException {

        StreamingClient<TRequest, TResponse> client = new StreamingClient.Builder<TRequest, TResponse>()
                .rpcMethod(rpcMethod)
                .request(request)
                .observer(observer)
                .streamName(streamName)
                .credentials(credentials)
                .build();

        // Start streaming in a separate thread
        Thread streamThread = new Thread(() -> {
            try {
                client.startStreaming();
            } catch (InterruptedException e) {
                logger.info("Streaming thread interrupted for " + streamName);
                Thread.currentThread().interrupt();
            }
        });
        streamThread.setName(streamName + "-Thread");
        streamThread.start();

        return client;
    }

    /**
     * Check if the client is running.
     *
     * @return true if the client is running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Shutdown the client and clean up resources.
     *
     * @throws InterruptedException If interrupted while shutting down
     */
    public void shutdown() throws InterruptedException {
        // Signal running threads to stop
        running.set(false);

        // Shutdown auth service
        authService.shutdown();

        // Shutdown channel
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        logger.info("Bear Robotics gRPC client shutdown completed");
    }
}

