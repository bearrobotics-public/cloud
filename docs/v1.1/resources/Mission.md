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
|`base_mission`   |[`BaseMission`](#base_mission-basemission)	| Base missions are specific to the Base units. |
|`servi_mission`	|[`servi.Mission`](Servi.md#servi_mission-servimission) | Servi missions are specific to the Servi robot family. <br /> Refer to [Servi](Servi.md) for how to create and send a servi mission. |
|`carti_mission`	|[`carti.Mission`](Carti.md#carti_mission-cartimission)	| Carti missions are specific to the Carti robot family.<br /> Refer to [Carti](Carti.md) for how to create and send a carti mission. |

#### `BaseMission`
Use the `base_mission` field to send a mission to a Base unit. Current API version supports 2 types of base missions.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`navigate_mission`   |[`NavigateMission`](#navigate_mission-navigatemission)	| Create a base mission of type `Navigate`. |
|`navigate_auto_mission`	|[`NavigateAutoMission`](#navigate_auto_mission-navigateautomission)| Create a base mission of type `NavigateAuto`. |

#### `NavigateMission`
A mission consisting of a single, explicitly defined goal.

| Field  | Message Type | Description |
|------------|-------------| ---|
|`goal`   |[`Goal`](../LocalizationAndNavigation/#goal-goal-required)<br>`required`	| The target destination for the mission. |


#### `NavigateAutoMission`
A mission that automatically selects the **first unoccupied and unclaimed goal** from the provided list, preferring goals with lower index values.

For example, when sending a robot to one of several possible goals `[T1, T2, T3]`, it will first check if `T1` is unoccupied and unclaimed, then `T2`, and finally `T3`. If all goals are occupied (other robot is already present) or claimed (other robot is already en route to the destination), the mission will fail.

| Field  | Message Type | Description |
|------------|-------------| ---|
|`goals`   |*repeated* [`Goal`](../LocalizationAndNavigation/#goal-goal-required)<br>`required`	| The **list** of target destinations for the mission. |


##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "robot-001",
        "mission": {
          "baseMission": {
            "navigateMission": {
              "goal": {
                "pose": {
                  "xMeters": 2.5,
                  "yMeters": 3.0,
                  "headingRadians": 1.57
                }
              }
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
        "missionId": "cbd47ab1-df21-479e-9f72-677b81ab55b0"
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  The robot is already executing another mission. `CreateMission` is only valid if there is no running mission on the target robot, with its state in one of the [terminal states](#state-enum), i.e., Cancelled, Succeeded, Failed, Default |
|`INVALID_ARGUMENT`    |   The client supplied a request with invalid format. This covers sending empty requests, invalid goals, goals that do not match mission type, and other format errors. Client should update their usage to have correctly formatted requests with valid goals for the missions as defined in documentation. |
|`INTERNAL`    |   The request failed to execute due to internal error in mission system. Client should retry creating the mission.
|`DEADLINE_EXCEEDED`    |  The request was sent internally, but timed out waiting for confirmation response of request being accepted. Client should retry creating the mission. |

-----------
## AppendMission 
Appends a mission to the end of the [mission queue](../../concepts/mission.md#mission-queue).
Use this when a mission is currently running; otherwise, prefer [CreateMission](#createmission). 
Missions are executed in the order they are appended.

### Request / Response
!!! note 
    AppendMission request and response message types are the same as [CreateMission](#createmission). See [CreateMission JSON Examples](#json-request-example).

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  There is no mission in the mission queue. Client should first create the initial mission, and only use Append for queuing additional missions. |
|`INVALID_ARGUMENT`    |   The client supplied a request with invalid format. This covers sending empty requests, invalid goals, goals that do not match mission type, and other format errors. Client should update their usage to have correctly formatted requests with valid goals for the missions as defined in documentation. |
|`INTERNAL`    |   The request failed to execute due to internal error in mission system. Client should retry appending the mission. |
|`DEADLINE_EXCEEDED`    |  The request was sent internally, but timed out waiting for confirmation response of request being accepted. Client should retry appending the mission. |

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
[Command](../../concepts/mission.md#command) to update the state of an active mission.

| Field  | Message Type | Description |
|------------|-------------| ---------|
| `mission_id` | `string` <br />`required`|  The ID of the mission to control.|
| `command`    | [`Command`](#command-enum) *enum* <br />`required` | Command to update the state of an active mission. |


#### Command `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| COMMAND_UNKNOWN        | 0      | Default value. This should never be used explicitly. <br/> It means the `command` field is not set|
| COMMAND_CANCEL         | 1      | Cancel this mission.                             |
| COMMAND_PAUSE          | 2      | Pause this mission.                              |
| COMMAND_RESUME         | 3      | Resume a paused mission.                         |
| COMMAND_FINISH         | 4      | Mark the mission as completed.                   |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robot_id": "pennybot-abc123",
        "mission_command": {
          "mission_id": "f842c8ac-62de-412e-90fb-bf37022db2f4",
          "command": "COMMAND_PAUSE"
        }
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
| `FAILED_PRECONDITION` | The robot is either not on a mission, or the command is invalid for the robot's current state. For example, mission in [terminal state](#state-enum) (Cancelled, Succeeded, Failed) can't be updated. |
| `INVALID_ARGUMENT`| The client supplied a request with invalid format. This covers sending empty requests, invalid commands, incorrect mission ID, and other format errors. Client should update their usage to have correctly formatted requests with valid commands, and ensure the mission id matches the currently running mission. |
|`INTERNAL`    |   The request failed to execute due to internal error in mission system. Client should retry appending the mission. |
|`DEADLINE_EXCEEDED`    |  The request was sent internally, but timed out waiting for confirmation response of request being accepted. Client should retry appending the mission. |

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

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robot_ids": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        }
      }
    ```

### Response
This endpoint returns a stream of messages in response. <br/>
Each message includes:

##### `EventMetadata`

| Field | Message Type | Description |
|------|------|-------------|
| `timestamp` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | `int64` | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with. 

##### `MissionStates`

| Field | Message Type | Description |
|------|------|-------------|
| `missions` | *repeated* [`MissionState`](#mission_state-missionstate) | List of all missions assigned to the robot, in order from first to last assigned mission. |
| `current_mission_index` | `int32` | Index of the currently active mission in the missions list. -1 if no mission is currently active. |

#### `MissionState`

| Field | Message Type | Description |
|------|------|-------------|
| `mission_id` | `string` | Unique identifier for the mission. |
| `state` | [`State`](#state-enum) *enum* | Current lifecycle state of the mission. |
| `goals` | *repeated* [`Goal`](../LocalizationAndNavigation/#goal-goal-required) | All goals associated with the mission, <br />in the order the request was given. |
| `current_goal_index` | `int32` | Index of the currently active goal in the goals list. |
| `mission_feedback` | [`MissionFeedback`](#missionfeedback) | Latest feedback for the mission. |
| `mission_type` | [`MissionType`](#missiontype) | Type of the mission. |
| `owner` | `string` | Owner of the mission (e.g., "touchscreen", "api", etc.). |

#### State `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATE_UNKNOWN          | 0      | Default value. It means the `state` field is not returned. |
| STATE_DEFAULT          | 1      | Initial state when no mission has been run (e.g., feedback is empty).    |
| STATE_RUNNING          | 2      | The mission is actively running.                 |
| STATE_PAUSED           | 3      | The mission is paused.                          |
| STATE_CANCELED         | 4      | The mission was canceled before completion.    |
| STATE_SUCCEEDED        | 5      | The mission completed successfully.           |
| STATE_FAILED           | 6      | The mission encountered an error or failure.          |


#### MissionFeedback
Provides mission-specific runtime information.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`base_feedback` | [`BaseFeedback`](#basefeedback) | Feedback specific to Base unit mission types. |
|`servi_feedback` | [`servi.Feedback`](#servifeedback-enum) | Feedback specific to Servi missions. |
|`carti_feedback` | [`carti.Feedback`](#cartifeedback-enum) | Feedback specific to Carti missions. |

#### MissionType
Defines the different types of missions that can be executed.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`base_type` | [`BaseType`](#basetype-enum) *enum* | Base-specific mission types. |
|`servi_type` | [`servi.ServiType`](#serviservitype-enum) *enum* | Servi-specific mission types. |
|`carti_type` | [`carti.CartiType`](#carticartitype-enum) *enum* | Carti-specific mission types. |


#### BaseFeedback

| Field | Message Type | Description |
|------|------|-------------|
| `status` | [`Status`](#status-enum) *enum* | Current feedback status of the robot. |

#### servi.Feedback `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATUS_UNKNOWN         | 0      | Default value. It means `status` field is not returned. |
| STATUS_NAVIGATING      | 1      | The robot is currently navigating to its goal.|
| STATUS_ARRIVED         | 2      | The robot has arrived at a goal. |
| STATUS_DOCKING         | 3      | The robot is performing a docking maneuver.|
| STATUS_UNDOCKING       | 4      | The robot is performing an undocking maneuver.|

#### carti.Feedback `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATUS_UNKNOWN         | 0      | Default value. It means `status` field is not returned. |
| STATUS_NAVIGATING      | 1      | The robot is currently navigating to its goal.|
| STATUS_ARRIVED         | 2      | The robot has arrived at a goal. |
| STATUS_DOCKING         | 3      | The robot is performing a docking maneuver.|
| STATUS_UNDOCKING       | 4      | The robot is performing an undocking maneuver.|

#### BaseType `enum`

| Name | Number | Description |
|------|--------|-------------|
| BASE_TYPE_UNKNOWN | 0 | Default value for base type. |
| BASE_TYPE_NAVIGATE | 1 | A single navigation mission with a predefined goal. |
| BASE_TYPE_NAVIGATE_AUTO | 2 | An automated navigation mission that selects the best available goal from a list. |

#### servi.ServiType `enum`

| Name | Number | Description |
|------|--------|-------------|
| SERVI_TYPE_UNKNOWN | 0 | Default value for servi type. |
| SERVI_TYPE_SERVING | 1 | A serving mission that navigates to goals, stopping until weight is removed. |
| SERVI_TYPE_SERVING_PATROL | 2 | A serving patrol mission that continuously loops until all weight is removed. |
| SERVI_TYPE_BUSSING | 3 | A bussing mission that navigates to goals, stopping until weight is added. |
| SERVI_TYPE_BUSSING_PATROL | 4 | A bussing patrol mission that continuously loops until weight exceeds threshold. |
| SERVI_TYPE_NAVIGATE | 5 | A single navigation mission with a predefined goal. |
| SERVI_TYPE_NAVIGATE_AUTO | 6 | An automated navigation mission that selects the first available goal from a list. |

#### carti.CartiType `enum`

| Name | Number | Description |
|------|--------|-------------|
| CARTI_TYPE_UNKNOWN | 0 | Default value for carti type. |
| CARTI_TYPE_TRAVERSE | 1 | A traverse mission that follows a sequence of goals. |
| CARTI_TYPE_TRAVERSE_PATROL | 2 | A traverse patrol mission that follows a sequence of goals on loops. |
| CARTI_TYPE_NAVIGATE | 3 | A single navigation mission with a predefined goal. |
| CARTI_TYPE_NAVIGATE_AUTO | 4 | An automated navigation mission that selects the first available goal from a list. |

#### Status `enum`

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATUS_UNKNOWN         | 0      | Default value. It means `status` field is not returned. |
| STATUS_NAVIGATING      | 1      | The robot is currently navigating to its target.|

##### JSON Response Example
=== "JSON" 
    ```js
    {
      "metadata": {
        "timestamp": "2025-04-01T15:30:00Z",
        "sequenceNumber": 128
      },
      "robot_id": "pennybot-abc123",
      "mission_states": {
        "missions": [
          {
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
            },
            "mission_type": {
              "baseType": "BASE_TYPE_NAVIGATE"
            },
            "owner": "api"
          }
        ],
        "current_mission_index": 0
      }
    }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
| `INVALID_ARGUMENT`| One or more request parameters are malformed or logically incorrect. <br /> Example: Using an invalid robot ID format. |
    
-----------
## ChargeRobot
`ChargeRobot` is a special type of mission. Use this command to instruct the robot to begin charging, regardless of its current battery level. This command is only supported on robots equipped with a contact-based charging dock.
You can use [`SubscribeBatteryStatus`](RobotStatus.md#subscribebatterystatus) to monitor the charging process. 

### Request

##### robot_id `string` `required`
The ID of the robot that will receive this command.

##### JSON Request Example
=== "JSON" 
    ```js
    {
      "robot_id": "pennybot-abc123"
    }
    ```

### Response
##### **mission_id** `string`
The ID of the mission created. Since this command is a special type of mission, its execution state is also avaiable in response messages from [`SubscribeMissionStatus`](#subscribemissionstatus). 

##### JSON Response Example
=== "JSON" 
    ```js
    {
      "mission_id": "mission-xyz-001"
    }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
|`FAILED_PRECONDITION`   |  The robot is already executing a mission. The current mission must be canceled before issuing this command. |
|`INTERNAL`    |   The request failed to execute due to internal error in mission system. Client should retry creating the mission.
|`DEADLINE_EXCEEDED`    |  The request was sent internally, but timed out waiting for confirmation response of request being accepted. Client should retry creating the mission. |
