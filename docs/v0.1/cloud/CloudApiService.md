# Cloud API Service
The definition of Bear Cloud API service.

### Fleet Management
#### ListRobotIDs
- **Request Type:** [ListRobotIDsRequest](#listrobotidsrequest)
- **Response Type:** [ListRobotIDsResponse](#listrobotidsresponse)
- **Description:**
  List all robot IDs that you have permissions for and satisfies the given
filter options regardless of robot status.

### Map
#### GetCurrentMapContent
- **Request Type:** [GetCurrentMapContentRequest](#getcurrentmapcontentrequest)
- **Response Type:** [GetCurrentMapContentResponse](#getcurrentmapcontentresponse)
- **Description:**
  Retrieve the current map content data, which is loaded on the robot.
#### GetLocation
- **Request Type:** [GetLocationRequest](#getlocationrequest)
- **Response Type:** [GetLocationResponse](#getlocationresponse)
- **Description:**
  Retrieve the current location data to which the robot is connected.<br>If the robot is offline, it uses the cached Location data.
#### GetMap
- **Request Type:** [GetMapRequest](#getmaprequest)
- **Response Type:** [GetMapResponse](#getmapresponse)
- **Description:**
  Retrieve the map corresponding to a given map_id.<br>If offline, it uses the cached MapData data.
#### GetMapAnnotation
- **Request Type:** [GetMapAnnotationRequest](#getmapannotationrequest)
- **Response Type:** [GetMapAnnotationResponse](#getmapannotationresponse)
- **Description:**
  Retrieve annotation data for a specified annotation_id.<br>If offline, it uses the cached Annotation data.
#### GetMapData
- **Request Type:** [GetMapDataRequest](#getmapdatarequest)
- **Response Type:** [GetMapDataResponse](#getmapdataresponse)
- **Description:**
  Retrieve map data for a specified map_data_id.<br>If offline, it uses the cached MapData data.
#### SwitchMap
- **Request Type:** [SwitchMapRequest](#switchmaprequest)
- **Response Type:** [SwitchMapResponse](#switchmapresponse)
- **Description:**
  Switch the current map to a specified map.<br>The request should specify a floor level and section index to be used.
Returns the map_id of the switched map.

### Mission
#### AppendMission
- **Request Type:** [AppendMissionRequest](#appendmissionrequest)
- **Response Type:** [AppendMissionResponse](#appendmissionresponse)
- **Description:**
  Append the given mission to the end of the queue.
The mission will be added in the order it is received.
#### ChargeRobot
- **Request Type:** [ChargeRobotRequest](#chargerobotrequest)
- **Response Type:** [ChargeRobotResponse](#chargerobotresponse)
- **Description:**
  Create a mission to go charge a robot regardless of battery state.<br>The call will fail if the robot is already on a different mission,
which needs to be canceled before the robot can be charged.
#### CreateMission
- **Request Type:** [CreateMissionRequest](#createmissionrequest)
- **Response Type:** [CreateMissionResponse](#createmissionresponse)
- **Description:**
  Create a mission for a given type.<br>The call will fail if the robot cannot go on the requested mission.
#### SubscribeMissionStatus
- **Request Type:** [SubscribeMissionStatusRequest](#subscribemissionstatusrequest)
- **Response Type:** [SubscribeMissionStatusResponse](#subscribemissionstatusresponse)
- **Description:**
  Subscribe to robot's mission status.<br>Upon subscription, the server immediately sends the latest known
mission status, followed by updates whenever the mission status changes.
#### UpdateMission
- **Request Type:** [UpdateMissionRequest](#updatemissionrequest)
- **Response Type:** [UpdateMissionResponse](#updatemissionresponse)
- **Description:**
  Update the specified mission with the given command.<br>The call will fail if the robot is not on the specified mission
or cannot execute the command.
#### LocalizeRobot
- **Request Type:** [LocalizeRobotRequest](#localizerobotrequest)
- **Response Type:** [LocalizeRobotResponse](#localizerobotresponse)
- **Description:**
  Localize the robot to a localization goal.<br>If the goal is accepted, subcribe to SubscribeLocalizationStatus to get the
localization status.
#### SetRobotPose
- **Request Type:** [SetRobotPoseRequest](#setrobotposerequest)
- **Response Type:** [SetRobotPoseResponse](#setrobotposeresponse)
- **Description:**
  Manually set the robot pose given a pose on the map and a covariance matrix.
#### SubscribeLocalizationStatus
- **Request Type:** [SubscribeLocalizationStatusRequest](#subscribelocalizationstatusrequest)
- **Response Type:** [SubscribeLocalizationStatusResponse](#subscribelocalizationstatusresponse)
- **Description:**
  Subscribe to the robot's localization status.<br>Upon subscription, the latest known localization status will be sent.
If the robot is actively localizing, statuses will be published upon changes.
#### SubscribeRobotPose
- **Request Type:** [SubscribeRobotPoseRequest](#subscriberobotposerequest)
- **Response Type:** [SubscribeRobotPoseResponse](#subscriberobotposeresponse)
- **Description:**
  Subscribe to the robot's pose.<br>Upon subscription, the server provides regular updates (5Hz) of the
robot's estimated position.

### Settings
#### GetProfile
- **Request Type:** [GetProfileRequest](#getprofilerequest)
- **Response Type:** [GetProfileResponse](#getprofileresponse)
- **Description:**
  Retrieve user-defined profile with customizable settings for a profile_id.
#### GetRobotProfiles
- **Request Type:** [GetRobotProfilesRequest](#getrobotprofilesrequest)
- **Response Type:** [GetRobotProfilesResponse](#getrobotprofilesresponse)
- **Description:**
  Retrieve a set of user-defined profiles for a specific robot_id.
#### GetSoundFileUploadURL
- **Request Type:** [GetSoundFileUploadURLRequest](#getsoundfileuploadurlrequest)
- **Response Type:** [GetSoundFileUploadURLResponse](#getsoundfileuploadurlresponse)
- **Description:**
  Retrieve the sound file upload URL for specific sound_id.
#### ListProfileIds
- **Request Type:** [ListProfileIdsRequest](#listprofileidsrequest)
- **Response Type:** [ListProfileIdsResponse](#listprofileidsresponse)
- **Description:**
  List all profile IDs for a specific location_id.

### Robot Status
#### SubscribeBatteryStatus
- **Request Type:** [SubscribeBatteryStatusRequest](#subscribebatterystatusrequest)
- **Response Type:** [SubscribeBatteryStatusResponse](#subscribebatterystatusresponse)
- **Description:**
  Subscribe to the robots' battery status.<br>Upon subscription, the server immediately sends the latest known
battery status, followed by updates whenever the battery status changes.
#### SubscribeRobotStatus
- **Request Type:** [SubscribeRobotStatusRequest](#subscriberobotstatusrequest)
- **Response Type:** [SubscribeRobotStatusResponse](#subscriberobotstatusresponse)
- **Description:**
  Subscribe to connection and operation state of the robot<br>Upon subscription, the server immediately sends the latest known operation
status, followed by updates whenever the operation status changes.
#### SubscribeTrayStatuses
- **Request Type:** [SubscribeTrayStatusesRequest](#subscribetraystatusesrequest)
- **Response Type:** [SubscribeTrayStatusesResponse](#subscribetraystatusesresponse)
- **Description:**
  Subscribe for high level tray status.<br>This is only available for robots with trays (e.g. Servi, Plus).
Upon subscription, the latest known tray status will be sent, followed by
snapshot updates of all tray states when any state is updated.
Weight changes are updated at a 10g granularity threshold. Robots with
trays that does not have weight sensor will have an UNKNOWN load state and
no weight data streamed.

### Robot System
#### GetRobotSystemInfo
- **Request Type:** [GetRobotSystemInfoRequest](#getrobotsysteminforequest)
- **Response Type:** [GetRobotSystemInfoResponse](#getrobotsysteminforesponse)
- **Description:**
  Get the overall robot system information.<br>When called, the server returns robot system information. The system info
tends to be static and does not change often.

## Message Types

##### AppendMissionRequest
  | Field      | Type                          | Description |
  | -----------| ------------------------------|-------------|
  | `robot_id` | string  | |
  | `mission`  | [Mission](../robot/Mission.md#mission) | |
  
##### AppendMissionResponse
| Field        | Type   | Description |
|--------------|--------|-------------|
| `mission_id` | string |             |

##### ChargeRobotRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### ChargeRobotResponse
| Field        | Type   | Description |
|--------------|--------|-------------|
| `mission_id` | string |             |

##### CreateMissionRequest
| Field      | Type                          | Description |
|------------|-------------------------------|-------------|
| `robot_id` | string                        |             |
| `mission`  | [Mission](../robot/Mission.md/#mission) | |

##### CreateMissionResponse
| Field        | Type   | Description |
|--------------|--------|-------------|
| `mission_id` | string |             |

##### GetCurrentMapContentRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### GetCurrentMapContentResponse
| Field         | Type                                | Description |
|---------------|-------------------------------------|-------------|
| `map_content` | [MapContent](../location/Map.md/#mapcontent) | |

##### GetLocationRequest
| Field         | Type   | Description |
|---------------|--------|-------------|
| `location_id` | string | The unique identifier of the location.<br>The ID is typically a string of uppercase letters and numbers.<br>Example: "QSVS". |

##### GetLocationResponse
| Field      | Type                              | Description |
|------------|-----------------------------------|-------------|
| `location` | [Location](../location/Location.md/#location) | Information about the specified location, as represented by a<br>location.Location message.<br>This includes all relevant details about the location, including<br>display name, floor, and section info. |

##### GetMapAnnotationRequest
| Field           | Type   | Description |
|-----------------|--------|-------------|
| `annotation_id` | string |             |

##### GetMapAnnotationResponse
| Field        | Type                                    | Description |
|--------------|-----------------------------------------|-------------|
| `annotation` | [Annotation](../location/Annotation.md/#annotation) | |

##### GetMapDataRequest
| Field        | Type   | Description |
|--------------|--------|-------------|
| `map_data_id` | string |             |

##### GetMapDataResponse
| Field       | Type                              | Description |
|-------------|-----------------------------------|-------------|
| `map_data`  | [MapData](../location/Map.md#mapdata) | Information about the map data which includes all relevant details about<br>the map data, especially the map. |

##### GetMapRequest
| Field    | Type   | Description |
|----------|--------|-------------|
| `map_id` | string |             |

##### GetMapResponse
| Field  | Type                       | Description |
|--------|----------------------------|-------------|
| `map`  | [Map](../location/Map.md#map) | Contains all relevant information about the requested map,<br>including its metadata and configuration details. |

##### GetProfileRequest
| Field         | Type   | Description |
|---------------|--------|-------------|
| `profile_id`  | string |             |

##### GetProfileResponse
| Field     | Type                                | Description |
|-----------|-------------------------------------|-------------|
| `profile` | [Profile](../cloud/Profiles.md/#profile) |             |

##### GetRobotProfilesRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### GetRobotProfilesResponse
| Field      | Type                                | Description |
|------------|-------------------------------------|-------------|
| `profiles` | [Profile](../cloud/Profiles.md/#profile) |             |

##### GetRobotSystemInfoRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### GetRobotSystemInfoResponse
| Field               | Type                                       | Description |
|---------------------|--------------------------------------------|-------------|
| `robot_system_info` | [RobotSystemInfo](../cloud/RobotSettings.md/#robotsysteminfo) | robot_system_info contains ID, display name, family, <br> and software version of the robot. |

##### GetSoundFileUploadURLRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `sound_id` | string |             |

##### GetSoundFileUploadURLResponse
| Field       | Type   | Description |
|-------------|--------|-------------|
| `upload_url` | string |             |

##### ListProfileIdsRequest
| Field         | Type   | Description |
|---------------|--------|-------------|
| `location_id` | string |             |

##### ListProfileIdsResponse
| Field        | Type   | Description |
|--------------|--------|-------------|
| `profile_ids` | string |             |

##### ListRobotIDsRequest
| Field      | Type                                    | Description |
|------------|-----------------------------------------|-------------|
| `filter`   | [RobotFilter](../cloud/Config.md/#robotfilter) |             |

##### ListRobotIDsResponse
| Field          | Type   | Description |
|----------------|--------|-------------|
| `total_robots` | int32  |             |
| `robot_ids`    | string | This might not have all the robot IDs if there are too many.<br>It will have all the robot IDs if the number of robot_ids is the same as<br>total_robots. |

##### LocalizeRobotRequest
| Field      | Type                                        | Description |
|------------|--------------------------------------------|-------------|
| `robot_id` | string                                     |             |
| `goal`     | [LocalizationGoal](../robot/Localization.md/#localizationgoal) |             |

##### LocalizeRobotResponse
- *(No fields defined)*

##### SetRobotPoseRequest
| Field                  | Type                                     | Description |
|------------------------|------------------------------------------|-------------|
| `robot_id`             | string                                  |             |
| `pose_with_covariance` | [PoseWithCovariance](../robot/Robot.md/#posewithcovariance) |             |

##### SetRobotPoseResponse
- *(No fields defined)*

##### SubscribeBatteryStatusRequest
| Field      | Type                                    | Description |
|------------|-----------------------------------------|-------------|
| `selector` | [RobotSelector](../cloud/Config.md/#robotselector) |             |

##### SubscribeBatteryStatusResponse
| Field          | Type                                | Description |
|----------------|-------------------------------------|-------------|
| `metadata`     | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `robot_id`     | string                              |             |
| `battery_state` | BatteryState                       |             |

##### SubscribeLocalizationStatusRequest
| Field      | Type                                    | Description |
|------------|-----------------------------------------|-------------|
| `selector` | [RobotSelector](../cloud/Config.md/#robotselector) |             |

##### SubscribeLocalizationStatusResponse
| Field                | Type                                        | Description |
|----------------------|---------------------------------------------|-------------|
| `metadata`           | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `robot_id`           | string                                     |             |
| `localization_state` | [LocalizationState](../robot/Localization.md/#localizationstate) |             |

##### SubscribeMissionStatusRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### SubscribeMissionStatusResponse
| Field           | Type                                | Description |
|-----------------|-------------------------------------|-------------|
| `metadata`      | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `mission_state` | [MissionState](../robot/Mission.md/#missionstate) |             |

##### SubscribeRobotPoseRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### SubscribeRobotPoseResponse
| Field      | Type                                | Description |
|------------|-------------------------------------|-------------|
| `metadata` | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `pose`     | Pose                                |             |

##### SubscribeRobotStatusRequest
| Field      | Type                                    | Description |
|------------|-----------------------------------------|-------------|
| `selector` | [RobotSelector](../cloud/Config.md/#robotselector) |             |

##### SubscribeRobotStatusResponse
| Field        | Type                                | Description |
|--------------|-------------------------------------|-------------|
| `metadata`   | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `robot_state` | [RobotState](../cloud/RobotStatus.md#robotstate) | Robot state is not available when robot connection shows disconnected. |

##### SubscribeTrayStatusesRequest
| Field      | Type   | Description |
|------------|--------|-------------|
| `robot_id` | string |             |

##### SubscribeTrayStatusesResponse
| Field        | Type                               | Description |
|--------------|------------------------------------|-------------|
| `metadata`   | [EventMetadata](../common/Annotations.md/#eventmetadata) |             |
| `tray_states` | [TrayState](../robot/Status.md/#traystate) | State of enabled trays, ordered from the top-most tray on the robot to the<br>bottom. This state is only available for robots with trays. |

##### SwitchMapRequest
| Field          | Type   | Description |
|----------------|--------|-------------|
| `robot_id`     | string |             |
| `floor_level`  | int32  | Positive integer floor_level is the first key used to index the map dict.<br> SwitchMap will raise an exception if there is no matching map.<br>Floor level begins at 1. |
| `section_index` | int32 | Non-negative integer section_index is the second key used to <br> index the map dict.<br> SwitchMap will raise an exception if there is no matching map.<br> Section index begins at 0. |

##### SwitchMapResponse
| Field    | Type   | Description |
|----------|--------|-------------|
| `map_id` | string |             |

##### UpdateMissionRequest
| Field             | Type                                    | Description |
|-------------------|-----------------------------------------|-------------|
| `robot_id`        | string                                 |             |
| `mission_command` | [MissionCommand](../robot/Mission.md/#missioncommand) |             |

##### UpdateMissionResponse
- *(No fields defined)*
