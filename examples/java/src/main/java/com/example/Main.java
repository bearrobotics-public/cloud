package com.example;

import com.example.streaming.StreamingClient;
import com.example.unary.UnaryClient;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bearrobotics.api.v1.core.FleetSelector.RobotSelector;
import bearrobotics.api.v1.services.cloud.ApiService.*;
import bearrobotics.api.v1.core.MissionOuterClass.*;
import bearrobotics.api.v1.core.AnnotationOuterClass.Goal;

/**
 * Examples demonstrating different ways to use the generic streaming client.
 * This class shows various patterns and use cases for streaming gRPC calls.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private final BearRoboticsClient client;

    public Main(BearRoboticsClient client) {
        this.client = client;
    }

    /**
     * Example 1: Battery status streaming with custom error handling.
     * Shows how to implement custom error handling and completion logic.
     */
    public void basicBatteryStatusExample(String robotId) throws InterruptedException {
        logger.info("=== Mission Status Example ===");

        RobotSelector selector = RobotSelector.newBuilder()
                .setRobotIds(RobotSelector.RobotIDs.newBuilder()
                    .addIds(robotId)
                    .build())
                .build();

        SubscribeBatteryStatusRequest request = SubscribeBatteryStatusRequest.newBuilder()
                .setSelector(selector)
                .build();

        // Custom stream observer with detailed logging and error handling
        StreamObserver<SubscribeBatteryStatusResponse> observer = new StreamObserver<SubscribeBatteryStatusResponse>() {
            @Override
            public void onNext(SubscribeBatteryStatusResponse response) {
                logger.info("Robot ID: " + response.getRobotId());
                logger.info("Battery percent: " + response.getBatteryState().getChargePercent());
                logger.info("Charge method: " + response.getBatteryState().getChargeMethod());
            }

            @Override
            public void onError(Throwable error) {
                logger.severe("Custom error handler triggered: " + error.getMessage());
                // You could implement custom error handling logic here
                // For example: send alerts, log to external systems, etc.
            }

            @Override
            public void onCompleted() {
                logger.info("Custom completion handler: Mission status stream ended gracefully");
                // You could implement cleanup logic here
            }
        };

        StreamingClient<SubscribeBatteryStatusRequest, SubscribeBatteryStatusResponse> streamingClient =
            client.<SubscribeBatteryStatusRequest, SubscribeBatteryStatusResponse>createStreamingClient()
                .rpcMethod(client.getAsyncStub()::subscribeBatteryStatus)
                .request(request)
                .observer(observer)
                .streamName("Mission Status")
                .reconnectDelay(10) // Custom reconnect delay of 10 seconds
                .build();

        streamingClient.startStreaming();
    }

    /**
     * Example 2: Multiple concurrent streams.
     * Shows how to run multiple streaming clients simultaneously.
     */
    public void multipleConcurrentStreamsExample(String robotId) throws InterruptedException {
        logger.info("=== Multiple Concurrent Streams Example ===");

        CountDownLatch allStreamsLatch = new CountDownLatch(2);

        // Start mission status stream in background thread
        Thread missionThread = new Thread(() -> {
            try {
                RobotSelector missionSelector = RobotSelector.newBuilder()
                        .setRobotIds(RobotSelector.RobotIDs.newBuilder()
                            .addIds(robotId)
                            .build())
                        .build();

                SubscribeMissionStatusRequest missionRequest = SubscribeMissionStatusRequest.newBuilder()
                        .setSelector(missionSelector)
                        .build();

                StreamObserver<SubscribeMissionStatusResponse> missionObserver = new StreamObserver<SubscribeMissionStatusResponse>() {
                    @Override
                    public void onNext(SubscribeMissionStatusResponse response) {
                        logger.info("[MISSION-THREAD] Robot: " + response.getRobotId() + " - " + response.getMissionState());
                    }

                    @Override
                    public void onError(Throwable error) {
                        logger.severe("[MISSION-THREAD] Error: " + error.getMessage());
                        allStreamsLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("[MISSION-THREAD] Stream completed");
                        allStreamsLatch.countDown();
                    }
                };

                StreamingClient<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse> missionClient =
                    client.<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse>createStreamingClient()
                        .rpcMethod(client.getAsyncStub()::subscribeMissionStatus)
                        .request(missionRequest)
                        .observer(missionObserver)
                        .streamName("Concurrent Mission Status")
                        .build();

                missionClient.startStreaming();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                allStreamsLatch.countDown();
            }
        });

        // Start robot status stream in background thread
        Thread statusThread = new Thread(() -> {
            try {
                RobotSelector selector = RobotSelector.newBuilder()
                        .setRobotIds(RobotSelector.RobotIDs.newBuilder()
                            .addIds(robotId)
                            .build())
                        .build();

                SubscribeRobotStatusRequest statusRequest = SubscribeRobotStatusRequest.newBuilder()
                        .setSelector(selector)
                        .build();

                StreamObserver<SubscribeRobotStatusResponse> statusObserver = new StreamObserver<SubscribeRobotStatusResponse>() {
                    @Override
                    public void onNext(SubscribeRobotStatusResponse response) {
                        logger.info("[STATUS-THREAD] Robot: " + response.getRobotId() + " - " + response.getRobotState());
                    }

                    @Override
                    public void onError(Throwable error) {
                        logger.severe("[STATUS-THREAD] Error: " + error.getMessage());
                        allStreamsLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("[STATUS-THREAD] Stream completed");
                        allStreamsLatch.countDown();
                    }
                };

                StreamingClient<SubscribeRobotStatusRequest, SubscribeRobotStatusResponse> statusClient =
                    client.<SubscribeRobotStatusRequest, SubscribeRobotStatusResponse>createStreamingClient()
                        .rpcMethod(client.getAsyncStub()::subscribeRobotStatus)
                        .request(statusRequest)
                        .observer(statusObserver)
                        .streamName("Concurrent Robot Status")
                        .build();

                statusClient.startStreaming();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                allStreamsLatch.countDown();
            }
        });

        missionThread.setName("MissionStreamThread");
        statusThread.setName("StatusStreamThread");

        missionThread.start();
        statusThread.start();

        // Wait for both streams to complete
        allStreamsLatch.await();
        logger.info("All concurrent streams completed");
    }

    /**
     * Example 3: Unary RPC with retry logic.
     * Shows how to use the UnaryClient for RPC calls with retry capabilities.
     */
    public void unaryRpcExample(String robotId, String destinationId) {
        logger.info("=== Unary RPC Example ===");
        logger.info("Sending robot " + robotId + " to destination: " + destinationId);

        // Create a Goal with the destination_id
        Goal goal = Goal.newBuilder()
                .setDestinationId(destinationId)
                .build();

        // Create a NavigateMission with the goal
        NavigateMission navigateMission = NavigateMission.newBuilder()
                .setGoal(goal)
                .build();

        // Create a BaseMission with the NavigateMission
        BaseMission baseMission = BaseMission.newBuilder()
                .setNavigateMission(navigateMission)
                .build();

        // Wrap the BaseMission in a Mission
        Mission mission = Mission.newBuilder()
                .setBaseMission(baseMission)
                .build();

        CreateMissionRequest request = CreateMissionRequest.newBuilder()
                .setRobotId(robotId)
                .setMission(mission)
                .build();

        // Create unary client with retry configuration
        UnaryClient<CreateMissionRequest, CreateMissionResponse> unaryClient =
            client.<CreateMissionRequest, CreateMissionResponse>createUnaryClient()
                .rpcMethod(req -> client.getBlockingStub().createMission(req))
                .request(request)
                .rpcName("CreateMission")
                .maxRetries(5)
                .retryDelay(2000)
                .build();

        try {
            CreateMissionResponse response = unaryClient.callBlocking();
            logger.info("CreateMission Response received");
            logger.info("Mission ID: " + response.getMissionId());
        } catch (Exception e) {
            logger.severe("Unary RPC failed after all retries: " + e.getMessage());
        }
    }

    /**
     * Main method to run the examples.
     */
    public static void main(String[] args) {
        String host = "api.bearrobotics.ai";
        int port = 443;
        String robotId = args.length > 0 ? args[0] : "robot-1";
        String example = args.length > 1 ? args[1] : "basic";
        String destinationId = args.length > 2 ? args[2] : "destination_1";

        try {
            BearRoboticsClient client = new BearRoboticsClient(host, port);
            Main examples = new Main(client);

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    client.shutdown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

            switch (example.toLowerCase()) {
                case "basic":
                    examples.basicBatteryStatusExample(robotId);
                    break;
                case "concurrent":
                    examples.multipleConcurrentStreamsExample(robotId);
                    break;
                case "unary":
                    examples.unaryRpcExample(robotId, destinationId);
                    break;
                default:
                    logger.severe("Unknown example: " + example);
                    logger.info("Available examples: basic, concurrent, unary");
                    return;
            }

        } catch (IOException e) {
            logger.severe("Failed to initialize client: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.info("Examples interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
