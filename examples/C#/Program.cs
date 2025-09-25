using System;
using System.Threading;
using System.Threading.Tasks;
using BearRoboticsCloudAPI;
using BearRoboticsCloudAPI.Streaming;
using BearRoboticsCloudAPI.Unary;
using Bearrobotics.Api.V1.Core;
using Bearrobotics.Api.V1.Services.Cloud;
using Grpc.Core;

namespace BearRoboticsExamples
{
    class Program
    {
        private readonly BearRoboticsClient _client;

        public Program(BearRoboticsClient client)
        {
            _client = client;
        }

        /// <summary>
        /// Demonstrates how to use the SubscribeBatteryStatus streaming endpoint.
        /// This is a SERVER STREAMING RPC - the server sends multiple messages to the client.
        /// </summary>
        /// <param name="robotId">The ID of the robot to monitor</param>
        /// <returns>The streaming client instance for battery status updates</returns>
        public async Task<StreamingClient<SubscribeBatteryStatusRequest, SubscribeBatteryStatusResponse>> BasicBatteryStatusExample(string robotId)
        {
            Console.WriteLine("=== Battery Status Example ===");

            // Step 1: Create a RobotSelector to specify which robots to monitor
            // The selector can filter by robot IDs, tags, or other criteria
            var selector = new RobotSelector
            {
                RobotIds = new RobotSelector.Types.RobotIDs()
            };
            selector.RobotIds.Ids.Add(robotId);  // Add specific robot ID to monitor

            // Step 2: Create the request message for the streaming RPC
            // This request will be sent once to initiate the stream
            var request = new SubscribeBatteryStatusRequest
            {
                Selector = selector  // Specify which robots to get updates for
            };

            // Step 3: Create an observer to handle incoming stream messages
            // The observer implements callbacks for: OnNext (data), OnError, OnCompleted
            var observer = new BatteryStatusObserver();

            // Step 4: Build the streaming client with configuration
            var streamingClient = _client.CreateStreamingClient<SubscribeBatteryStatusRequest, SubscribeBatteryStatusResponse>()
                // Specify the gRPC streaming method to call
                .RpcMethod((req, opts) => _client.GetClient().SubscribeBatteryStatus(req, opts))
                // Set the request to send when initiating the stream
                .Request(request)
                // Set the observer to handle stream events
                .Observer(observer)
                // Give the stream a friendly name for logging
                .StreamName("Battery Status")
                // Set reconnection delay in seconds (auto-reconnects on failure)
                .ReconnectDelay(10)
                .Build();

            // Step 5: Start the streaming connection
            // This will:
            // - Send the initial request
            // - Begin receiving messages from the server
            // - Call observer.OnNext() for each message
            // - Automatically reconnect on network failures

            // Start streaming in background task to keep it alive
            var streamTask = Task.Run(async () =>
            {
                await streamingClient.StartStreamingAsync();
            });

            // Give the stream a moment to initialize
            await Task.Delay(100);

            Console.WriteLine($"Successfully started battery status stream for robot: {robotId}");
            return streamingClient;
        }

