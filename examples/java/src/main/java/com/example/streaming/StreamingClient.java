package com.example.streaming;

import com.example.auth.JwtCredentials;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic streaming client for gRPC server-side streaming RPCs.
 * Handles authentication, reconnection logic, and error handling for any streaming RPC.
 *
 * @param <TRequest> The request type for the streaming RPC
 * @param <TResponse> The response type for the streaming RPC
 */
public class StreamingClient<TRequest, TResponse> {
    private static final Logger logger = Logger.getLogger(StreamingClient.class.getName());

    // Set of status codes that should trigger a retry
    private static final Set<Status.Code> RETRYABLE_STATUS_CODES = EnumSet.of(
        Status.Code.UNAVAILABLE,        // Server is temporarily unavailable
        Status.Code.INTERNAL,           // Server encountered an internal error
        Status.Code.DEADLINE_EXCEEDED,  // Request deadline exceeded
        Status.Code.UNAUTHENTICATED     // Authentication failure - handled separately
    );

    private final StreamingRpcMethod<TRequest, TResponse> rpcMethod;
    private final TRequest request;
    private final StreamObserver<TResponse> observer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final String streamName;
    private final int reconnectDelaySeconds;
    private final JwtCredentials credentials;

    /**
     * Creates a new streaming client.
     *
     * @param rpcMethod The streaming RPC method to call
     * @param request The request to send
     * @param observer Stream observer for handling responses, errors, and completion
     * @param streamName Name of the stream for logging purposes
     * @param credentials JWT credentials for authentication error handling
     */
    public StreamingClient(StreamingRpcMethod<TRequest, TResponse> rpcMethod,
                          TRequest request,
                          StreamObserver<TResponse> observer,
                          String streamName,
                          JwtCredentials credentials) {
        this(rpcMethod, request, observer, streamName, 5, credentials);
    }

    /**
     * Creates a new streaming client with custom reconnect delay.
     *
     * @param rpcMethod The streaming RPC method to call
     * @param request The request to send
     * @param observer Stream observer for handling responses, errors, and completion
     * @param streamName Name of the stream for logging purposes
     * @param reconnectDelaySeconds Delay before reconnecting after an error
     * @param credentials JWT credentials for authentication error handling
     */
    public StreamingClient(StreamingRpcMethod<TRequest, TResponse> rpcMethod,
                          TRequest request,
                          StreamObserver<TResponse> observer,
                          String streamName,
                          int reconnectDelaySeconds,
                          JwtCredentials credentials) {
        this.rpcMethod = rpcMethod;
        this.request = request;
        this.observer = observer;
        this.streamName = streamName;
        this.reconnectDelaySeconds = reconnectDelaySeconds;
        this.credentials = credentials;
    }

    /**
     * Starts the streaming subscription.
     * This method runs indefinitely until the application is shut down or a non-retryable error occurs.
     *
     * @throws InterruptedException If the thread is interrupted while waiting
     */
    public void startStreaming() throws InterruptedException {
        logger.info("Starting " + streamName + " streaming (indefinite subscription)");

        // Create a countdown latch that will only be triggered on shutdown or non-retryable error
        final CountDownLatch terminationLatch = new CountDownLatch(1);

        // Create the stream observer with reconnection logic
        startStreamingWithReconnect(new StreamObserver<TResponse>() {
            @Override
            public void onNext(TResponse response) {
                try {
                    observer.onNext(response);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in response callback for " + streamName, e);
                }
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                Status.Code code = status.getCode();

                logger.log(Level.SEVERE, "CAUSE: {0}", t.getCause());
                logger.log(Level.WARNING, streamName + " streaming failed: {0} (Code: {1})",
                        new Object[]{t.getMessage(), code});

                // Call user's error callback
                try {
                    observer.onError(t);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in error callback for " + streamName, e);
                }

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
                logger.info(streamName + " streaming completed by server");

                // Call user's completion callback
                try {
                    observer.onCompleted();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in completion callback for " + streamName, e);
                }

                // Only count down if we're shutting down
                if (!running.get()) {
                    terminationLatch.countDown();
                } else {
                    // We don't automatically reconnect when the server completes the stream normally
                    logger.info("Server gracefully completed the stream. Subscription ended.");
                    terminationLatch.countDown();
                }
            }
        });

        // Wait indefinitely for the application to be terminated
        logger.info(streamName + " subscription is active and will run indefinitely until application shutdown or non-retryable error");
        logger.info("Will only reconnect on status codes: UNAVAILABLE, INTERNAL, DEADLINE_EXCEEDED, UNAUTHENTICATED");
        terminationLatch.await();
    }

