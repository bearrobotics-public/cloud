## Introduction

This document describes the [gRPC-based](https://grpc.io/) Bear Cloud API Service for third-party clients. The Bear Cloud API service enables third parties to:

1. Send commands to control the robots
2. Receive status updates from robots (e.g., tray status, battery status)
3. Retrieve information about the robots

For a detailed list of API capabilities, please navigate through the **API Reference**. Bear Cloud API uses the open-source gRPC framework. In the future, corresponding REST APIs will be added.

## Overview

Third-party clients communicate with the Bear Cloud API service via [gRPC](https://grpc.io/docs/what-is-grpc/introduction). For request/response type communication, [unary RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#unary-rpc) is used. For scenarios where clients need continuous updates from the Bear Cloud API service, [server streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) is utilized.

For server streaming RPCs, all responses include metadata containing a timestamp and a sequence number:
<br>
- **Timestamp**: The timestamp is based on the local clock where the response was generated and is not globally synchronized, so it should not be used for ordering responses.
<br>
- **Sequence Number**: The sequence number is guaranteed to be incremental and can be used to detect duplicate responses. Note that the sequence number may reset to 0 at any time, though this should be rare (e.g., only during a service or robot restart).

If strict detection of response duplication is desired, both the sequence number and timestamp should be used together. 

Also note the following for streaming RPC queries:
<br>
- It will persist for a maximum of 60 minutes, if the query is still needed longer than this time a query must be reissued.
<br>
- The messages have delivery guarantees (i.e. QoS) of “[best effort](https://en.wikipedia.org/wiki/Best-effort_delivery)” and not “at least once”<sup>[1](#footnote1)</sup>, unless specifically noted. This means that some messages can be dropped and not delivered to clients due to multiple reasons such as, but not limited to, packet loss, network traffic load, and processing failures.
---
<a name="footnote1">1</a>: A message will be delivered to the client at least one time, and it might be delivered multiple times due to network issues or processing failures.
<br>
- As noted above, clients need to handle duplicate messages.

When a gRPC call completes successfully, the server returns an OK status along with the specified response to the client. If an error occurs, gRPC returns an [error status code](https://grpc.io/docs/guides/error/#error-status-codes) and an error message.

## Message Encoding
 gRPC uses [Protocol Buffers](https://protobuf.dev/) which provide a serialization format for encoding and decoding, as 
 well as Interface  Definition  Language