        /// <summary>
        /// Demonstrates how to run multiple streaming RPCs concurrently.
        /// Shows best practices for managing multiple streams in parallel.
        /// </summary>
        /// <param name="robotId">The ID of the robot to monitor</param>
        /// <returns>Summary of started streams</returns>
        public async Task<string> MultipleConcurrentStreamsExample(string robotId)
        {
            Console.WriteLine("=== Multiple Concurrent Streams Example ===");

            // Use CountdownEvent to track when all streams complete
            var allStreamsComplete = new CountdownEvent(2);  // 2 streams total
            var results = new System.Collections.Generic.List<string>();

            // Stream 1: Mission Status Subscription
            // Runs in a separate task to enable concurrent execution
            var missionTask = Task.Run(async () =>
            {
                try
                {
                    // Create selector for mission status updates
                    var missionSelector = new RobotSelector
                    {
                        RobotIds = new RobotSelector.Types.RobotIDs()
                    };
                    missionSelector.RobotIds.Ids.Add(robotId);

                    // Create request for SubscribeMissionStatus endpoint
                    var missionRequest = new SubscribeMissionStatusRequest
                    {
                        Selector = missionSelector
                    };

                    // Observer with prefix to distinguish output from multiple streams
                    var missionObserver = new MissionStatusObserver("[MISSION-THREAD]", allStreamsComplete);

                    // Build and configure the mission status streaming client
                    var missionClient = _client.CreateStreamingClient<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse>()
                        .RpcMethod((req, opts) => _client.GetClient().SubscribeMissionStatus(req, opts))
                        .Request(missionRequest)
                        .Observer(missionObserver)
                        .StreamName("Concurrent Mission Status")
                        .Build();

                    // Start streaming mission updates
                    await missionClient.StartStreamingAsync();
                    results.Add("Mission status stream started successfully");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Mission thread error: {ex.Message}");
                    results.Add($"Mission stream failed: {ex.Message}");
                    allStreamsComplete.Signal();
                }
            });

            // Stream 2: Robot Status Subscription
            // Runs concurrently with the mission status stream
            var statusTask = Task.Run(async () =>
            {
                try
                {
                    // Create selector for robot status updates
                    var statusSelector = new RobotSelector
                    {
                        RobotIds = new RobotSelector.Types.RobotIDs()
                    };
                    statusSelector.RobotIds.Ids.Add(robotId);

                    // Create request for SubscribeRobotStatus endpoint
                    var statusRequest = new SubscribeRobotStatusRequest
                    {
                        Selector = statusSelector
                    };

                    // Observer with different prefix to distinguish output
                    var statusObserver = new RobotStatusObserver("[STATUS-THREAD]", allStreamsComplete);

                    // Build and configure the robot status streaming client
                    var statusClient = _client.CreateStreamingClient<SubscribeRobotStatusRequest, SubscribeRobotStatusResponse>()
                        .RpcMethod((req, opts) => _client.GetClient().SubscribeRobotStatus(req, opts))
                        .Request(statusRequest)
                        .Observer(statusObserver)
                        .StreamName("Concurrent Robot Status")
                        .Build();

                    // Start streaming robot status updates
                    await statusClient.StartStreamingAsync();
                    results.Add("Robot status stream started successfully");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Status thread error: {ex.Message}");
                    results.Add($"Status stream failed: {ex.Message}");
                    allStreamsComplete.Signal();
                }
            });

            // Wait for both streaming tasks to complete
            // This demonstrates how to manage multiple concurrent gRPC streams
            await Task.WhenAll(missionTask, statusTask);
            Console.WriteLine("All concurrent streams completed");

            return $"Started {results.Count} concurrent streams for robot: {robotId}\n" + string.Join("\n", results);
        }

        /// <summary>
        /// Demonstrates how to use the CreateMission unary RPC endpoint.
        /// This is a UNARY RPC - single request, single response pattern.
        /// Shows how to build complex request messages and handle responses.
        /// </summary>
        /// <param name="robotId">The ID of the robot to send on a mission</param>
        /// <param name="destinationId">The destination ID where the robot should navigate</param>
        /// <returns>The CreateMissionResponse from the server</returns>
        public async Task<CreateMissionResponse> UnaryRpcExample(string robotId, string destinationId)
        {
            Console.WriteLine("=== Unary RPC Example ===");
            Console.WriteLine($"Sending robot {robotId} to destination: {destinationId}");

            // Step 1: Build the mission structure using nested protobuf messages
            // Bear Robotics uses a hierarchical mission structure:
            // Mission -> BaseMission -> NavigateMission -> Goal

            // Create the navigation goal with the destination
            var goal = new Goal
            {
                DestinationId = destinationId  // The target location for navigation
            };

            // Wrap the goal in a NavigateMission (one of many mission types)
            var navigateMission = new NavigateMission
            {
                Goal = goal
            };

            // Wrap in BaseMission (can be NavigateMission, ChargeMission, etc.)
            var baseMission = new BaseMission
            {
                NavigateMission = navigateMission  // Using navigation mission type
            };

            // Create the top-level Mission object
            var mission = new Mission
            {
                BaseMission = baseMission
            };

            // Step 2: Create the request with robot ID and mission details
            var request = new CreateMissionRequest
            {
                RobotId = robotId,  // Which robot should execute this mission
                Mission = mission   // The mission to execute
            };

            // Step 3: Build the unary client with retry configuration
            var unaryClient = _client.CreateUnaryClient<CreateMissionRequest, CreateMissionResponse>()
                // Specify the gRPC method to call
                .RpcMethod(req => _client.GetClient().CreateMission(req, _client.GetCallOptions()))
                // Set the request to send
                .Request(request)
                // Give the RPC a friendly name for logging
                .RpcName("CreateMission")
                // Configure automatic retry on failure (5 attempts total)
                .MaxRetries(5)
                // Wait 2 seconds between retry attempts
                .RetryDelay(2000)
                .Build();

            try
            {
                // Step 4: Execute the unary RPC call
                // This sends the request and waits for a single response
                var response = await unaryClient.CallAsync();

                // Step 5: Process the response
                Console.WriteLine("CreateMission Response received");
                Console.WriteLine($"Mission ID: {response.MissionId}");  // Server assigns unique mission ID

                return response;
            }
            catch (Exception ex)
            {
                // Handle failure after all retries exhausted
                Console.WriteLine($"Unary RPC failed after all retries: {ex.Message}");
                throw;
            }
        }

