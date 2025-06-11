package com.example.unary;

/**
 * Functional interface for gRPC unary RPC methods.
 * This allows the generic unary client to work with any unary RPC.
 *
 * @param <TRequest> The request type for the unary RPC
 * @param <TResponse> The response type for the unary RPC
 */
@FunctionalInterface
public interface UnaryRpcMethod<TRequest, TResponse> {
    /**
     * Calls the unary RPC method.
     *
     * @param request The request to send
     * @return The response from the RPC call
     * @throws Exception If the RPC call fails
     */
    TResponse call(TRequest request) throws Exception;
}