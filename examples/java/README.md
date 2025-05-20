# BCAS gRPC Client Example

This project demonstrates how to use gRPC to connect to the Bear Robotics Cloud API Service (BCAS).

## Project Structure

- `proto/cloud/`: Git submodule containing the official Bear Robotics API protobuf files
- `src/main/java/`: Java source code
  - `com.example`: Main application code
  - `com.example.auth`: Authentication-related classes
- `src/main/resources/`: Configuration files and credentials
- `build.gradle`: Gradle build configuration with protobuf compiler setup

## Setup

1. Clone this repository:
   ```
   git clone https://your-repository-url.git
   cd bcas-example
   ```

2. Set up API credentials:
   Create a file at `src/main/resources/credentials.json` with the following format:
   ```json
   {
     "api_key": "your-api-key",
     "secret": "your-api-secret",
     "scope": "Bear"
   }
   ```
   Note: This file is listed in `.gitignore` to prevent committing credentials.

3. Build the project:
   ```
   ./gradlew build
   ```

## Usage

The Main class demonstrates how to create a gRPC client to connect to the Bear Robotics Cloud API Service.

It includes an example of using server-side streaming with the `SubscribeMissionStatus` RPC method, which allows
you to receive continuous updates about a robot's mission status.

To run the example:

```
./gradlew run --args="pennybot-dbe062"
```

## API Authentication

The project includes a complete implementation of the Bear Robotics API authentication flow:

1. The `BearAuthService` class reads API credentials from a local file
2. It makes an HTTP call to the authentication endpoint to obtain a JWT token
3. The `JwtCredentials` class implements gRPC's `CallCredentials` to add the JWT token to each request
4. Token expiration is handled automatically by refreshing when needed

This implementation securely connects to the API using TLS and proper authentication.

## Long-Running Connections

This project demonstrates best practices for maintaining long-running gRPC connections:

1. **Automatic Token Refreshing**: The `BearAuthService` class uses a scheduled executor to proactively refresh the JWT token before it expires, avoiding authentication errors during streaming calls.

2. **Reconnection Logic**: The client implements automatic reconnection if a streaming connection fails. This allows it to recover from network issues or server-side disruptions.

3. **Keep-Alive Settings**: The gRPC channel is configured with keep-alive settings to detect and recover from "half-open" connections, where one side believes the connection is still active but it's actually broken.

4. **Graceful Shutdown**: The client properly cleans up resources on shutdown, including canceling any active streaming calls and shutting down the token refresh scheduler.

## Development

### Authentication Flow

The authentication process works as follows:

1. API credentials (API key and secret) are loaded from `credentials.json`
2. The application makes a request to `https://api-auth.bearrobotics.ai/authorizeApiAccess` with these credentials
3. The auth endpoint returns a JWT token string
4. This token is attached to all gRPC requests via the Authorization header
5. When the token expires (or a predefined duration of time passes), a new one is automatically fetched

### Token Refresh Handling

JWT tokens from the Bear Robotics API expire after an hour. Our implementation handles this in several ways:

1. **Proactive Refresh**: Tokens are refreshed every 30 minutes before they expire to ensure smooth operation
2. **Background Refresh**: Token refreshing happens in a background thread, not interrupting the main application flow
3. **Error Recovery**: If a request fails with an UNAUTHENTICATED error, the client can retry with a fresh token
4. **Retry Support**: For streaming RPCs, the client can retry with a fresh token if the stream is broken

### Generating code from proto files

The project is configured to automatically generate Java code from the proto files during the build process. The generated code will be placed in `src/generated/`.