    /**
     * Stops the streaming subscription.
     */
    public void stop() {
        logger.info("Stopping " + streamName + " streaming...");
        running.set(false);
    }

    /**
     * Starts streaming with selective reconnection logic.
     * This only reconnects for specific status codes.
     *
     * @param observer The observer to receive events
     */
    private void startStreamingWithReconnect(StreamObserver<TResponse> observer) {
        // Create a wrapper observer that handles reconnection
        StreamObserver<TResponse> reconnectingObserver = new StreamObserver<TResponse>() {
            @Override
            public void onNext(TResponse response) {
                observer.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // Extract the gRPC status code
                Status status = Status.fromThrowable(t);
                Status.Code code = status.getCode();

                // Handle authentication errors specially
                if (code == Status.Code.UNAUTHENTICATED) {
                    logger.info("Authentication error detected, forcing token refresh");
                    if (credentials != null) {
                        credentials.handleAuthenticationError();
                    }
                }

                // Forward the error to the original observer
                observer.onError(t);

                // Attempt to reconnect only for specific status codes and if we're still running
                if (running.get() && RETRYABLE_STATUS_CODES.contains(code)) {
                    try {
                        logger.info("Reconnecting after error (" + code + ") in " + reconnectDelaySeconds + " seconds...");
                        Thread.sleep(reconnectDelaySeconds * 1000); // Wait before reconnecting

                        // Subscribe again if we're still running
                        if (running.get()) {
                            logger.info("Resubscribing to " + streamName);
                            startStreamingWithReconnect(observer);
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Reconnection interrupted for " + streamName, e);
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

        // Make the call with the reconnecting observer
        try {
            rpcMethod.call(request, reconnectingObserver);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting " + streamName + " stream", e);
            // Trigger error handling through the observer
            reconnectingObserver.onError(e);
        }
    }

    /**
     * Builder class for easier construction of StreamingClient instances.
     */
    public static class Builder<TRequest, TResponse> {
        private StreamingRpcMethod<TRequest, TResponse> rpcMethod;
        private TRequest request;
        private StreamObserver<TResponse> observer;
        private String streamName = "Generic Stream";
        private int reconnectDelaySeconds = 5;
        private JwtCredentials credentials;

        public Builder<TRequest, TResponse> rpcMethod(StreamingRpcMethod<TRequest, TResponse> rpcMethod) {
            this.rpcMethod = rpcMethod;
            return this;
        }

        public Builder<TRequest, TResponse> request(TRequest request) {
            this.request = request;
            return this;
        }

        public Builder<TRequest, TResponse> observer(StreamObserver<TResponse> observer) {
            this.observer = observer;
            return this;
        }

        public Builder<TRequest, TResponse> streamName(String name) {
            this.streamName = name;
            return this;
        }

        public Builder<TRequest, TResponse> reconnectDelay(int seconds) {
            this.reconnectDelaySeconds = seconds;
            return this;
        }

        public Builder<TRequest, TResponse> credentials(JwtCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public StreamingClient<TRequest, TResponse> build() {
            if (rpcMethod == null) {
                throw new IllegalArgumentException("rpcMethod is required");
            }
            if (request == null) {
                throw new IllegalArgumentException("request is required");
            }
            if (observer == null) {
                throw new IllegalArgumentException("observer is required");
            }

            return new StreamingClient<>(rpcMethod, request, observer, streamName, reconnectDelaySeconds, credentials);
        }
    }
}

