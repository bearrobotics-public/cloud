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

##### mission `Mission` `required`
Universal wrapper for mission types. Only one [mission type](../../concepts/mission.md#mission-types) may be set at a time.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`base_mission`   |[`BaseMission`](Mission.md#base_mission-basemission)	| Base missions are applicable to all robot families. <br /> Refer to [Mission](Mission.md) for how to create and send a base mission.|
|`servi_mission`	|[`servi.Mission`](Servi.md#servi_mission-servimission) | Servi missions are specific to the Servi robot family.<br /> Refer to [Servi](Servi.md) for how to create and send a servi mission. |
|`carti_mission`	|[`carti.Mission`](#carti_mission-cartimission)| Carti missions are specific to the Carti robot family. |

##### carti_mission `carti.Mission`
Use the field `carti_mission` to create and send a mission. Current API version supports 2 types of Carti missions.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`traverse_mission`   |`TraverseMission`	| Create a carti mission of type `Traverse`. |
|`traverse_patrol_mission`	|`TraversePatrolMission`| Create a carti mission of type `TraversePatrol`. |

**TraverseMission** <br />
A traverse mission that navigates to one or more goals, stopping at each for a set amount of time or until directed to continue.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`TraverseParams` <br />`optional`|  ***There is no param defined in this API version.*** |

**TraversePatrolMission** <br />
A traverse patrol mission that navigates to one or more goals and continuously loops through the goals, stopping at each for a set amount of time or until directed to continue.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`TraversePatrolParam` <br />`optional`|  ***There is no param defined in this API version.*** |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "carti-001",
        "mission": {
          "cartiMission": {
            "traverseMission": {
              "goals": [
                { "destinationId": "room-a" },
                { "destinationId": "room-b" }
              ],
              "params": {}
            }
          }
        }
      }
    ```

### Response
##### **mission_id** `string`
The ID of the mission created. 

##### JSON Response Example
=== "JSON"
    ```js
      {
        "missionId": "mission-abc123"
      }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
|`INVALID_ARGUMENT`      | This command is sending to is not a Carti family robot. |
|`FAILED_PRECONDITION`   |  The robot is already executing another mission. <br /> This command is valid if current mission is in [terminal state](Mission.md#state-enum), <br /> e.g Cancelled, Succeeded, Failed. |

-----------

## GetConveyorIndex

Retrieves the configured conveyor indexes for the robot. Indexes represent logical positions, not physical installation:

- **Carti 100 (vertical)**: INDEX_1ST = uppermost conveyor
- **Carti 600 (horizontal)**: INDEX_1ST = front facing conveyor

### Request

##### robot_id `string` `required`
The ID of the robot to retrieve conveyor indexes for.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "carti-001"
      }
    ```

### Response

##### indexes `repeated int32`
List of available conveyor indexes for this robot.

##### JSON Response Example
=== "JSON"
    ```js
      {
        "indexes": [1, 2]
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INTERNAL` | Internal server error occurred while processing the request. |
| `INVALID_ARGUMENT` | This command is being sent to a non-Carti family robot. |
| `NOT_FOUND` | The specified robot ID does not exist or is not accessible. |

-----------
## SubscribeConveyorStatus
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to get conveyor status updates for the robot. Upon subscription, the latest known conveyor states are sent immediately. Updates are streamed when any conveyor state changes.

### Request
##### robot_id `string` `required`
The ID of the robot to subscribe to conveyor status for.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "carti-001"
      }
    ```

### Response

##### states `repeated ConveyorState`
State of all conveyors on the robot.

##### ConveyorState
Represents the state of a single conveyor.

| Field | Message Type | Description |
|------|------|-------------|
| `index` | int32 | Unique identifier for the conveyor (logical position). |
| `operation_state` | OperationState *enum* | Current operation state of the conveyor motor. |
| `payload_state` | PayloadState *enum* | Current state of the payload on the conveyor. |
| `health_state` | HealthState *enum* | Current health of the conveyor. |
| `installation_state` | InstallationState *enum* | Current installation state of the conveyor. |

##### OperationState `enum`
| Name | Number | Description |
|------|--------|-------------|
| OPERATION_STATE_UNKNOWN | 0 | Default value. It means the `operation_state` field is not returned. |
| OPERATION_STATE_ROLLING | 1 | The conveyor's motor is rolling in any direction. |
| OPERATION_STATE_STOP | 2 | The conveyor's motor is stopped. |

##### PayloadState `enum`
| Name | Number | Description |
|------|--------|-------------|
| PAYLOAD_STATE_UNKNOWN | 0 | Default value. It means the `payload_state` field is not returned. |
| PAYLOAD_STATE_LOADED | 1 | The conveyor has a payload. |
| PAYLOAD_STATE_EMPTY | 2 | The conveyor is empty. |

##### HealthState `enum`
| Name | Number | Description |
|------|--------|-------------|
| HEALTH_STATE_UNKNOWN | 0 | Default value. It means the `health_state` field is not returned. |
| HEALTH_STATE_OK | 1 | The conveyor is installed and functioning. |
| HEALTH_STATE_ERROR | 2 | The conveyor is not functioning. |

##### InstallationState `enum`
| Name | Number | Description |
|------|--------|-------------|
| INSTALLATION_STATE_UNKNOWN | 0 | Default value. It means the `installation_state` field is not returned. |
| INSTALLATION_STATE_INSTALLED | 1 | The conveyor is installed. |
| INSTALLATION_STATE_NOT_INSTALLED | 2 | The conveyor is not installed. |

##### JSON Response Example
=== "JSON"
    ```js
    {
      "states": [
        {
          "index": 1,
          "operationState": "OPERATION_STATE_ROLLING",
          "payloadState": "PAYLOAD_STATE_LOADED",
          "healthState": "HEALTH_STATE_OK",
          "installationState": "INSTALLATION_STATE_INSTALLED"
        },
        {
          "index": 2,
          "operationState": "OPERATION_STATE_STOP",
          "payloadState": "PAYLOAD_STATE_EMPTY",
          "healthState": "HEALTH_STATE_OK",
          "installationState": "INSTALLATION_STATE_INSTALLED"
        }
      ]
    }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request conveyor status for a `robot_id` you don't own. <br /> Tip: check the spelling of the `robot_id` value. |

-----------
## ControlConveyor

Controls conveyor motor operations for the specified conveyor indexes. This call allows manual control of conveyor motors for clockwise/counter-clockwise rotation or stop commands.

### Request

##### robot_id `string` `required`
The ID of the robot that will receive this command.

##### commands `repeated carti.ConveyorMotorCommand` `required`
List of conveyor motor commands to execute.

##### ConveyorMotorCommand
| Field | Message Type | Description |
|------|------|-------------|
| `index` | int32 | The target conveyor to control. |
| `command` | CommandConveyorMotor *enum* | The motor command to execute. |

##### CommandConveyorMotor `enum`
| Name | Number | Description |
|------|--------|-------------|
| COMMAND_CONVEYOR_MOTOR_UNKNOWN | 0 | Default value. This should never be used explicitly. |
| COMMAND_CONVEYOR_MOTOR_STOP | 1 | Stop the conveyor. |
| COMMAND_CONVEYOR_MOTOR_CW | 2 | Rotate the conveyor clockwise. |
| COMMAND_CONVEYOR_MOTOR_CCW | 3 | Rotate the conveyor counter-clockwise. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "carti-001",
        "commands": [
          {
            "index": 1,
            "command": "COMMAND_CONVEYOR_MOTOR_CW"
          },
          {
            "index": 2,
            "command": "COMMAND_CONVEYOR_MOTOR_STOP"
          }
        ]
      }
    ```

### Response

*(No fields defined)*

##### JSON Response Example
=== "JSON"
    ```js
      {}
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INTERNAL` | Internal server error occurred while processing the request. |
| `INVALID_ARGUMENT` | This command is being sent to a non-Carti family robot, or one or more conveyor indexes are not installed on the robot. |
| `FAILED_PRECONDITION` | The robot is in an error state that prevents conveyor control. |
