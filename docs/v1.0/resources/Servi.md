These endpoints and their message types are only available for the Servi robot family. Attempting to run a Servi commands on a non Servi robot will result in an INVALID_ARGUMENT error.


------------
## CreateMission 
Use the shared `CreateMission` endpoint to send missions for Servi robots. Servi-specific missions must be sent using the appropriate request message format. <br/>

!!! Note
    When sending a Servi mission, `servi.Feedback` is returned in [`SubscribeMissionStatus`](Mission.md#subscribemissionstatus) response message.
    
    ##### servi.Feedback `enum`

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
|`servi_mission`	|[`servi.Mission`](#servi_mission-servimission) | Servi missions are specific to the Servi robot family. |
|`carti_mission`	|[`carti.Mission`](Carti.md#carti_mission-cartimission)	| Carti missions are specific to the Carti robot family.<br /> Refer to [Carti](Carti.md) for how to create and send a carti mission. |


##### servi_mission `servi.Mission`
Use the field `servi_mission` to create and send a servi mission. Current API version supports 4 types of Servi mission.

| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
|`bussing_mission`   |[`BussingMission`](#bussing_mission-bussingmission)	| Create a servi mission of type `Bussing`. |
|`bussing_patrol_mission`	|[`BussingPatrolMission`](#bussing_patrol_mission-bussingpatrolmission)| Create a servi mission of type `BussingPatrol`. |
|`delivery_mission`   |[`DeliveryMission`](#delivery_mission-deliverymission)	| Create a servi mission of type `Delivery`. |
|`delivery_patrol_mission`	|[`DeliveryPatrolMission`](#delivery_patrol_mission-deliverypatrolmission)| Create a servi mission of type `DeliveryPatrol`. |

##### bussing_mission `BussingMission`
A mission that navigates to one or more goals, stopping at each for a set amount of time or until some weight is added.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`BussingParams` <br />`optional`|  ***There is no param defined in this API version.*** |

##### bussing_patrol_mission `BussingPatrolMission`
A mission that continuously loops through goals, stopping at each for a set amount of time or until weight exceeds a threshold.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`BussingPatrolParams` <br />`optional`|  ***There is no param defined in this API version.*** |

##### delivery_mission `DeliveryMission`
A mission that navigates to one or more goals, stopping at each for a set amount of time or until some weight is removed.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`DeliveryParams` <br />`optional`|  ***There is no param defined in this API version.*** |

##### delivery_patrol_mission `DeliveryPatrolMission`
A that continuously loops through goals, stopping at each for a set amount of time or until all weight is removed.

| Field | Message Type | Description |
|------|------|-------------|
|`goals`| *repeated* `Goal` <br />`required`| a list of [`Goal`](Mission.md#goal-goal-required) |
|`params`|`DeliveryPatrolParams` <br />`optional`|  ***There is no param defined in this API version.*** |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "pennybot-456efg",
        "mission": {
          "serviMission": {
            "bussingMission": {
              "goals": [
                { "destinationId": "pickup-table-1" },
                { "destinationId": "dropoff-station-3" }
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
## SubscribeTrayStatuses
Subscribes to the robot’s tray status updates. <br />
Upon subscription, the latest known tray states are sent immediately. Updates are streamed when any tray state changes.

!!! Note ""
    Weight changes are reported with 10g precision.

### Request
##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. <br/>
 It supports selection by a list of robot IDs **or** all robots at a given location.

1. `robot_ids` `RobotIDs` <br/>
  Selects robots by their specific IDs. <br/>
  Example: `["pennybot-123abc", "pennybot-abc123"]` <br/>

2. `location_id` `string` <br/>
  Selects all robots at the specified location. <br/>

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robotIds": {
            "ids": [
              "pennybot-456efg"
            ]
          }
        }
      }
    ```
### Response
##### tray_states `map<string, TrayStatesWithMetadata>`
A mapping of tray states reported by individual robots. Each entry pairs a robot ID (key) with its corresponding tray states.

##### TrayStatesWithMetadata
| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata | Metadata associated with the tray states. |
| `tray_states` | TrayStates | The tray states reported by the robot. |

##### TrayStates `TrayState` `repeated`
State of enabled trays, ordered from the top-most tray on the robot to the bottom.

##### TrayState
Represents the state of a single tray.

| Field | Message Type | Description |
|------|------|-------------|
| `tray_name`  |string|  Unique string name for the given tray. <br /> e.g. "top", "middle", "bottom" <br /> See [illustrations](#tray-configurations-on-different-servi-models) for tray configurations on different robot models.
| `load_state` | LoadState *enum* |  |
| `weight_kg` | float | Weight on the tray in kilograms. Minimum precision is 10g. |
| `load_ratio` | float | Ratio of the current load to the tray’s maximum load capacity.<br />This value may exceed 1.0 if the tray is overloaded.<br /> Caveats:<br>- If the maximum load is misconfigured (e.g., set to 0.0),<br />  this value may return NaN. |

##### LoadState `enum`
| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| LOAD_STATE_UNKNOWN          | 0      | Default value. It means the `load_state` field is not returned. |
| LOAD_STATE_LOADED          | 1      | The tray has a valid load.   |
| LOAD_STATE_EMPTY          | 2      | The tray is empty.             |
| LOAD_STATE_OVERLOADED           | 3      | The tray is carrying more than its maximum capacity.                     |

### Errors
| ErrorCode  | Description |
|------------|-------------|
|`INVALID_ARGUMENT`   | This command is sending to is not a Servi family robot. |
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|

##### JSON Response Example
=== "JSON"
    ```js
    {
      "trayStates": {
        "pennybot-456efg": {
          "metadata": {
            "timestamp": "2025-04-01T16:00:00Z",
            "sequenceNumber": 105
          },
          "trayStates": [
            {
              "trayName": "top",
              "loadState": "LOAD_STATE_OVERLOADED",
              "weightKg": 8.1,
              "loadRatio": 1.18
            },
            {
              "trayName": "middle",
              "loadState": "LOAD_STATE_LOADED",
              "weightKg": 2.3,
              "loadRatio": 0.76
            },
            {
              "trayName": "bottom",
              "loadState": "LOAD_STATE_EMPTY",
              "weightKg": 0,
              "loadRatio": 0
            }
          ]
        }
      }
    }
    ```

##### Tray configurations on different Servi models
###### Servi Plus
![Servi Plus](../../assets/servi-plus.png)

###### Servi
![Servi](../../assets/servi.png)

###### Servi Mini
![Servi Mini](../../assets/servi-mini.png)
