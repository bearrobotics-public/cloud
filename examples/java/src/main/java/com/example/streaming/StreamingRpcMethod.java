package com.example.streaming;

import io.grpc.stub.StreamObserver;

/**
 * Functional interface for gRPC streaming RPC methods.
 * This allows the generic streaming client to work with any streaming RPC.
 *
 * @param <TRequest> The request type for the streaming RPC
 * @param <TResponse> The response type for the streaming RPC
 */
@FunctionalInterface
public interface StreamingRpcMethod<TRequest, TResponse> {
    /**
     * Calls the streaming RPC method.
     *
     * @param request The request to send
     * @param observer The observer to receive streaming responses
     */
    void call(TRequest request, StreamObserver<TResponse> observer);
}

