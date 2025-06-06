# Usage Examples

This page provides example client usage of the Bear Cloud API Service.

For complete, runnable examples, see our [public repository](https://github.com/bearrobotics-public/cloud/tree/main).

## Prerequisites

Ensure you have installed the package(s) required to compile the protocol buffers:

=== "Python"

	```
	pip install grpcio-tools
	```

=== "Go"

	```
	go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
	go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
	```

## Defining Import Paths (Go)

In order to compile protocol buffers into Go code, you must first provide Go import paths. Please refer to the [Protocol Buffers Documentation](https://protobuf.dev/reference/go/go-generated/#package).

## Compiling Protocol Buffers

Run the following script to generate the necessary protocol buffer files:

=== "Python"

	```bash
	cd "<path/to/raw/protos>"

	PROTO_OUT="<path/to/generated/protos>"
	mkdir "$PROTO_OUT"

	# Create __init__.py to make this a proper Python package
	touch "$PROTO_OUT/__init__.py"

	# Generate protos
	python3 -m grpc_tools.protoc \
		-I . \
		--python_out="$PROTO_OUT" \
		--grpc_python_out="$PROTO_OUT" \
		bearrobotics/api/v1/*/*.proto \
		bearrobotics/api/v1/*/*/*.proto \
		google/api/*.proto

	# Fix imports in generated files
	find "$PROTO_OUT" -name "*.py" -type f -exec sed -i 's/from bearrobotics/from <path/to/generated/protos>.bearrobotics/g' {} +
	find "$PROTO_OUT" -name "*.py" -type f -exec sed -i 's/from google.api/from <path/to/generated/protos>.google.api/g' {} +
	```

=== "Go"

	```bash
	cd <"path/to/raw/protos">

	PROTO_OUT="<path/to/generated/protos>"
	mkdir "$PROTO_OUT"

	# Create __init__.py to make this a proper Python package
	touch "$PROTO_OUT/__init__.py"

	# If you did not specify import paths in each proto file with `option go_package`,
	# then you must add another --go_opt=M${PROTO_FILE}=${GO_IMPORT_PATH} flag for each here.
	protoc --proto_path=. \
		--go_out="$PROTO_OUT" \
		--go_opt=paths=source_relative \
		--go-grpc_out="$PROTO_OUT" \
		--go-grpc_opt=paths=source_relative \
		bearrobotics/api/v1/*/*.proto \
		bearrobotics/api/v1/*/*/*.proto \
		google/api/*.proto
	```

## Importing Generated Protos

=== "Python"

	```python
	from path.to.generated.protos.proto_name_pb2 import ProtoFn
	from path.to.generated.protos.proto_name_pb2_grpc import ProtoGRPCFn
	```

=== "Go"

	```go
	import generated_pb "path/to/generated/protos/proto_name_go_proto"
	```

## Connecting to the API with Credentials

#### Prerequisites:
- API key (See the [Authentication Guide](../setup/authentication.md))

=== "Python"

	```python
	def get_token():
		# Fetch your API key JWT and return it as a string.
		# See the Python reference in the Authentication Guide.

	def create_channel_with_credentials_refresh():
		# Create a secure connection with SSL credentials.
		# See the Python reference in the Authentication Guide.
	```

=== "Go"

	```go
	func GetToken() (string, error) {
		// Fetch your API key JWT and return it as a string.
		// See the example Go client code in the Authentication Guide.
	}

	func CreateChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
		// Create a secure connection with SSL credentials.
		// See the example Go client code in the Authentication Guide.
	}
	```

## Example: List Robot IDs

#### Prerequisites:
- [Compiled protobuf files](#compiling-protocol-buffers) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

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
- [Compiled protobuf files](#compiling-protocol-buffers) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

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