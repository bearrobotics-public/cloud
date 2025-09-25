# Bear Robotics Cloud API - C# Examples

This project contains C# examples demonstrating how to interact with the Bear Robotics Cloud API using gRPC.

## Prerequisites

- .NET 6.0 SDK or later
- Protocol Buffer compiler (protoc) - will be automatically installed by Grpc.Tools package
- Valid Bear Robotics API credentials

## Project Structure

```
C#/
├── Auth/
│   ├── BearAuthService.cs      # JWT authentication service
│   └── JwtCredentials.cs       # gRPC call credentials implementation
├── Streaming/
│   ├── StreamingClient.cs      # Generic streaming client with reconnection
│   └── StreamingRpcMethod.cs   # Streaming RPC method delegate
├── Unary/
│   ├── UnaryClient.cs          # Generic unary client with retry logic
│   └── UnaryRpcMethod.cs       # Unary RPC method delegate
├── Resources/
│   └── credentials.json        # API credentials (create this file)
├── proto/                      # Proto files (copy from java examples)
├── BearRoboticsClient.cs       # Main client class
├── Program.cs                  # Example scenarios
├── BearRoboticsCloudAPI.csproj # Project configuration
└── README.md                   # This file
```

## Setup

### 1. Create Credentials File

Create a `Resources/credentials.json` file with your API credentials:

```json
{
  "api_key": "your_api_key",
  "secret": "your_secret",
  "scope": "your_scope"
}
```

### 2. Copy Proto Files

Copy the proto files from the Java examples:

```bash
cp -r ../java/proto ./
```

### 3. Install Dependencies

```bash
dotnet restore
```

### 4. Build the Project

```bash
dotnet build
```

## Running the Examples

The application supports three example scenarios:

### Usage

```bash
dotnet run -- <robot_id> <example_type> [destination_id]
```

**Parameters:**
- `robot_id`: The ID of the robot (e.g., "robot-1")
- `example_type`: One of: basic, concurrent, unary
- `destination_id`: (Optional) Required only for unary example (e.g., "destination_1")

### 1. Basic Battery Status Streaming

Demonstrates streaming battery status updates with custom error handling and reconnection logic:

```bash
dotnet run -- robot-1 basic
```

**Response Output:**
- Streams real-time battery status updates showing:
  - Robot ID
  - Battery charge percentage
  - Charge method
- Returns a summary message confirming stream initialization
- Updates continue streaming until the program is stopped

### 2. Concurrent Streams

Demonstrates running multiple streaming subscriptions simultaneously:

```bash
dotnet run -- robot-1 concurrent
```

**Response Output:**
- Starts two concurrent streams:
  - Mission status stream (prefixed with [MISSION-THREAD])
  - Robot status stream (prefixed with [STATUS-THREAD])
- Returns a summary showing:
  - Number of streams started
  - Success/failure status for each stream
- Both streams continue updating until completion

### 3. Unary RPC with Retry

Demonstrates a unary RPC call (CreateMission) with automatic retry on failure:

```bash
dotnet run -- robot-1 unary destination_1
```

**Response Output:**
- Sends a navigation mission request to the API
- On success, displays:
  - Mission ID returned by the server
  - Robot ID used in the request
  - Destination ID
  - Full response object details
- On failure, displays:
  - Error message
  - Inner exception details (if available)
  - Automatically retries up to 5 times with 2-second delays

## Features

### Authentication
- **BearAuthService**: Manages JWT token acquisition and automatic refresh every 30 minutes
- **JwtCredentials**: Implements gRPC CallCredentials for adding JWT tokens to requests
- Automatic token refresh on authentication errors

### Streaming Client
- **Automatic Reconnection**: Reconnects on specific error codes (UNAVAILABLE, INTERNAL, DEADLINE_EXCEEDED, UNAUTHENTICATED)
- **Custom Error Handling**: Allows custom error callbacks for different scenarios
- **Configurable Retry Logic**: Customizable reconnection delays and retry behavior
- **Observer Pattern**: Clean abstraction for handling stream events

### Unary Client
- **Automatic Retry**: Retries failed calls with configurable max attempts and delays
- **Smart Error Handling**: Only retries on specific status codes
- **Async/Sync Support**: Both async and blocking call methods

### Main Client
- **Builder Pattern**: Fluent API for creating streaming and unary clients
- **Resource Management**: Proper cleanup of channels and authentication services
- **Type Safety**: Generic implementations for any request/response types

## Error Handling

The clients handle various error scenarios:

- **Authentication Errors**: Automatic token refresh when UNAUTHENTICATED status is received
- **Network Issues**: Automatic reconnection for transient network failures
- **Server Errors**: Smart retry logic based on gRPC status codes
- **Graceful Shutdown**: Proper resource cleanup on application termination

## Architecture

The C# implementation follows the same architecture as the Java examples:

1. **Authentication Layer**: Handles JWT token management and refresh
2. **Client Wrappers**: Generic implementations for streaming and unary RPCs
3. **Main Client**: Central class managing connections and providing builder APIs
4. **Example Application**: Demonstrates various usage patterns

## Dependencies

- **Grpc.Net.Client**: Modern .NET gRPC client
- **Google.Protobuf**: Protocol Buffers support
- **Grpc.Tools**: Proto file compilation
- **Newtonsoft.Json**: JSON parsing for credentials

## Troubleshooting

### SSL/TLS Issues
If you encounter SSL certificate validation errors, the client includes certificate validation bypass for development. Remove this in production.

### Authentication Failures
- Verify credentials.json is properly formatted
- Check API key, secret, and scope are correct
- Ensure the authentication endpoint is accessible

### Build Issues
- Ensure proto files are in the correct location
- Run `dotnet restore` to update packages
- Check .NET SDK version (6.0 or later required)

## Notes

- The client maintains persistent connections for streaming
- Token refresh happens automatically every 30 minutes
- All examples include proper error handling and logging
- The implementation is thread-safe for concurrent operations