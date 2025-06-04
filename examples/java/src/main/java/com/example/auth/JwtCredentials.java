package com.example.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Call credentials that add a JWT token to gRPC requests.
 * Handles token refreshing for long-running connections.
 */
public class JwtCredentials extends CallCredentials implements BearAuthService.TokenRefreshListener {
    private static final Logger logger = Logger.getLogger(JwtCredentials.class.getName());
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of(
            "Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final BearAuthService authService;

    /**
     * Creates new JWT credentials that automatically renew tokens.
     *
     * @param authService The authentication service to use
     */
    public JwtCredentials(BearAuthService authService) {
        this.authService = authService;
        // Register to receive token refresh notifications
        this.authService.setTokenRefreshListener(this);
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            try {
                // Get JWT token (will be refreshed automatically if needed)
                String token = authService.getJwtToken();

                // Apply the token to the request metadata
                Metadata headers = new Metadata();
                headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + token);

                applier.apply(headers);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to apply JWT credentials", e);
                applier.fail(Status.UNAUTHENTICATED.withCause(e).withDescription(
                        "Failed to apply JWT credentials: " + e.getMessage()));
            }
        });
    }

    /**
     * Handles authentication errors by forcing a token refresh.
     * This should be called when an UNAUTHENTICATED error is received from the server.
     */
    public void handleAuthenticationError() {
        try {
            logger.info("Handling authentication error - forcing token refresh");
            authService.forceTokenRefresh();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to refresh token after authentication error", e);
        }
    }

    /**
     * Checks if the given throwable is an authentication error that should trigger token refresh.
     *
     * @param throwable The throwable to check
     * @return true if this is an authentication error
     */
    public static boolean isAuthenticationError(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            StatusRuntimeException sre = (StatusRuntimeException) throwable;
            return sre.getStatus().getCode() == Status.Code.UNAUTHENTICATED;
        }
        return false;
    }

    /**
     * Called when the token is refreshed.
     * For long-running gRPC calls, this doesn't automatically update the current connection,
     * but ensures that new calls will use the new token.
     *
     * @param newToken The new JWT token
     */
    @Override
    public void onTokenRefreshed(String newToken) {
        logger.info("JWT token refreshed, new connections will use the updated token");
    }

    // Note: This method is deprecated but still required by the interface.
    // The warning can be suppressed as it's part of the API we need to implement.
    @Override
    public void thisUsesUnstableApi() {
        // Required by the interface but not used
    }
}
