# Python Examples

This page provides example usage of the Bear Cloud API Service in Python.

## Prerequisites

Ensure you have installed the Python package required to compile the protocol buffers:

```bash
pip install grpcio-tools
```

## Compiling Protocol Buffers into Python

Run the following commands to generate the necessary protocol buffer files:

```bash
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

PROTO_OUT="examples/python/generated_protos"
mkdir -p "$PROTO_OUT"

python3 -m grpc_tools.protoc -I . --python_out="$PROTO_OUT" --grpc_python_out="$PROTO_OUT" bearrobotics/api/v0/**/*.proto google/api/*.proto
```

## Importing Compiled Protos

```python
from cloud_api_service_pb2 import ListRobotIdsRequest
from cloud_api_service_pb2_grpc import CloudAPIServiceStub
```

## Connecting to the API with Credentials

#### Prerequisites:
- API key (See the [Authentication Guide](../setup/authentication.md))

```python
def get_token():
	# Fetch your API key JWT and return it as a string.
	# See the Python reference in the Authentication Guide.

def create_channel_with_credentials_refresh():
	# Create a secure connection with SSL credentials.
	# See the Python reference in the Authentication Guide.
```

## List Robot IDs

#### Prerequisites:
- [Compiled pb.go files](#compiling-protocol-buffers-into-go) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

```python
def list_robot_ids():
    try:
        # Create the stub
        token = load_bearer_token()
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

## Subscribe To Battery Status

#### Prerequisites:
- [Compiled pb.go files](#compiling-protocol-buffers-into-go) for `cloud_api_service`
- [API connection with credentials](#connecting-to-the-api-with-credentials)

```python
def subscribe_battery_status():
    try:
        # Create the stub
        token = load_bearer_token()
        channel = create_channel_with_credentials()
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