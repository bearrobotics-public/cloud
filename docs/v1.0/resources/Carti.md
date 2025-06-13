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
