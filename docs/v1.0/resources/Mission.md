
[Missions](../../concepts/mission.md#missions) are atomic units of behavior that define a robot's high-level actions.
This API enables users to control robot behavior—from simple navigation
tasks to complex, conditional workflows.

------------
## CreateMission 
Send a robot on a mission of specified type. 
<br/>

### Request

##### robot_id `string` `required`
The ID of the robot that will receive this command.


##### mission `Mission` `required`
Universal wrapper for mission types. Only one [mission type](../../concepts/mission.md#mission-types) may be set at a time.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`base_mission`   |[`BaseMission`](#base_mission-basemission)	| Base missions are applicable to all robot families. |
|`servi_mission`	|[`servi.Mission`](Servi.md#servi_mission-servimission) | Servi missions are specific to the Servi robot family. <br /> Refer to [Servi](Servi.md) for how to create and send a servi mission. |
|`carti_mission`	|[`carti.Mission`](Carti.md#carti_mission-cartimission)	| Carti missions are specific to the Carti robot family.<br /> Refer to [Carti](Carti.md) for how to create and send a carti mission. |

##### base_mission `BaseMission`
Use field `base_mission` to send a base mission. Current API version supports 2 types of base missions.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`navigate_mission`   |[`NavigateMission`](#navigate_mission-navigatemission)	| Create a base mission of type `Navigate`. |
|`navigate_auto_mission`	|[`NavigateAutoMission`](#navigate_auto_mission-navigateautomission)| Create a base mission of type `NavigateAuto`. |

##### navigate_mission `NavigateMission`
A mission consisting of a single, explicitly defined goal.

| Field  | Message Type | Description |
|------------|-------------| ---|
|`goal`   |[`Goal`](#goal-goal-required)<br>`required`	| The target destination or pose for the mission. |


##### navigate_auto_mission `NavigateAutoMission`
A mission that automatically selects the best available goal from the provided list. The system will choose the closest goal while avoiding goals currently occupied by other robots.

| Field  | Message Type | Description |
|------------|-------------| ---|
|`goals`   |*repeated* [`Goal`](#goal-goal-required)<br>`required`	| The **list** of target destinations for the mission. |

##### goal `Goal` `required`

[Goal](../../concepts/mission.md#goals) represents a target destination **or** pose for the robot to navigate to.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|[destination_id](../../v1.0/resources/LocationsAndMaps.md) | `string` | Unique identifier for the destination.|

**Refer to the [examples](#examples) for how to create and send a Mission.**

### Response

##### **mission_id** `string`
The ID of the mission created. 

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  The robot is already executing another mission. <br /> This command is valid if current mission is in [terminal state](#state-enum), <br /> e.g Cancelled, Succeeded, Failed. |


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

    func createMissionWithDestination() {
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

      req := &corepb.CreateMissionRequest{
        RobotId: "pennybot-123abc",
        Mission: &corepb.Mission{
          BaseMission: &corepb.BaseMission{
            NavigateMission: &corepb.NavigateMission{
              Goal: &corepb.Goal{
                Goal: &corepb.Goal_DestinationId{
                  DestinationId: "dest-001",
                },
              },
            },
          },
        },
      }

      resp, err := stub.CreateMission(ctx, req)
      if err != nil {
        log.Printf("CreateMission (destination_id) failed: %v", err)
        return
      }

      fmt.Println("CreateMission (destination_id) response:", resp)
    }

    func main() {
      createMissionWithDestination()
    }
    ```

=== "Python gRPC"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Fetch your API key JWT and return it as a string.
        pass

    def create_channel_with_credentials_refresh():
        # Create a secure connection with SSL credentials.
        pass

    def create_mission_request_with_destination():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = CloudAPIServiceStub(channel)

            request = core_pb2.CreateMissionRequest(
                robot_id="pennybot-123abc",
                mission=core_pb2.Mission(
                    base_mission=core_pb2.BaseMission(
                        navigate_mission=core_pb2.NavigateMission(
                            goal=core_pb2.Goal(
                                destination_id="dest-001"
                            )
                        )
                    )
                )
            )

            response = stub.CreateMission(request)
            print("CreateMission (destination) response:", response)

        except grpc.RpcError as e:
            print(f"gRPC error: {e.code().name} - {e.details()}")
        except Exception as ex:
            print(f"Unexpected error: {type(ex).__name__} - {ex}")
    ```
=== "Python HTTP/POST"
    ```python
    import requests

    BEAR_API_BASE_URL = "https://api.bearrobotics.api"
    ACCESS_TOKEN = "YOUR_JWT_BEARER_TOKEN"

    def bear_api_create_mission(robot_id: str, destination_id: str) -> requests.Response:
        """
        Creates a mission for a robot using the Bear API via HTTP POST.

        Args:
            robot_id: The unique ID of the robot to assign the mission to.
            destination_id: The goal destination ID to navigate to.

        Returns:
            A `requests.Response` object from the Bear API.
        """
        url = f"{BEAR_API_BASE_URL}/v1/mission/create"
        payload = {
            "robot_id": robot_id,
            "mission": {
                "baseMission": {
                    "navigateMission": {
                        "goal": {
                            "destinationId": destination_id
                        }
                    }
                }
            }
        }
        headers = {
            "Authorization": f"Bearer {ACCESS_TOKEN}",
            "Content-Type": "application/json"
        }

        return requests.post(url, json=payload, headers=headers)

    def main():
        robot_id = "pennybot-abc123"
        destination_id = "kitchen-42"

        response = bear_api_create_mission(robot_id, destination_id)

        if response.ok:
            print("Mission created:", response.json())
        else:
            print("Bear API error:", response.status_code, response.text)

    if __name__ == "__main__":
        main()

    ```
=== "gRPCurl"
    ```bash
    grpcurl \
      -proto bearrobotics/api/v1/services/cloud_api_service.proto \
      -import-path protos \
      -d '{
        "robot_id": "pennybot-123abc",
        "mission": {
          "baseMission": {
            "navigateMission": {
              "goal": {
                "destinationId": "dest-001"
              }
            }
          }
        }
      }' \
      api-test.bearrobotics.api:443 bearrobotics.api.v1.services.cloud.APIService.CreateMission

    ```

=== "Protobuf"
    ###### Refer to our [public protobuf repo](https://github.com/bearrobotics-public/cloud/tree/v1.0) for actual package names and full definitions.
    ```proto

    message Goal {
      oneof goal {
        string destination_id = 1;
      }
    }

    message NavigateMission {
      Goal goal = 1;
    }

    message NavigateAutoMission {
      repeated Goal goals = 1;
    }
    
    message BaseMission {
      oneof mission {
        NavigateMission navigate_mission = 1;
        NavigateAutoMission navigate_auto_mission = 2;
      }
    }

    message Mission {
      oneof mission {
        BaseMission base_mission = 1;
        servi.Mission servi_mission = 2;
        carti.Mission carti_mission = 3;
      }
    }

    message CreateMissionRequest {
      string robot_id = 1;
      core.Mission mission = 2;
    }

    message CreateMissionResponse {
      string mission_id = 1;
    }
    ```

-----------
## AppendMission 
Appends a mission to the end of the [mission queue](../../concepts/mission.md#mission-queue). <br/>
Use this when a mission is currently running; otherwise, prefer [CreateMission](#createmission). <br/>
Missions are executed in the order they are appended.

### Request / Response/ Examples
!!! note ""
    AppendMission request and response message types are the same as [CreateMission](#createmission).  
    See CreateMission [Examples](#examples).

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  There is no mission in the mission queue.|

-----------
## UpdateMission 
Issues a command to control or update the current mission (e.g., pause, cancel).
!!! warning
    We currently do not support updating missions in [mission queue](../../concepts/mission.md#mission-queue). <br/>
    Attempting to send UpdateMission command to a queued mission will result in `NOT_FOUND` error.

### Request
##### robot_id `string` `required`
The ID of the robot that will receive this command.

##### mission_command `MissionCommand` `required`
[Command](../../concepts/mission.md#command) to update the state of an active mission. <br/>
`MissionCommand` has 2 fields.

| Field  | Message Type | Description |
|------------|-------------| ---------|
| `mission_id` | `string` <br />`required`|  The ID of the mission to control.|
| `command`    | `Command` *enum* <br />`required` |                          |


##### Command `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| COMMAND_UNKNOWN        | 0      | Default value. This should never be used explicitly. <br/> It means the `command` field is not set|
| COMMAND_CANCEL         | 1      | Cancel this mission.                             |
| COMMAND_PAUSE          | 2      | Pause this mission.                              |
| COMMAND_RESUME         | 3      | Resume a paused mission.                         |
| COMMAND_FINISH         | 4      | Mark the mission as completed.                   |

**Refer to the [examples](#examples_1) for how to create and send a MissionCommand.**

-----------
### Response

*(No fields defined)* `{}` 

-----------
### Errors
| ErrorCode  | Description |
|------------|-------------|
| `NOT_FOUND` | The robot is NOT on the mission specified in the request. |
| `INVALID_ARGUMENT`| The command is invalid for the robot's current state. <br /> For example, mission in [terminal state](#state-enum) (Cancelled, Succeeded, Failed) can't be updated. |

-----------
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

    func updateMissionCommand() {
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

      req := &corepb.UpdateMissionRequest{
        RobotId: "pennybot-123abc",
        MissionCommand: &corepb.MissionCommand{
          MissionId: "d6637a14-5f6b-43f6-bd86-cc1871a8322e",
          Command:   corepb.MissionCommand_COMMAND_PAUSE,
        },
      }

      _, err = stub.UpdateMission(ctx, req)
      if err != nil {
        log.Printf("UpdateMission failed: %v", err)
        return
      }

      fmt.Println("Mission command sent successfully.")
    }

    func main() {
      updateMissionCommand()
    }
    ```

=== "Python gRPC"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Fetch your API key JWT and return it as a string.
        pass

    def create_channel_with_credentials_refresh():
        # Create a secure gRPC channel with SSL credentials.
        pass

    def send_mission_command():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = services_pb2_grpc.ServicesStub(channel)

            request = core_pb2.UpdateMissionRequest(
                robot_id="pennybot-123abc",
                mission_command=core_pb2.MissionCommand(
                    mission_id="d6637a14-5f6b-43f6-bd86-cc1871a8322e",
                    command=core_pb2.MissionCommand.COMMAND_PAUSE
                )
            )

            metadata = [("authorization", f"Bearer {token}")]
            response = stub.UpdateMission(request, metadata=metadata)
            print("Mission command response:", response)

        except grpc.RpcError as e:
            print(f"gRPC error: {e.code().name} - {e.details()}")
        except Exception as ex:
            print(f"Unexpected error: {type(ex).__name__} - {ex}")
    ```
=== "Python HTTP/POST"
    ```python
    import requests

    API_BASE_URL = "https://api.bearrobotics.api"
    ACCESS_TOKEN = "YOUR_JWT_BEARER_TOKEN"

    def update_mission(robot_id: str, mission_id: str, command: str) -> requests.Response:
        """
        Sends a mission update command via HTTP POST to the Bear API.

        Args:
            robot_id: The robot's unique identifier.
            mission_id: The mission UUID to update.
            command: The command to send (e.g., "COMMAND_PAUSE", "COMMAND_CANCEL").

        Returns:
            A `requests.Response` object.
        """
        url = f"{API_BASE_URL}/v1/mission/update"
        payload = {
            "robot_id": robot_id,
            "mission_command": {
                "mission_id": mission_id,
                "command": command
            }
        }
        headers = {
            "Authorization": f"Bearer {ACCESS_TOKEN}",
            "Content-Type": "application/json"
        }

        return requests.post(url, json=payload, headers=headers)

    def main():
        robot_id = "pennybot-abc123"
        mission_id = "f842c8ac-62de-412e-90fb-bf37022db2f4"
        command = "COMMAND_PAUSE"

        response = update_mission(robot_id, mission_id, command)

        if response.ok:
            print("Mission update sent successfully.")
        else:
            print("Error:", response.status_code, response.text)

    if __name__ == "__main__":
        main()
    ```
=== "gRPCurl"
    ```bash
    grpcurl \
      -proto bearrobotics/api/v1/services/cloud_api_service.proto \
      -import-path protos \
      -d '{
        "robot_id": "pennybot-123abc",
        "mission_command": {
          "mission_id": "d6637a14-5f6b-43f6-bd86-cc1871a8322e",
          "command": "COMMAND_PAUSE"
        }
      }' \
      api-test.bearrobotics.api:443 bearrobotics.api.v1.services.cloud.APIService.UpdateMission
    ```

=== "Protobuf"
    ```proto
    message MissionCommand {
      string mission_id = 1;

      enum Command {
        COMMAND_UNKNOWN = 0;
        COMMAND_CANCEL = 1;
        COMMAND_PAUSE = 2;
        COMMAND_RESUME = 3;
        COMMAND_FINISH = 4;
      }

      Command command = 2;
    }

    message UpdateMissionRequest {
      string robot_id = 1;
      core.MissionCommand mission_command = 2;
    }

    message UpdateMissionResponse {}
    ```

-----------
## SubscribeMissionStatus 
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to get updates on the robot's mission state. Upon subscription, the latest known mission state is sent immediately. Subsequent updates are streamed as the state changes. 

### Request
##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. <br/>
 It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

**Refer to the [examples](#examples_2) for how to send a SubscribeMissionStatus request.**


### Response
This endpoint returns a stream of messages in response. <br/>
Each message includes:

##### metadata `EventMetadata`

| Field | Message Type | Description |
|------|------|-------------|
| `timestamp` | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | int64 | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with. 

##### mission_state `MissionState`

| Field | Message Type | Description |
|------|------|-------------|
| `mission_id` | string | Unique identifier for the mission. |
| `state` | State *enum* |  |
| `goals` | [Goal](#goal-goal-required) | All goals associated with the mission, <br />in the order the request was given. |
| `current_goal_index` | int32 | Index of the currently active goal in the goals list. |
| `mission_feedback` | MissionFeedback | Latest feedback for the mission. |


##### State `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATE_UNKNOWN          | 0      | Default value. It means the `state` field is not returned. |
| STATE_DEFAULT          | 1      | Initial state when no mission has been run (e.g., feedback is empty).    |
| STATE_RUNNING          | 2      | The mission is actively running.                 |
| STATE_PAUSED           | 3      | The mission is paused.                          |
| STATE_CANCELED         | 4      | The mission was canceled before completion.    |
| STATE_SUCCEEDED        | 5      | The mission completed successfully.           |
| STATE_FAILED           | 6      | The mission encountered an error or failure.          |

##### MissionFeedback
`BaseFeedback` will be returned when sending one of the [BaseMission](#mission-mission-required) missions.

##### BaseFeedback `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATUS_UNKNOWN         | 0      | Default value. It means `status` field is not returned. |
| STATUS_NAVIGATING      | 1      | The robot is currently navigating to its target.|


### Errors

| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
| `INVALID_ARGUMENT`| One or more request parameters are malformed or logically incorrect. <br /> Example: Using an invalid robot ID format. |

### Examples
##### Request

=== "Go"
    ```go
    package main

    import (
      "context"
      "fmt"
      "io"
      "log"
      "time"

      "google.golang.org/grpc"
      "google.golang.org/grpc/metadata"

      corepb "your_project_path/bearrobotics/api/v1/core"
      servicespb "your_project_path/bearrobotics/api/v1/services"
    )

    func GetToken() (string, error) {
      // Fetch your API key JWT.
    }

    func createChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
      // Create a secure gRPC channel.
    }

    func subscribeMissionStatus() {
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

      ctx, cancelCtx := context.WithTimeout(context.Background(), 60*time.Second)
      defer cancelCtx()
      ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+token)

      stream, err := stub.SubscribeMissionStatus(ctx, &corepb.SubscribeMissionStatusRequest{
        Selector: &corepb.RobotSelector{
          TargetId: &corepb.RobotSelector_RobotIds{
            RobotIds: &corepb.RobotSelector_RobotIDs{
              Ids: []string{"pennybot-123abc", "pennybot-abc123"},
            },
          },
        },
      })
      if err != nil {
        log.Fatalf("Failed to subscribe: %v", err)
      }

      for {
        resp, err := stream.Recv()
        if err == io.EOF {
          break
        }
        if err != nil {
          log.Printf("Stream error: %v", err)
          break
        }
        fmt.Println("Mission status update:", resp)
      }
    }

    func main() {
      subscribeMissionStatus()
    }
    ```

=== "Python gRPC Streaming"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Return your JWT auth token.
        pass

    def create_channel_with_credentials_refresh():
        # Return a secure gRPC channel.
        pass

    def subscribe_mission_status():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = services_pb2_grpc.ServicesStub(channel)

            request = core_pb2.SubscribeMissionStatusRequest(
                selector=core_pb2.RobotSelector(
                    robot_ids=core_pb2.RobotSelector.RobotIDs(
                        ids=["pennybot-123abc", "pennybot-abc123"]
                    )
                )
            )

            metadata = [("authorization", f"Bearer {token}")]
            stream = stub.SubscribeMissionStatus(request, metadata=metadata)

            for response in stream:
                print("Mission status update:", response)

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
        "selector": {
          "robotIds": {
            "ids": ["pennybot-123abc", "pennybot-abc123"]
          }
        }
      }' \
      api.bearrobotics.api:443 \
      bearrobotics.api.v1.services.cloud.APIService.SubscribeMissionStatus
    ```
=== "Protobuf"
    ```proto
    message RobotSelector {
      message RobotIDs {
        repeated string ids = 1;
      }

      oneof target_id {
        RobotIDs robot_ids = 1;
        string location_id = 2;
      }
    }

    message SubscribeMissionStatusRequest {
      core.RobotSelector selector = 1;
    }
    ```

##### Response
=== "JSON" 
    ```js
    {
      "metadata": {
        "timestamp": "2025-04-01T15:30:00Z",
        "sequenceNumber": 128
      },
      "robot_id": "pennybot-abc123",
      "mission_state": {
        "mission_id": "d6637a14-5f6b-43f6-bd86-cc1871a8322e",
        "state": "STATE_RUNNING",
        "goals": [
          {
            "destinationId": "pickup_zone"
          },
          {
            "pose": {
              "xMeters": 4.2,
              "yMeters": 7.8,
              "headingRadians": 1.57
            }
          }
        ],
        "current_goal_index": 1,
        "mission_feedback": {
          "baseFeedback": {
            "status": "STATUS_NAVIGATING"
          }
        }
      }
    }

    ```

=== "Protobuf"
    ```proto
    message BaseFeedback {

    enum Status {
      STATUS_UNKNOWN = 0;

      STATUS_NAVIGATING = 1;
    }
      Status status = 1;
    }

    message MissionState {
      string mission_id = 1;

      enum State {
        STATE_UNKNOWN = 0;

        STATE_DEFAULT = 1;

        STATE_RUNNING = 2;

        STATE_PAUSED = 3;

        STATE_CANCELED = 4;

        STATE_SUCCEEDED = 5;

        STATE_FAILED = 6;
      }
      State state = 2;

      repeated Goal goals = 3;

      int32 current_goal_index = 4;

      message MissionFeedback {
        oneof feedback {
          BaseFeedback base_feedback = 1;
        }
      }

      MissionFeedback mission_feedback = 5;
    }

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

    message SubscribeMissionStatusResponse {
      core.EventMetadata metadata = 1;
      string robot_id = 2;
      core.MissionState mission_state = 3; 
    }

    message EventMetadata {
      
      google.protobuf.Timestamp timestamp = 1;

      int64 sequence_number = 2;
    }
    ```
    
-----------
## ChargeRobot
`ChargeRobot` is a special type of mission. Use this command to instruct the robot to begin charging, regardless of its current battery level. <br />

This command is only supported on robots equipped with a contact-based charging dock. Robots without a compatible dock will return an `INVALID_ARGUMENT` error. <br />

You can use [`SubscribeBatteryStatus`](RobotStatus.md#subscribebatterystatus) to monitor the charging process. 

### Request

##### robot_id `string` `required`
The ID of the robot that will receive this command.

### Response
##### **mission_id** `string`
The ID of the mission created. Since this command is a special type of mission, its execution state is also avaiable in response messages from [`SubscribeMissionStatus`](#subscribemissionstatus). 

### Errors

| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  The robot is already executing a mission. <br />  The current mission must be canceled before issuing this command. |
