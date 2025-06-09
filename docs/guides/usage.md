# Usage Examples

This page provides example client code snippets for using the Bear Cloud API Service.

For complete, runnable examples, see our [public repository](https://github.com/bearrobotics-public/cloud/tree/main).

## Connecting to the API with Credentials

#### Prerequisites:
- API key (See the [Authentication Guide](authentication.md))

=== "Python"

	```python
	def get_token():
		# Fetch your API key JWT and return it as a string.
		# See the Python reference in the Authentication Guide.

	def create_channel_with_credentials_refresh():
		# Create a secure connection with TLS/SSL credentials.
		# See the Python reference in the Authentication Guide.
	```

=== "Go"

	```go
	func GetToken() (string, error) {
		// Fetch your API key JWT and return it as a string.
		// See the example Go client code in the Authentication Guide.
	}

	func CreateChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
		// Create a secure connection with TLS/SSL credentials.
		// See the example Go client code in the Authentication Guide.
	}
	```

## Compiling and Importing Protocol Buffer Files

#### Prerequisites:
- Bear API protocol buffer (protobuf) files, available for download from our public repository [here](https://github.com/bearrobotics-public/cloud/tree/main/bearrobotics/api)

!!! note
	In order to call Bear API functions, you must first compile the Bear API protobuf files into the language of your choice and import them in your code.

Please see the complete examples in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main) for details.

Additionally, refer to the gRPC reference on generated code: [Java](https://grpc.io/docs/languages/java/generated-code/) | [Python](https://grpc.io/docs/languages/python/generated-code/)

## Example: List Robot IDs

#### Prerequisites:
- [API connection with credentials](#connecting-to-the-api-with-credentials)
- [Compiled protobuf files](#compiling-and-importing-protocol-buffer-files) for `cloud_api_service`

=== "Python"

	```python
	def list_robot_ids():
		try:
			# Create the stub
			token = get_token()
			channel = create_channel_with_credentials_refresh()
			stub = CloudAPIServiceStub(channel)
			
			# Create the request body
			request = ListRobotIDsRequest()

			# Make the gRPC call with the Bearer token as metadata
			response = stub.ListRobotIDs(request, metadata=[('authorization', f'Bearer {token}')])
			
			# Handle the response
			print("Robot IDs:", response)
		except grpc.RpcError as e:
			if e.code() == StatusCode.UNAUTHENTICATED:
				print("Authentication failed! Please check your Bearer token.")
			else:
				print(f"An error occurred: {e.details()}")
	```

=== "Go"

	```go
	func listRobotIDs() {
		// Load the Bearer token from file.
		token, err := GetToken()
		if err != nil {
			log.Fatalf("Failed to load token: %v", err)
		}

		// Create the gRPC channel.
		conn, cancelRefresher, err := CreateChannelWithCredentialsRefresh()
		if err != nil {
			log.Fatalf("Failed to create channel: %v", err)
		}
		defer cancelRefresher()
		defer conn.Close()

		// Create the stub.
		client := cloud_api_service_pb.NewCloudAPIServiceClient(conn)

		// Prepare the request.
		req := &cloud_api_service_pb.ListRobotIDsRequest{}

		// Create metadata with Bearer token.
		md := metadata.New(map[string]string{
			"authorization": fmt.Sprintf("Bearer %s", token),
		})

		// Make the gRPC call with the Bearer token as metadata.
		ctx := metadata.NewOutgoingContext(context.Background(), md)
		resp, err := client.ListRobotIDs(ctx, req)
		if err != nil {
			// Handle the error.
			if status.Code(err) == codes.Unauthenticated {
				log.Println("Authentication failed! Please check your Bearer token.")
			} else {
				log.Printf("An error occurred: %v", err)
			}
			return
		}

		// Handle the response.
		fmt.Println("Listing robot IDs:", resp)
	}
	```

## Example: Subscribe Battery Status

#### Prerequisites:
- [API connection with credentials](#connecting-to-the-api-with-credentials)
- [Compiled protobuf files](#compiling-and-importing-protocol-buffer-files) for `cloud_api_service`

=== "Python"

	```python
	def subscribe_battery_status():
		try:
			# Create the stub
			token = get_token()
			channel = create_channel_with_credentials_refresh()
			stub = CloudAPIServiceStub(channel)
			
			# Create the request body
			request = SubscribeBatteryStatusRequest(
				selector=RobotSelector(
					robot_ids=RobotSelector.RobotIDs(
						ids=[
							"your-robot-id-1",
							"your-robot-id-2",
							"your-robot-id-etc",
						]
					)
				)
			)

			# Make the gRPC call with the Bearer token as metadata
			responseChannel = stub.SubscribeBatteryStatus(request, metadata=[('authorization', f'Bearer {token}')])
			
			# Handle the response
			for response in responseChannel:
				print("Battery Status:", response)
		except grpc.RpcError as e:
			if e.code() == StatusCode.UNAUTHENTICATED:
				print("Authentication failed! Please check your Bearer token.")
			else:
				print(f"An error occurred: {e.details()}")
	```

=== "Go"

	```go
	func subscribeBatteryStatus() {
		// Load the Bearer token from file.
		token, err := GetToken()
		if err != nil {
			log.Fatalf("Failed to load token: %v", err)
		}

		// Create the gRPC channel.
		conn, cancelRefresher, err := CreateChannelWithCredentialsRefresh()
		if err != nil {
			log.Fatalf("Failed to create channel: %v", err)
		}
		defer cancelRefresher()
		defer conn.Close()

		// Create the stub.
		client := cloud_api_service_pb.NewCloudAPIServiceClient(conn)

		// Prepare the request.
		req := &cloud_api_service_pb.SubscribeBatteryStatusRequest{
			Selector: &cloud_api_service_pb.RobotSelector{
				TargetId: &cloud_api_service_pb.RobotSelector_RobotIds{
					RobotIds: &cloud_api_service_pb.RobotSelector_RobotIDs{
						Ids: []string{
							"your-robot-id-1",
							"your-robot-id-2",
							"your-robot-id-etc",
						},
					},
				},
			},
		}

		// Create metadata with Bearer token.
		md := metadata.New(map[string]string{
			"authorization": fmt.Sprintf("Bearer %s", token),
		})

		// Make the gRPC call with the Bearer token as metadata.
		ctx := metadata.NewOutgoingContext(context.Background(), md)
		stream, err := client.SubscribeBatteryStatus(ctx, req)
		if err != nil {
			// Handle the error.
			if status.Code(err) == codes.Unauthenticated {
				log.Println("Authentication failed! Please check your Bearer token.")
			} else {
				log.Printf("An error occurred: %v", err)
			}
			return
		}

		// Handle the response.
		fmt.Println("Subscribing to battery status:")
		for {
			msg, err := stream.Recv()
			if err == io.EOF {
				break
			}
			if err != nil {
				log.Fatalf("client.SubscribeBatteryStatus failed: %v", err)
			}
			log.Printf("Message received: %v", msg)
		}
	}
	```