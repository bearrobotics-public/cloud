These endpoints and their message types are only available for the **Carti** robot family. Attempting to run a **Carti** commands on a non **Carti** robot will result in an INVALID_ARGUMENT error.

------------
## CreateMission 
Use the shared `CreateMission` endpoint to send missions for Carti robots. Carti-specific missions must be sent using the appropriate request message format. <br/>

!!! Note
    When sending a Carti mission, `carti.Feedback` is returned in [`SubscribeMissionStatus`](Mission.md#subscribemissionstatus) response message.
    
    ##### carti.Feedback `enum`

    | Name                   | Number | Description                                      |
    |------------------------|--------|--------------------------------------------------|
    | STATUS_UNKNOWN         | 0      | Default value. It means `status` field is not returned. |
    | STATUS_NAVIGATING      | 1      | The robot is currently navigating to its goal.|
    | STATUS_ARRIVED         | 2      | The robot has arrived at a goal. |
    | STATUS_DOCKING     | 3      | The robot is performing a docking maneuver.|
    | STATUS_UNDOCKING         | 4      | The robot is performing an undocking maneuver.|

### Request
##### robot_id `string` `required`
The ID of the robot that will receive this command.


##### mission [`Mission`](Mission.md#mission-mission-required) `required`
Use the field `carti_mission` to create and send a mission. Current API version supports 2 Carti missions.

**TraverseMission** <br />
A traverse mission that navigates to one or more goals. It is defined by 2 fields

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| List of `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`TraverseParams` <br />`optional`|  ***There is no param defined in this API version.*** |

**TraversePatrolMission** <br />
A traverse patrol mission that navigates to one or more goals and continuously loops through the goals stopping at each for a time limit.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| List of `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`TraversePatrolParam` <br />`optional`|  ***There is no param defined in this API version.*** |

**Refer to the [examples](#examples) for how to create and send a Carti mission.**

### Response
##### **mission_id** `string`
The ID of the mission created. 

### Errors

| ErrorCode  | Description |
|------------|-------------|
|`INVALID_ARGUMENT`      | This command is sending to is not a Carti family robot. |
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
      // Return a valid Bearer token.
    }

    func createChannelWithCredentialsRefresh() (*grpc.ClientConn, context.CancelFunc, error) {
      // Return a secure gRPC channel with refreshable credentials.
    }

    func createTraverseMission() {
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
        RobotId: "robot-123",
        Mission: &corepb.Mission{
          Mission: &corepb.Mission_TraverseMission{
            TraverseMission: &corepb.TraverseMission{
              Goals: []*corepb.Goal{
                {
                  Goal: &corepb.Goal_DestinationId{
                    DestinationId: "room-a",
                  },
                },
                {
                  Goal: &corepb.Goal_DestinationId{
                    DestinationId: "room-b",
                  },
                },
              },
              Params: &corepb.TraverseParams{},
            },
          },
        },
      }

      resp, err := stub.CreateMission(ctx, req)
      if err != nil {
        log.Fatalf("CreateMission failed: %v", err)
      }

      fmt.Println("Created mission ID:", resp.MissionId)
    }

    func main() {
      createTraverseMission()
    }
    ```

=== "Python gRPC"
    ```python
    import grpc
    from bearrobotics.api.v1 import core_pb2
    from bearrobotics.api.v1 import services_pb2_grpc

    def get_token():
        # Return a valid Bearer token
        pass

    def create_channel_with_credentials_refresh():
        # Return a secure gRPC channel
        pass

    def create_traverse_mission():
        try:
            token = get_token()
            channel = create_channel_with_credentials_refresh()
            stub = services_pb2_grpc.ServicesStub(channel)

            request = core_pb2.CreateMissionRequest(
                robot_id="robot-123",
                mission=core_pb2.Mission(
                    traverse_mission=core_pb2.TraverseMission(
                        goals=[
                            core_pb2.Goal(destination_id="room-a"),
                            core_pb2.Goal(destination_id="room-b")
                        ],
                        param=core_pb2.TraverseParams()
                    )
                )
            )

            metadata = [("authorization", f"Bearer {token}")]
            response = stub.CreateMission(request, metadata=metadata)
            print("Created mission ID:", response.mission_id)

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
        "robot_id": "robot-123",
        "mission": {
          "traverseMission": {
            "goals": [
              { "destinationId": "room-a" },
              { "destinationId": "room-b" }
            ],
            "params": {}
          }
        }
      }' \
      api.bearrobotics.api:443 \
      bearrobotics.api.v1.services.cloud.CloudAPIService.CreateMission
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

    message TraverseParams {}

    message TraverseMission {
      repeated core.Goal goals = 1;
      TraverseParams params = 2;
    }

    message carti.Mission {
      oneof mission {
        TraverseMission traverse_mission = 1;
      }
    }

    message Mission {
      oneof mission {
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
