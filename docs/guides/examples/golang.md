# Go Examples

This page provides example usage of the Bear Cloud API Service in Go.

## Prerequisites

Ensure you have installed the Go packages required to compile the protocol buffers:

```bash
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

## Defining Import Paths

In order to compile protocol buffers into Go code, you must first provide Go import paths. Please refer to the [Protocol Buffers Documentation](https://protobuf.dev/reference/go/go-generated/#package).

## Compiling Protocol Buffers into Go

Run the following commands to generate Go files from the protocol buffer files:

```bash
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

PROTO_OUT="examples/go/generated_protos"
mkdir -p "$PROTO_OUT"

# If you did not specify import paths in each proto file with `option go_package`,
# then you must add another --go_opt=M${PROTO_FILE}=${GO_IMPORT_PATH} flag for each here.
protoc --proto_path=. --go_out="$PROTO_OUT" --go_opt=paths=source_relative \
       --go-grpc_out="$PROTO_OUT" --go-grpc_opt=paths=source_relative \
       bearrobotics/api/v0/**/*.proto google/api/*.proto
```

## Importing Compiled Protos

```go
import compiled_pb "path/to/compiled/proto"
```

## Connecting to the API with Credentials

#### Prerequisites:
- API key (See the [Authentication Guide](../setup/authentication.md))

```go
func GetToken() (string, error) {
	// Fetch your API key JWT and return it as a string.
	// See the example Go client code in the Authentication Guide.
}

func createChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
	// Create a secure connection with SSL credentials.
	// See the example Go client code in the Authentication Guide.
}
```

## List Robot IDs

#### Prerequisites:
- [Compiled pb.go files](#compiling-protocol-buffers-into-go) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

```go
func listRobotIDs() {
	// Load the Bearer token from file.
	token, err := loadBearerToken()
	if err != nil {
		log.Fatalf("Failed to load token: %v", err)
	}

	// Create the gRPC channel.
	conn, cancelRefresher, err := createChannelWithCredentials()
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

## Subscribe To Battery Status

#### Prerequisites:
- [Compiled pb.go files](#compiling-protocol-buffers-into-go) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

```go
func subscribeBatteryStatus() {
	// Load the Bearer token from file.
	token, err := loadBearerToken()
	if err != nil {
		log.Fatalf("Failed to load token: %v", err)
	}

	// Create the gRPC channel.
	conn, cancelRefresher, err := createChannelWithCredentials()
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