        /// <summary>
        /// Main entry point demonstrating different gRPC communication patterns with Bear Robotics API.
        /// Shows how to:
        /// 1. Initialize the gRPC client with authentication
        /// 2. Execute different types of RPCs (streaming, unary)
        /// 3. Handle responses and errors
        /// </summary>
        static async Task Main(string[] args)
        {
            // Bear Robotics API endpoint configuration
            string host = "api.bearrobotics.ai";  // Production API endpoint
            int port = 443;                       // Standard HTTPS/gRPC port

            // Parse command-line arguments with defaults
            string robotId = args.Length > 0 ? args[0] : "robot-1";
            string example = args.Length > 1 ? args[1] : "basic";
            string destinationId = args.Length > 2 ? args[2] : "destination_1";

            try
            {
                // Initialize the Bear Robotics client
                // This handles:
                // - gRPC channel creation with TLS
                // - JWT authentication setup
                // - Automatic token refresh
                using var client = new BearRoboticsClient(host, port);
                var examples = new Program(client);

                // Ensure graceful shutdown on application exit
                AppDomain.CurrentDomain.ProcessExit += (sender, e) =>
                {
                    client.ShutdownAsync().Wait();
                };

                // Execute the requested example based on command-line argument
                switch (example.ToLower())
                {
                    case "basic":
                        // Demonstrates server streaming RPC
                        // Server continuously sends battery updates to client
                        var streamingClient = await examples.BasicBatteryStatusExample(robotId);
                        Console.WriteLine("\n=== STREAMING STATUS ===");
                        Console.WriteLine($"Battery status stream is active for robot: {robotId}");
                        Console.WriteLine("Waiting for battery status updates...");
                        Console.WriteLine("The stream will automatically reconnect if disconnected.");
                        Console.WriteLine("Battery updates will appear above as they are received.");

                        // Keep the stream alive - wait for updates 
                        await Task.Delay(TimeSpan.FromSeconds(30));
                        Console.WriteLine("\n[Stream will continue in background]");
                        break;

                    case "concurrent":
                        // Demonstrates multiple concurrent streaming RPCs
                        // Shows how to manage multiple real-time data streams
                        var concurrentResult = await examples.MultipleConcurrentStreamsExample(robotId);
                        Console.WriteLine("\n=== STREAMING STATUS ===");
                        Console.WriteLine(concurrentResult);
                        Console.WriteLine($"Multiple concurrent streams are active for robot: {robotId}");
                        Console.WriteLine("Waiting for mission and robot status updates...");
                        Console.WriteLine("The streams will automatically reconnect if disconnected.");
                        Console.WriteLine("Status updates will appear above as they are received.");

                        // Keep the streams alive - wait for updates
                        await Task.Delay(TimeSpan.FromSeconds(30));
                        Console.WriteLine("\n[Streams will continue in background]");
                        break;

                    case "unary":
                        // Demonstrates unary RPC (single request/response)
                        // Common pattern for commands like creating missions
                        try
                        {
                            var unaryResponse = await examples.UnaryRpcExample(robotId, destinationId);
                            Console.WriteLine("\n=== RESPONSE DETAILS ===");
                            Console.WriteLine($"Mission successfully created!");
                            Console.WriteLine($"  Mission ID: {unaryResponse.MissionId}");
                            Console.WriteLine($"  Robot ID: {robotId}");
                            Console.WriteLine($"  Destination: {destinationId}");
                            Console.WriteLine($"  Full Response Object: {unaryResponse}");
                        }
                        catch (Exception ex)
                        {
                            // Handle unary RPC failures
                            Console.WriteLine("\n=== ERROR RESPONSE ===");
                            Console.WriteLine($"Failed to create mission: {ex.Message}");
                            if (ex.InnerException != null)
                            {
                                Console.WriteLine($"Inner Exception: {ex.InnerException.Message}");
                            }
                        }
                        break;

                    default:
                        // Show usage instructions for invalid input
                        Console.WriteLine($"Unknown example: {example}");
                        Console.WriteLine("Available examples: basic, concurrent, unary");
                        Console.WriteLine("Usage: dotnet run -- <robot_id> <example_type> [destination_id]");
                        return;
                }

                // Keep the application running to receive stream updates
                Console.WriteLine("\nPress any key to exit...");
                Console.ReadKey();
            }
            catch (Exception ex)
            {
                // Handle initialization failures (usually authentication issues)
                Console.WriteLine($"\n❌ Failed to initialize client: {ex.Message}");

                // Show inner exception if available for more details
                if (ex.InnerException != null)
                {
                    Console.WriteLine($"   Details: {ex.InnerException.Message}");
                }

                Console.WriteLine("\n📋 Troubleshooting:");
                Console.WriteLine("1. Check your credentials in Resources/credentials.json");
                Console.WriteLine("2. Verify the API endpoint is reachable");
                Console.WriteLine("3. Ensure your API key and secret are valid");

                Console.WriteLine("\n📝 Usage: dotnet run -- <robot_id> <example_type> [destination_id]");
                Console.WriteLine("   robot_id: The ID of the robot (e.g., robot-1)");
                Console.WriteLine("   example_type: One of: basic, concurrent, unary");
                Console.WriteLine("   destination_id: (Optional) Required for unary example");

                // Show stack trace in debug mode
#if DEBUG
                Console.WriteLine("\n🔍 Stack Trace (Debug Mode):");
                Console.WriteLine(ex.StackTrace);
#endif
            }
        }
    }

