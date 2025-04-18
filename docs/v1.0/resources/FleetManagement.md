Utilities for managing multiple robots within an account or workspace.

## ListRobotIDs 
Retrieves a list of robot IDs the user has access to, filtered by optional criteria. <br />
The list includes all known robots, regardless of their current connection state. <br />

### Request
##### filter `RobotFilter` `required`
`RobotFilter` defines the conditions for selecting robots. <br/>
All specified fields are combined using an **AND** condition. 


| Field | Message Type | Description |
|------|------|-------------|
|`location_id`| `string`|An empty location_id return all robots assigned <br />to all locations created and owned by API key user.  |

### Response
A response message has 2 fields: <br/>

| Field | Message Type | Description |
|------|------|-------------|
|`total_robots`| `int32`| Total number of robots. |
|`robot_ids`| List of `string` | This might not have all the robot IDs if there are too many.<br /> It will have all the robot IDs if the number of <br /> `robot_ids` is same as `total_robots`. |


### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to retrieve Robot IDs with a `location_id` you don't own. <br /> Tips: check the spelling of `location_id`.|

### Examples
=== "Go"
    ```go
    package main

    import (
      "context"
      "fmt"
      "log"
      "time"

      "google.golang.org/grpc"
      "google.golang.org/grpc/metadata"

      corepb "your_project_path/bearrobotics/api/v1/core"
      servicespb "your_project_path/bearrobotics/api/v1/services"
    )

    func GetToken() (string, error) {
      // Fetch your API key JWT and return it as a string.
    }

    func createChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
      // Create a secure connection with SSL credentials.
    }

    func listRobotIDs() {
      token, err := GetToken()
      if err != nil {
        log.Fatalf("Failed to get token: %v", err)
      }

      conn, cancel, err := createChannelWithCredentialsRefresh()
      if err != nil {
        log.Fatalf("Failed to create channel: %v", err)
      }
      defer cancel()
      defer conn.Close()

      stub := servicespb.NewServicesClient(conn)

      ctx, cancelCtx := context.WithTimeout(context.Background(), 5*time.Second)
      defer cancelCtx()
      ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+token)

      req := &corepb.ListRobotIDsRequest{
        Filter: &corepb.RobotFilter{
          LocationId:    "location-123",
        },
      }

      resp, err := stub.ListRobotIDs(ctx, req)
      if err != nil {
        log.Fatalf("ListRobotIDs failed: %v", err)
      }

      fmt.Println("Robot IDs:", resp)
    }

    func main() {
      listRobotIDs()
    }
    ```

=== "Python gRPC"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Fetch your API key JWT
        pass

    def create_channel_with_credentials_refresh():
        # Return a secure gRPC channel
        pass

    def list_robot_ids():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = services_pb2_grpc.ServicesStub(channel)

            request = core_pb2.ListRobotIDsRequest(
                filter=core_pb2.RobotFilter(
                    location_id="location-123",
                )
            )

            metadata = [("authorization", f"Bearer {token}")]
            response = stub.ListRobotIDs(request, metadata=metadata)
            print("Robot IDs:", response)

        except grpc.RpcError as e:
            print(f"gRPC error: {e.code().name} - {e.details()}")
        except Exception as ex:
            print(f"Unexpected error: {type(ex).__name__} - {ex}")
    ```

=== "gRPCurl"
    ```bash
    grpcurl \
      -proto bearrobotics/api/v1/services/cloud_api_service.proto \
      -import-path protos \
      -d '{
        "filter": {
          "locationId": "location-123",
        }
      }' \
      api.bearrobotics.api:443 \
      bearrobotics.api.v1.services.cloud.APIService.ListRobotIDs
    ```

=== "Protobuf"
    ###### Refer to our [public protobuf repo](https://github.com/bearrobotics-public/cloud/tree/v1.0) for actual package names and full definitions.
    ```proto
    message RobotFilter {
      string location_id = 1;
    }

    message ListRobotIDsRequest {
      core.RobotFilter filter = 1;
    }
    ```
