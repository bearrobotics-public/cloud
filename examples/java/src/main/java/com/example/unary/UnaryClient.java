package com.example.unary;

import com.example.auth.JwtCredentials;
import io.grpc.Status;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic unary client for gRPC unary RPCs with retry logic.
 * Handles authentication, retry logic, and error handling for any unary RPC.
 *
 * @param <TRequest> The request type for the unary RPC
 * @param <TResponse> The response type for the unary RPC
 */
public class UnaryClient<TRequest, TResponse> {
    private static final Logger logger = Logger.getLogger(UnaryClient.class.getName());

    // Set of status codes that should trigger a retry
    private static final Set<Status.Code> RETRYABLE_STATUS_CODES = EnumSet.of(
        Status.Code.UNAVAILABLE,        // Server is temporarily unavailable
        Status.Code.INTERNAL,           // Server encountered an internal error
        Status.Code.DEADLINE_EXCEEDED,  // Request deadline exceeded
        Status.Code.UNAUTHENTICATED     // Authentication failure - handled separately
    );

    private final UnaryRpcMethod<TRequest, TResponse> rpcMethod;
    private final TRequest request;
    private final String rpcName;
    private final int maxRetries;
    private final long retryDelayMs;
    private final JwtCredentials credentials;

    /**
     * Creates a new unary client with default retry configuration.
     *
     * @param rpcMethod The unary RPC method to call
     * @param request The request to send
     * @param rpcName Name of the RPC for logging purposes
     * @param credentials JWT credentials for authentication error handling
     */
    public UnaryClient(UnaryRpcMethod<TRequest, TResponse> rpcMethod,
                      TRequest request,
                      String rpcName,
                      JwtCredentials credentials) {
        this(rpcMethod, request, rpcName, 3, 1000, credentials);
    }

    /**
     * Creates a new unary client with custom retry configuration.
     *
     * @param rpcMethod The unary RPC method to call
     * @param request The request to send
     * @param rpcName Name of the RPC for logging purposes
     * @param maxRetries Maximum number of retry attempts
     * @param retryDelayMs Delay between retries in milliseconds
     * @param credentials JWT credentials for authentication error handling
     */
    public UnaryClient(UnaryRpcMethod<TRequest, TResponse> rpcMethod,
                      TRequest request,
                      String rpcName,
                      int maxRetries,
                      long retryDelayMs,
                      JwtCredentials credentials) {
        this.rpcMethod = rpcMethod;
        this.request = request;
        this.rpcName = rpcName;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.credentials = credentials;
    }

    /**
     * Executes the unary RPC call with retry logic.
     *
     * @return CompletableFuture containing the response
     */
    public CompletableFuture<TResponse> call() {
        return callWithRetry(0);
    }

    /**
     * Synchronously executes the unary RPC call with retry logic.
     *
     * @return The response
     * @throws Exception If the RPC fails after all retries
     */
    public TResponse callBlocking() throws Exception {
        try {
            return call().join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new Exception("RPC call failed", cause);
            }
        }
    }

    /**
     * Internal method that performs the RPC call with retry logic.
     *
     * @param attemptNumber Current attempt number (0-based)
     * @return CompletableFuture containing the response
     */
    private CompletableFuture<TResponse> callWithRetry(int attemptNumber) {
        logger.info("Attempting " + rpcName + " RPC call (attempt " + (attemptNumber + 1) + "/" + (maxRetries + 1) + ")");

        return CompletableFuture.supplyAsync(() -> {
            try {
                return rpcMethod.call(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(throwable -> {
            Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
            Status status = Status.fromThrowable(cause);
            Status.Code code = status.getCode();

            logger.log(Level.WARNING, rpcName + " RPC failed: {0} (Code: {1})",
                    new Object[]{cause.getMessage(), code});

            // Handle authentication errors specially
            if (code == Status.Code.UNAUTHENTICATED) {
                logger.warning("Authentication error detected. Token may have expired and will be refreshed.");
                if (credentials != null) {
                    credentials.handleAuthenticationError();
                }
            }

            // Check if we should retry based on the status code and attempt count
            boolean shouldRetry = RETRYABLE_STATUS_CODES.contains(code) && attemptNumber < maxRetries;

            if (shouldRetry) {
                logger.info("Error is retryable (Status code: " + code + "). Will attempt retry " +
                        (attemptNumber + 2) + "/" + (maxRetries + 1) + " after " + retryDelayMs + "ms delay");

                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                // Recursive retry call
                return callWithRetry(attemptNumber + 1).join();
            } else {
                if (attemptNumber >= maxRetries) {
                    logger.severe("Max retries (" + maxRetries + ") exceeded for " + rpcName + " RPC. Giving up.");
                } else {
                    logger.info("Error is NOT retryable (Status code: " + code + "). Will not retry.");
                }

                // Re-throw the original exception
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        });
    }

    /**
     * Builder class for easier construction of UnaryClient instances.
     */
    public static class Builder<TRequest, TResponse> {
        private UnaryRpcMethod<TRequest, TResponse> rpcMethod;
        private TRequest request;
        private String rpcName = "Generic RPC";
        private int maxRetries = 3;
        private long retryDelayMs = 1000;
        private JwtCredentials credentials;

        public Builder<TRequest, TResponse> rpcMethod(UnaryRpcMethod<TRequest, TResponse> rpcMethod) {
            this.rpcMethod = rpcMethod;
            return this;
        }

        public Builder<TRequest, TResponse> request(TRequest request) {
            this.request = request;
            return this;
        }

        public Builder<TRequest, TResponse> rpcName(String name) {
            this.rpcName = name;
            return this;
        }

        public Builder<TRequest, TResponse> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder<TRequest, TResponse> retryDelay(long delayMs) {
            this.retryDelayMs = delayMs;
            return this;
        }

        public Builder<TRequest, TResponse> credentials(JwtCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public UnaryClient<TRequest, TResponse> build() {
            if (rpcMethod == null) {
                throw new IllegalArgumentException("rpcMethod is required");
            }
            if (request == null) {
                throw new IllegalArgumentException("request is required");
            }

            return new UnaryClient<>(rpcMethod, request, rpcName, maxRetries, retryDelayMs, credentials);
        }
    }
}