    /// <summary>
    /// Observer implementation for handling battery status stream events.
    /// This demonstrates the Observer pattern for processing streaming gRPC responses.
    /// </summary>
    class BatteryStatusObserver : StreamingClient<SubscribeBatteryStatusRequest, SubscribeBatteryStatusResponse>.IStreamObserver
    {
        /// <summary>
        /// Called for each message received from the server stream.
        /// This is where you process the actual data from the gRPC endpoint.
        /// </summary>
        /// <param name="response">The battery status update from the server</param>
        public void OnNext(SubscribeBatteryStatusResponse response)
        {
            // Log the complete response object for debugging
            Console.WriteLine("\n=== Battery Status Update Received ===");
            Console.WriteLine($"Full Response: {response}");

            // Extract and display relevant fields from the response
            Console.WriteLine($"Robot ID: {response.RobotId}");
            Console.WriteLine($"Battery percent: {response.BatteryState.ChargePercent}%");
            Console.WriteLine($"Charge method: {response.BatteryState.ChargeMethod}");
            Console.WriteLine($"Timestamp: {DateTime.Now:yyyy-MM-dd HH:mm:ss}");
            Console.WriteLine("=====================================\n");

            // You can add additional processing here:
            // - Store in database
            // - Trigger alerts if battery low
            // - Update UI components
            // - Send notifications
        }

        /// <summary>
        /// Called when an error occurs in the stream.
        /// This could be network issues, authentication failures, or server errors.
        /// </summary>
        /// <param name="error">The exception that occurred</param>
        public void OnError(Exception error)
        {
            Console.WriteLine($"Custom error handler triggered: {error.Message}");

            // Typical error handling:
            // - Log error details
            // - Attempt reconnection (handled by StreamingClient)
            // - Notify monitoring systems
            // - Update UI to show disconnected state
        }

