# Quick Start Guide

Get up and running with the Bear Robotics Cloud API v1 client.

## Prerequisites

- Java 17 or higher (latest LTS 21 recommended)
- Valid Bear Robotics API credentials

## Step 1: Setup Credentials

Create `src/main/resources/credentials.json`:

```json
{
  "api_key": "your-api-key-here",
  "secret": "your-secret-here",
  "scope": "Bear"
}
```

## Step 2: Build and Run

```bash
# Build the project
./gradlew build
```

## Step 3: Try Different Request Types (Streaming/Unary)

```bash
# Basic battery status streaming with custom error handling
./gradlew run --args="<target_robot_id> basic"

# Concurrent streaming (both robot and mission status)
./gradlew run --args="<target_robot_id> concurrent"

# Unary request example (CreateMission)
./gradlew run --args="<target_robot_id> unary"
```

## Step 4: Custom Implementation

Create your own streaming client:

```java
public class MyStreamingApp {
    public static void main(String[] args) throws Exception {
        String robotId = args.length > 0 ? args[0] : "robot-1";
        
        // Initialize client
        BearRoboticsClient client = new BearRoboticsClient("api.bearrobotics.ai", 443);

        // Build request
        RobotSelector selector = RobotSelector.newBuilder()
            .setRobotIds(RobotSelector.RobotIDs.newBuilder()
                .addIds(robotId)
                .build())
            .build();
        SubscribeMissionStatusRequest request = SubscribeMissionStatusRequest.newBuilder()
            .setSelector(selector)
            .build();

        // Create streaming client with custom callback
        StreamingClient<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse> streamingClient =
            client.<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse>createStreamingClient()
                .rpcMethod((req, observer) -> client.getAsyncStub().subscribeMissionStatus(req, observer))
                .request(request)
                .onResponse(response -> {
                    // Your custom logic here
                    System.out.println("Robot: " + response.getRobotId() + 
                                     " - Mission: " + response.getMissionState().getMissionId() +
                                     " - State: " + response.getMissionState().getState());
                })
                .onError(error -> {
                    // Your custom logic here
                    System.err.println("Error: " + error.getMessage());
                })
                .streamName("My Mission Stream")
                .build();

        // Start streaming
        streamingClient.startStreaming();
    }
}
```

## Key Features

- **Automatic JWT token refresh** - No auth interruptions
- **Smart reconnection logic** - Retries subscription upon recoverable error codes
- **Custom callbacks** - Handle data with custom callbacks (`onResponse`, `onError`, `onCompleted`)

## Next Steps

- Check out [Main.java](src/main/java/com/example/Main.java) for implementations for different patterns

## Support

If you encounter issues:

1. Check your `credentials.json` format and file location
2. Enable debug logging: `Logger.getLogger("com.example.streaming").setLevel(Level.FINE)`
3. Check network connectivity to the API Server `api.bearrobotics.ai:443`

The client handles most connection issues automatically through reconnection logic.

