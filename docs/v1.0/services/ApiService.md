
### APIService
APIService defines the control, navigation, monitoring, and fleet management
interface for robots. This includes mission orchestration, localization,
pose tracking, battery monitoring, and map retrieval.

=== Mission Commands =====================================================

Missions are atomic units of behavior that define a robot's high-level actions.
This API enables users to control robot behavior—from simple navigation
tasks to complex, conditional workflows.
##### CreateMission
- **Request Type:** [CreateMissionRequest](#createmissionrequest)
- **Response Type:** [CreateMissionResponse](#createmissionresponse)
- **Description:**
  Creates a new mission of a specified type.<br>This call will fail if:
- The robot is already executing another mission.
- The requested mission is not compatible with the robot's current state.
##### AppendMission
- **Request Type:** [AppendMissionRequest](#appendmissionrequest)
- **Response Type:** [AppendMissionResponse](#appendmissionresponse)
- **Description:**
  Appends a mission to the end of the mission queue.<br>Use this when a mission is currently running; otherwise, prefer CreateMission.
Missions are executed in the order they are appended.
##### UpdateMission
- **Request Type:** [UpdateMissionRequest](#updatemissionrequest)
- **Response Type:** [UpdateMissionResponse](#updatemissionresponse)
- **Description:**
  Issues a command to control or update the current mission (e.g., pause, cancel).<br>This call will fail if:
- The robot is not on the specified mission.
- The command is invalid for the robot's current state.
##### ChargeRobot
- **Request Type:** [ChargeRobotRequest](#chargerobotrequest)
- **Response Type:** [ChargeRobotResponse](#chargerobotresponse)
- **Description:**
  Instructs the robot to begin charging, regardless of its current battery level.<br>This call will fail if:
- The robot is already executing a mission.
  The current mission must be canceled before issuing this command.<br>Notes:
- This command is only supported on robots equipped with a contact-based charging dock.
  Robots without a compatible dock will ignore the request.
- The robot must have the multi-robot coordination flag enabled for this command to be accepted.
- Use `SubscribeBatteryStatus` to monitor the charging process.
##### SubscribeMissionStatus
- **Request Type:** [SubscribeMissionStatusRequest](#subscribemissionstatusrequest)
- **Response Type:** [SubscribeMissionStatusResponse](#subscribemissionstatusresponse)
- **Description:**
  Subscribes to updates on the robot's mission status.<br>Upon subscription:
- The latest known mission status is sent immediately.
- Subsequent updates are streamed as the status changes.
##### GetCurrentMap
- **Request Type:** [GetCurrentMapRequest](#getcurrentmaprequest)
- **Response Type:** [GetCurrentMapResponse](#getcurrentmapresponse)
- **Description:**
  Retrieves the current map used by the robot.<br>The returned map includes annotations and destinations, which can be used
in mission goals, localization, and navigation.
##### LocalizeRobot
- **Request Type:** [LocalizeRobotRequest](#localizerobotrequest)
- **Response Type:** [LocalizeRobotResponse](#localizerobotresponse)
- **Description:**
  Localizes the robot to a known pose or destination.<br>If the request is accepted, subscribe to `SubscribeLocalizationStatus`
to track localization progress.
##### SubscribeLocalizationStatus
- **Request Type:** [SubscribeLocalizationStatusRequest](#subscribelocalizationstatusrequest)
- **Response Type:** [SubscribeLocalizationStatusResponse](#subscribelocalizationstatusresponse)
- **Description:**
  Subscribes to the robot’s localization status.<br>Upon subscription:
- The latest localization status is sent immediately.
- Status updates are streamed while localization is active.
##### SubscribeRobotPose
- **Request Type:** [SubscribeRobotPoseRequest](#subscriberobotposerequest)
- **Response Type:** [SubscribeRobotPoseResponse](#subscriberobotposeresponse)
- **Description:**
  Subscribes to the robot's pose estimates at a regular frequency. (~10Hz)<br>Current implementation supports up to 5 robots, if more than 5 robots are
requested, only the first 5 robots will be processed.<br>Use this to track the robot's estimated position in real time.
##### SubscribeBatteryStatus
- **Request Type:** [SubscribeBatteryStatusRequest](#subscribebatterystatusrequest)
- **Response Type:** [SubscribeBatteryStatusResponse](#subscribebatterystatusresponse)
- **Description:**
  Subscribes to battery status updates for the robot.<br>Upon subscription:
- The latest battery state is sent immediately.
- Updates are streamed whenever the state changes.
##### SubscribeRobotStatus
- **Request Type:** [SubscribeRobotStatusRequest](#subscriberobotstatusrequest)
- **Response Type:** [SubscribeRobotStatusResponse](#subscriberobotstatusresponse)
- **Description:**
  Subscribes to the robot's connectivity and operational state.<br>Upon subscription:
- The current robot status is sent immediately.
- Updates are streamed as the robot's state changes.
##### ListRobotIDs
- **Request Type:** [ListRobotIDsRequest](#listrobotidsrequest)
- **Response Type:** [ListRobotIDsResponse](#listrobotidsresponse)
- **Description:**
  Returns a list of robot IDs the user has access to, filtered by optional criteria.<br>The list includes all known robots, regardless of their current connection status.
##### SubscribeTrayStatuses
- **Request Type:** [SubscribeTrayStatusesRequest](#subscribetraystatusesrequest)
- **Response Type:** [SubscribeTrayStatusesResponse](#subscribetraystatusesresponse)
- **Description:**
  Subscribes to the robot’s tray status updates.<br>Only applicable for tray-equipped robots (e.g., Servi, Servi Plus).<br>Upon subscription:
- The latest known tray states are sent immediately.
- Updates are streamed when any tray state changes.<br>Notes:
- Weight changes are reported with 10g precision.
- Robots without weight sensors will report UNKNOWN state and omit weight data.

### Message Types

##### AppendMissionRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |
| `mission` | Mission |  |

##### AppendMissionResponse

| Name | Type | Description |
|------|------|-------------|
| `mission_id` | string |  |

##### ChargeRobotRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |

##### ChargeRobotResponse

| Name | Type | Description |
|------|------|-------------|
| `mission_id` | string |  |

##### CreateMissionRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |
| `mission` | Mission |  |

##### CreateMissionResponse

| Name | Type | Description |
|------|------|-------------|
| `mission_id` | string |  |

##### GetCurrentMapRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |

##### GetCurrentMapResponse

| Name | Type | Description |
|------|------|-------------|
| `map` | Map |  |

##### ListRobotIDsRequest

| Name | Type | Description |
|------|------|-------------|
| `filter` | RobotFilter |  |

##### ListRobotIDsResponse

| Name | Type | Description |
|------|------|-------------|
| `total_robots` | int32 |  |
| `robot_ids` | string |  |

##### LocalizeRobotRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |
| `goal` | LocalizationGoal |  |

##### LocalizeRobotResponse
- *(No fields defined)*

##### SubscribeBatteryStatusRequest

| Name | Type | Description |
|------|------|-------------|
| `selector` | RobotSelector |  |

##### SubscribeBatteryStatusResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `robot_id` | string |  |
| `battery_state` | BatteryState |  |

##### SubscribeLocalizationStatusRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |

##### SubscribeLocalizationStatusResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `localization_state` | LocalizationState |  |

##### SubscribeMissionStatusRequest

| Name | Type | Description |
|------|------|-------------|
| `selector` | RobotSelector |  |

##### SubscribeMissionStatusResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `robot_id` | string |  |
| `mission_state` | MissionState |  |

##### SubscribeRobotPoseRequest

| Name | Type | Description |
|------|------|-------------|
| `selector` | RobotSelector |  |

##### SubscribeRobotPoseResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `poses` | PosesEntry |  |

##### PosesEntry

| Name | Type | Description |
|------|------|-------------|
| `key` | string |  |
| `value` | Pose |  |

##### SubscribeRobotStatusRequest

| Name | Type | Description |
|------|------|-------------|
| `selector` | RobotSelector |  |

##### SubscribeRobotStatusResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `robot_id` | string |  |
| `robot_states` | RobotState |  |

##### SubscribeTrayStatusesRequest

| Name | Type | Description |
|------|------|-------------|
| `selector` | RobotSelector |  |

##### SubscribeTrayStatusesResponse

| Name | Type | Description |
|------|------|-------------|
| `metadata` | EventMetadata |  |
| `tray_states` | TrayStatesEntry |  |

##### TrayStatesEntry

| Name | Type | Description |
|------|------|-------------|
| `key` | string |  |
| `value` | TrayStates |  |

##### UpdateMissionRequest

| Name | Type | Description |
|------|------|-------------|
| `robot_id` | string |  |
| `mission_command` | MissionCommand |  |

##### UpdateMissionResponse
- *(No fields defined)*