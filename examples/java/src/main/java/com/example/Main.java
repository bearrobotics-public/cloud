package com.example;

import com.example.auth.BearAuthService;
import com.example.auth.JwtCredentials;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

// Import the generated gRPC classes for v0 API
import bearrobotics.api.v0.cloud.CloudAPIServiceGrpc;
import bearrobotics.api.v0.cloud.CloudApiService.SubscribeMissionStatusRequest;
import bearrobotics.api.v0.cloud.CloudApiService.SubscribeMissionStatusResponse;
import bearrobotics.api.v0.robot.MissionOuterClass.MissionState;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String CREDENTIALS_PATH = "src/main/resources/credentials.json";

    // Set of status codes that should trigger a retry
    private static final Set<Status.Code> RETRYABLE_STATUS_CODES = EnumSet.of(
        Status.Code.UNAVAILABLE,    // Server is temporarily unavailable
        Status.Code.INTERNAL,       // Server encountered an internal error
        Status.Code.DEADLINE_EXCEEDED, // Request deadline exceeded
        Status.Code.UNAUTHENTICATED // Authentication failure - handled separately
    );

    private final ManagedChannel channel;
    private final CloudAPIServiceGrpc.CloudAPIServiceStub asyncStub;
    private final BearAuthService authService;
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Initialize the gRPC client with TLS and JWT authentication.
     *
     * @param host The host to connect to
     * @param port The port to connect to
     * @throws IOException If there's an error initializing authentication
     */
    public Main(String host, int port) throws IOException {
        // Initialize auth service
        this.authService = new BearAuthService(CREDENTIALS_PATH);

        // Create the JWT credentials
        JwtCredentials credentials = new JwtCredentials(authService);

        // Build the channel with TLS and keep-alive options for long-running connections
        channel = ManagedChannelBuilder.forAddress(host, port)
                .useTransportSecurity() // Use TLS for secure connection
                .keepAliveTime(30, TimeUnit.SECONDS) // Send keepalive ping every 30 seconds
                .keepAliveTimeout(10, TimeUnit.SECONDS) // Wait 10 seconds for ping ack before considering connection dead
                .build();

        // Create the async stub with authentication
        asyncStub = CloudAPIServiceGrpc.newStub(channel)
                .withCallCredentials(credentials);

        logger.info("gRPC client initialized with TLS and JWT authentication using v0 API");
    }

    public void shutdown() throws InterruptedException {
        // Signal running threads to stop
        running.set(false);

        // Shutdown auth service
        authService.shutdown();

        // Shutdown channel
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        logger.info("gRPC client shutdown completed");
    }

    /**
     * Subscribe to mission status updates using server-side streaming RPC.
     * This method demonstrates how to handle a streaming response from the server.
     * It also includes selective auto-reconnection for specific error codes.
     * This subscription runs indefinitely until the application is shut down.
     *
     * @param robotId The ID of the robot to subscribe to
     */
    public void subscribeMissionStatus(String robotId) throws InterruptedException {
        logger.info("Subscribing to mission status for robot: " + robotId + " using v0 API (indefinite subscription)");

        // Create a countdown latch that will only be triggered on shutdown or error
        final CountDownLatch terminationLatch = new CountDownLatch(1);

        // Create a stream observer with selective reconnection logic
        subscribeMissionStatusWithReconnect(robotId, new StreamObserver<SubscribeMissionStatusResponse>() {
            @Override
            public void onNext(SubscribeMissionStatusResponse response) {
                MissionState missionState = response.getMissionState();
                logger.info("Received mission status update for robot: " + robotId);
                logger.info("Message: " + missionState);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                Status.Code code = status.getCode();

                logger.log(Level.SEVERE, "CAUSE: {0}", t.getCause());
                logger.log(Level.WARNING, "Mission status subscription failed: {0} (Code: {1})",
                        new Object[]{t.getMessage(), code});

                // Check if we should retry based on the status code
                boolean shouldRetry = running.get() && RETRYABLE_STATUS_CODES.contains(code);

                if (shouldRetry) {
                    // Special handling for authentication errors
                    if (code == Status.Code.UNAUTHENTICATED) {
                        logger.warning("Authentication error detected. Token may have expired and will be refreshed.");
                    }

                    logger.info("Error is retryable (Status code: " + code + "). Will attempt to reconnect...");
                } else {
                    logger.info("Error is NOT retryable (Status code: " + code + "). Will not reconnect.");
                    terminationLatch.countDown();
                }
            }

            @Override
            public void onCompleted() {
                logger.info("Mission status subscription completed by server");

                // Only count down if we're shutting down
                if (!running.get()) {
                    terminationLatch.countDown();
                } else {
                    // We don't automatically reconnect when the server completes the stream normally
                    // Only specific errors will trigger reconnection
                    logger.info("Server gracefully completed the stream. Subscription ended.");
                    terminationLatch.countDown();
                }
            }
        });

        // Wait indefinitely for the application to be terminated
        logger.info("Subscription is active and will run indefinitely until application shutdown or non-retryable error");
        terminationLatch.await();
    }

    /**
     * Subscribes to mission status with selective reconnection logic.
     * This only reconnects for specific status codes.
     *
     * @param robotId The robot ID to subscribe to
     * @param observer The observer to receive events
     */
    private void subscribeMissionStatusWithReconnect(String robotId, StreamObserver<SubscribeMissionStatusResponse> observer) {
        // Create a wrapper observer that handles reconnection
        StreamObserver<SubscribeMissionStatusResponse> reconnectingObserver = new StreamObserver<SubscribeMissionStatusResponse>() {
            @Override
            public void onNext(SubscribeMissionStatusResponse response) {
                observer.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // Extract the gRPC status code
                Status status = Status.fromThrowable(t);
                Status.Code code = status.getCode();

                // Forward the error to the original observer
                observer.onError(t);

                // Attempt to reconnect only for specific status codes and if we're still running
                if (running.get() && RETRYABLE_STATUS_CODES.contains(code)) {
                    try {
                        int delaySeconds = 5;
                        logger.info("Reconnecting after error (" + code + ") in " + delaySeconds + " seconds...");
                        Thread.sleep(delaySeconds * 1000); // Wait before reconnecting

                        // Subscribe again if we're still running
                        if (running.get()) {
                            logger.info("Resubscribing to mission status for robot: " + robotId);
                            subscribeMissionStatusWithReconnect(robotId, observer);
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Reconnection interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logger.info("Not reconnecting for status code: " + code);
                }
            }

            @Override
            public void onCompleted() {
                // Forward the completion event
                observer.onCompleted();

                // We don't reconnect on normal completion
                logger.info("Stream completed normally by server, not reconnecting");
            }
        };

        // Build the request with robot ID
        SubscribeMissionStatusRequest request = SubscribeMissionStatusRequest.newBuilder()
                .setRobotId(robotId)
                .build();

        // Make the call with the reconnecting observer
        asyncStub.subscribeMissionStatus(request, reconnectingObserver);
    }

    public static void main(String[] args) {
        // Default values
        String host = "api-beta.bearrobotics.ai";
        int port = 443;
        String robotId = "robot-1";

        // Adjust based on command line arguments if provided
        if (args.length >= 1) robotId = args[0];

        Main client = null;
        try {
            client = new Main(host, port);

            // Register shutdown hook to clean up resources
            final Main finalClient = client;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutdown hook triggered, shutting down client...");
                    finalClient.shutdown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

            logger.info("Starting indefinite mission status subscription for robot: " + robotId);
            logger.info("Will only reconnect on status codes: UNAVAILABLE, INTERNAL, DEADLINE_EXCEEDED, UNAUTHENTICATED");

            // Subscribe to mission status updates indefinitely
            client.subscribeMissionStatus(robotId);

            // This line will only be reached if the subscription terminates
            logger.info("Mission status subscription ended");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing authentication", e);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Client interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            // Ensure we clean up resources
            if (client != null) {
                try {
                    client.shutdown();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Shutdown interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
