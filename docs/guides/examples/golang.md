# Go Examples

This page provides example usage of the Bear Cloud API Service in Go.

## Prerequisites

Ensure you have installed the Go packages required to compile the protocol buffers:

```bash
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

## Defining Import Paths

In order to compile protocol buffers into Go code, you must provide Go import paths. Please refer to the [Protocol Buffers Documentation](https://protobuf.dev/reference/go/go-generated/#package).

## Compiling Protocol Buffers into Go

Run the following commands to generate Go files from the protocol buffer files:

```bash
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

PROTO_OUT="examples/go/generated_protos"
mkdir -p "$PROTO_OUT"

# If you did not specify import paths in the proto files with `option go_package`,
# then you must add another --go_opt=M${PROTO_FILE}=${GO_IMPORT_PATH} flag for each proto.
protoc --proto_path=. --go_out="$PROTO_OUT" --go_opt=paths=source_relative \
       --go-grpc_out="$PROTO_OUT" --go-grpc_opt=paths=source_relative \
       bearrobotics/api/v0/**/*.proto google/api/*.proto
```

## List Robot IDs

#### Prerequisites:
- Compiled pb.go files for `cloud_api_service`
- API key JWT (See the [Authentication Guide](../setup/authentication.md))

```go
package main

import (
	"context"
	"fmt"
	"log"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"

	// Import your compiled protobuf files here.
	cloud_api_service_pb "path/to/compiled/protos"
)

const (
	BEAR_API_ENDPOINT = "target.endpoint.bearrobotics.ai:port"
	BEAR_ROBOT_IDS    = []string{
		"your-robot-id-1",
		"your-robot-id 2",
	}
)

func loadBearerToken() (string, error) {
	// Read your API key token from JWT into a string.
	// See the example Go client code in the Authentication Guide.
}

func createChannelWithCredentials() (*grpc.ClientConn, error) {
	// Create a secure connection with SSL credentials.
	// See the example Go client code in the Authentication Guide.
}

func listRobotIDs() {
	// Load the Bearer token from file.
	token, err := loadBearerToken()
	if err != nil {
		log.Fatalf("Failed to load token: %v", err)
	}

	// Create the gRPC channel.
	conn, err := createChannelWithCredentials()
	if err != nil {
		log.Fatalf("Failed to create channel: %v", err)
	}
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

func main() {
	listRobotIDs()
}
```

## Subscribe To Battery Status

#### Prerequisites:
- Compiled pb.go files for `cloud_api_service`
- API key JWT (See the [Authentication Guide](../setup/authentication.md))

```go
package main

import (
	"context"
	"fmt"
	"io"
	"log"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"

	// Import your compiled protobuf files here.
	cloud_api_service_pb "path/to/compiled/protos"
)

const (
	BEAR_API_ENDPOINT = "target.endpoint.bearrobotics.ai:port"
	BEAR_ROBOT_IDS    = []string{
		"your-robot-id-1",
		"your-robot-id 2",
	}
)

func loadBearerToken() (string, error) {
	// Read your API key token from JWT into a string.
	// See the example Go client code in the Authentication Guide.
}

func createChannelWithCredentials() (*grpc.ClientConn, error) {
	// Create a secure connection with SSL credentials.
	// See the example Go client code in the Authentication Guide.
}

func subscribeBatteryStatus() {
	// Load the Bearer token from file.
	token, err := loadBearerToken()
	if err != nil {
		log.Fatalf("Failed to load token: %v", err)
	}

	// Create the gRPC channel.
	conn, err := createChannelWithCredentials()
	if err != nil {
		log.Fatalf("Failed to create channel: %v", err)
	}
	defer conn.Close()

	// Create the stub.
	client := cloud_api_service_pb.NewCloudAPIServiceClient(conn)

	// Prepare the request.
	req := &cloud_api_service_pb.SubscribeBatteryStatusRequest{
		Selector: &cloud_api_service_pb.RobotSelector{
			TargetId: &cloud_api_service_pb.RobotSelector_RobotIds{
				RobotIds: &cloud_api_service_pb.RobotSelector_RobotIDs{
					Ids: BEAR_ROBOT_IDS,
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

func main() {
	subscribeBatteryStatus()
}
```