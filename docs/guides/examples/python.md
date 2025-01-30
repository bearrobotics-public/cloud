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

## List Robot IDs

**Prerequisites:**
- Compiled pb2 files for `cloud_api_service`
- API key JWT

```python
import grpc
from grpc import StatusCode

# Import your generated protobuf code here.
from cloud_api_service_pb2 import ListRobotIdsRequest
from cloud_api_service_pb2_grpc import CloudAPIServiceStub

BEAR_API_ENDPOINT = "your.endpoint.bearrobotics.ai:port"

# Load Bearer token
with open("/path/to/api-key.jwt", "r") as token_file:
    token = token_file.read().strip()

# Create a channel with the Bearer token in the metadata
def create_channel_with_credentials():
    credentials = grpc.ssl_channel_credentials()
    channel = grpc.secure_channel(BEAR_API_ENDPOINT, credentials)

    return channel

# Create the stub
def list_robot_ids():
    try:
        channel = create_channel_with_credentials()
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

if __name__ == "__main__":
    list_robot_ids()
```

## Subscribe To Battery Status

**Prerequisites:**
- Compiled pb2 files for `cloud_api_service`
- API key JWT

```python
import grpc
from grpc import StatusCode

# Import your service classes here (generated from your .proto file).
from cloud_api_service_pb2 import SubscribeBatteryStatusRequest
from cloud_api_service_pb2_grpc import CloudAPIServiceStub
from config_pb2 import RobotSelector

BEAR_API_ENDPOINT = "your.endpoint.bearrobotics.ai:port"
BEAR_ROBOT_IDS    = [
    "your-robot-id-1",
    "your-robot-id 2",
]

# Load Bearer token
with open("/path/to/api-key.jwt", "r") as token_file:
    token = token_file.read().strip()

# Create a channel with the Bearer token in the metadata
def create_channel_with_credentials():
    credentials = grpc.ssl_channel_credentials()
    channel = grpc.secure_channel(BEAR_API_ENDPOINT, credentials)
    
    return channel

def subscribe_battery_status():
    try:
        # Create the stub
        channel = create_channel_with_credentials()
        stub = CloudAPIServiceStub(channel)
        
        # Create the request body
        request = SubscribeBatteryStatusRequest(
            selector=RobotSelector(
                robot_ids=RobotSelector.RobotIDs(
                    ids=BEAR_ROBOT_IDS
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

if __name__ == "__main__":
    subscribe_battery_status()
```