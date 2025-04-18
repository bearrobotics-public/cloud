Provides controls and subscriptions related to a robot’s position within its environment, including localization and pose tracking.

## LocalizeRobot 
Localizes the robot to a known pose or destination. <br />

If the request is accepted, subscribe to [**SubscribeLocalizationStatus**](#subscribelocalizationstatus) 
to track localization progress. <br />

### Request
##### robot_id `string` `required`
The ID of the robot that the localization command is sent to.

##### goal `Goal` `required`
[Goal](../../concepts/mission.md#goals) represents a target destination or pose for the robot to localize to.

| Field  | Message Type | Description |
|------------|-------------| ---|
|[destination_id](../../v1.0/resources/LocationsAndMaps.md) | `string` | Unique identifier for the destination.|
|[Pose](../../concepts/localization.md)| [`Pose`](../../v1.0/resources/Localization.md) |`x_meters` *float* X-coordinate in meters within the map. <br/> `x_meters` *float* Y-coordinate in meters within the map. <br/> `heading_radians` *float* The heading of the robot in radians. Ranges from -π to π, where 0.0 points along the positive x-axis.|

### Response

*(No fields defined)* `{}` 

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  While the robot is localizing, any subsequent requests <br /> to localize the robot will return a error until the process is completed.|

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
      // Fetch JWT token
    }

    func createChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
      // Return a secure channel
    }

    func localizeRobotWithPose() {
      token, err := GetToken()
      if err != nil {
        log.Fatalf("Token error: %v", err)
      }

      conn, cancel, err := createChannelWithCredentialsRefresh()
      if err != nil {
        log.Fatalf("Connection error: %v", err)
      }
      defer cancel()
      defer conn.Close()

      stub := servicespb.NewServicesClient(conn)

      ctx, cancelCtx := context.WithTimeout(context.Background(), 5*time.Second)
      defer cancelCtx()
      ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+token)

      req := &corepb.LocalizeRobotRequest{
        RobotId: "pennybot-aaa111",
        Goal: &corepb.Goal{
          Goal: &corepb.Goal_Pose{
            Pose: &corepb.Pose{
              XMeters:        1.5,
              YMeters:        2.8,
              HeadingRadians: -0.52,
            },
          },
        },
      }

      _, err = stub.LocalizeRobot(ctx, req)
      if err != nil {
        log.Fatalf("LocalizeRobot failed: %v", err)
      }

      fmt.Println("Localization request sent.")
    }

    func main() {
      localizeRobotWithPose()
    }
    ```

=== "Python gRPC"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Return JWT token
        pass

    def create_channel_with_credentials_refresh():
        # Return secure channel
        pass

    def localize_robot_with_pose():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = services_pb2_grpc.ServicesStub(channel)

            request = core_pb2.LocalizeRobotRequest(
                robot_id="pennybot-111aaa",
                goal=core_pb2.Goal(
                    pose=core_pb2.Pose(
                        x_meters=1.5,
                        y_meters=2.8,
                        heading_radians=-0.52
                    )
                )
            )

            metadata = [("authorization", f"Bearer {token}")]
            stub.LocalizeRobot(request, metadata=metadata)
            print("Localization request sent.")

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
        "robot_id": "pennybot-123123",
        "goal": {
          "pose": {
            "xMeters": 1.5,
            "yMeters": 2.8,
            "headingRadians": -0.52
          }
        }
      }' \
      api.bearrobotics.api:443 \
      bearrobotics.api.v1.services.cloud.CloudAPIService.LocalizeRobot
    ```

=== "Protobuf"
    ###### Refer to our [public protobuf repo](https://github.com/bearrobotics-public/cloud/tree/v1.0) for actual package names and full definitions.

    ```proto
    message Pose {
      float x_meters = 1;
      float y_meters = 2;
      float heading_radians = 3;
    }

    message Goal {
      oneof goal {
        string destination_id = 1;
        Pose pose = 2;
      }
    }

    message LocalizeRobotRequest {
      string robot_id = 1;
      core.Goal goal = 2;
    }
    ```