        /// <summary>
        /// Called when the stream completes normally.
        /// This indicates the server has finished sending data.
        /// </summary>
        public void OnCompleted()
        {
            Console.WriteLine("Custom completion handler: Battery status stream ended gracefully");

            // Typical completion handling:
            // - Clean up resources
            // - Log stream statistics
            // - Update UI to show stream ended
            // - Potentially restart stream if needed
        }
    }

    /// <summary>
    /// Observer for mission status updates in concurrent streaming scenario.
    /// Shows how to coordinate multiple streams using synchronization primitives.
    /// </summary>
    class MissionStatusObserver : StreamingClient<SubscribeMissionStatusRequest, SubscribeMissionStatusResponse>.IStreamObserver
    {
        private readonly string _prefix;           // Identifies which stream in logs
        private readonly CountdownEvent _latch;    // Synchronization for multiple streams

        public MissionStatusObserver(string prefix, CountdownEvent latch)
        {
            _prefix = prefix;
            _latch = latch;
        }

        /// <summary>
        /// Processes mission status updates from the server.
        /// Mission state indicates current mission progress (IDLE, RUNNING, COMPLETED, etc.)
        /// </summary>
        public void OnNext(SubscribeMissionStatusResponse response)
        {
            // Display mission state with thread prefix for clarity
            Console.WriteLine($"{_prefix} Robot: {response.RobotId} - {response.MissionState}");

            // Additional processing based on mission state:
            // - Track mission progress
            // - Update mission queue
            // - Trigger next mission if completed
            // - Alert on mission failures
        }

        /// <summary>
        /// Handles stream errors and signals completion to waiting threads.
        /// </summary>
        public void OnError(Exception error)
        {
            Console.WriteLine($"{_prefix} Error: {error.Message}");
            _latch.Signal();  // Signal that this stream is done (even if errored)
        }

        /// <summary>
        /// Handles normal stream completion and signals waiting threads.
        /// </summary>
        public void OnCompleted()
        {
            Console.WriteLine($"{_prefix} Stream completed");
            _latch.Signal();  // Signal that this stream is done
        }
    }

    /// <summary>
    /// Observer for robot status updates in concurrent streaming scenario.
    /// Demonstrates handling robot state changes (IDLE, MOVING, CHARGING, ERROR, etc.)
    /// </summary>
    class RobotStatusObserver : StreamingClient<SubscribeRobotStatusRequest, SubscribeRobotStatusResponse>.IStreamObserver
    {
        private readonly string _prefix;           // Identifies which stream in logs
        private readonly CountdownEvent _latch;    // Synchronization for multiple streams

        public RobotStatusObserver(string prefix, CountdownEvent latch)
        {
            _prefix = prefix;
            _latch = latch;
        }

        /// <summary>
        /// Processes robot status updates from the server.
        /// Robot state indicates current robot activity (IDLE, MOVING, CHARGING, etc.)
        /// </summary>
        public void OnNext(SubscribeRobotStatusResponse response)
        {
            // Display robot state with thread prefix for clarity
            Console.WriteLine($"{_prefix} Robot: {response.RobotId} - {response.RobotState}");

            // Additional processing based on robot state:
            // - Update robot fleet dashboard
            // - Track robot availability
            // - Monitor for error states
            // - Calculate utilization metrics
            // - Trigger maintenance alerts
        }

        /// <summary>
        /// Handles stream errors and ensures proper cleanup.
        /// </summary>
        public void OnError(Exception error)
        {
            Console.WriteLine($"{_prefix} Error: {error.Message}");
            _latch.Signal();  // Signal that this stream is done (even if errored)

            // Error recovery strategies:
            // - Log to monitoring system
            // - Attempt reconnection
            // - Switch to backup stream
            // - Alert operations team
        }

        /// <summary>
        /// Handles graceful stream termination.
        /// </summary>
        public void OnCompleted()
        {
            Console.WriteLine($"{_prefix} Stream completed");
            _latch.Signal();  // Signal that this stream is done
        }
    }
}