-----------
## SubscribeLocalizationStatus
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to get the robot’s localization state. Upon subscription, the latest localization state is sent immediately. State updates are streamed while localization is active.

### Request
##### robot_id `string` `required`
The ID of the robot that subscription request is sent to.

### Response

##### metadata `EventMetadata`

| Field | Message Type | Description |
|------|------|-------------|
| `timestamp` | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | int64 | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### LocalizationState `enum`
| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATE_UNKNOWN          | 0      | Default value. It means the `state` field is not returned. |
| STATE_FAILED          | 1      |  Localization failed.  |
| STATE_SUCCEEDED          | 2      | Localization completed successfully.     |
| STATE_LOCALIZING           | 3      | The robot is actively attempting to localize.  |

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for `robot_id` you don't own. <br /> Tips: check the spelling of the `robot_id`.|

### Examples
##### Response
=== "JSON"
    ```json
    {
      "metadata": {
        "timestamp": "2025-04-01T17:30:00Z",
        "sequenceNumber": 98
      },
      "localizationState": {
        "state": "STATE_LOCALIZING"
      }
    }
    ```

=== "Protobuf"
    ```proto
    message LocalizationState {
      enum State {
        STATE_UNKNOWN = 0;
        STATE_FAILED = 1;
        STATE_SUCCEEDED = 2;
        STATE_LOCALIZING = 3;
      }
      State state = 2;
    }

    message EventMetadata {
      google.protobuf.Timestamp timestamp = 1;
      int64 sequence_number = 2;
    }

    message SubscribeLocalizationStatusResponse {
      core.EventMetadata metadata = 1;
      core.LocalizationState localization_state = 2;
    }
    ```


-----------
## SubscribeRobotPose
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to subscribe to the robot's pose estimates at a regular frequency. (~10Hz) Use this to track the robot's position in real time.

!!! Warning
    Current implementation supports up to 5 robots. Requests with more than 5 robots will return a `RESOURCE_EXHAUSTED` error. When using `location_id` as a selector, the response will include the first 5 robots sorted alphabetically.

### Request
##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. <br/>
 It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

### Response
##### PoseWithMetadata

| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [EventMetadata](RobotStatus.md#metadata-eventmetadata) | Metadata associated with the event. |
| `pose` | Pose | Pose of the robot on the map. |

##### Pose

| Field | Message Type | Description |
|------|------|-------------|
| `x_meters` | float | X-coordinate in meters within the map. |
| `y_meters` | float | Y-coordinate in meters within the map. |
| `heading_radians` | float | The heading of the robot in radians.<br>Ranges from -π to π, where 0.0 points along the positive x-axis. |


### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for `RobotIDs`  or a `location_id` you don't own. <br /> Tip: check the spelling of the `RobotIDs` or `location_id`.|

### Examples
##### Response
=== "JSON"
    ```js
    {
      "poses": {
        "pennybot-abc123": {
          "metadata": {
            "timestamp": "2025-04-01T17:45:00Z",
            "sequenceNumber": 201
          },
          "pose": {
            "xMeters": 1.5,
            "yMeters": 3.2,
            "headingRadians": 0.78
          }
        },
        "pennybot-123abc": {
          "metadata": {
            "timestamp": "2025-04-01T17:45:02Z",
            "sequenceNumber": 202
          },
          "pose": {
            "xMeters": 0.0,
            "yMeters": 0.0,
            "headingRadians": -3.14
          }
        }
      }
    }
    ```

=== "Protobuf"
    ```proto
    message Pose {
      float x_meters = 1;
      float y_meters = 2;
      float heading_radians = 3;
    }

    message PoseWithMetadata {
      EventMetadata metadata = 1;
      Pose pose = 2;
    }

    message EventMetadata {
      google.protobuf.Timestamp timestamp = 1;
      int64 sequence_number = 2;
    }

    message SubscribeRobotPoseResponse {
      map<string, core.PoseWithMetadata> poses = 2;
    }
    ```

