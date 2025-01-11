### Message Types

#### Destination
| Field            | Type   | Description |
|------------------|--------|-------------|
| `destination_id` | string |             |

#### Zone
| Field    | Type   | Description |
|----------|--------|-------------|
| `zone_id`| string |             |

#### Position
| Field            | Type  | Description |
|------------------|-------|-------------|
| `x_meters`       | float |             |
| `y_meters`       | float |             |
| `heading_radians`| float |             |

#### Goal
`Goal` allows one of the following goals

| Field       | Type            | Description |
|-------------|-----------------|-------------|
| `destination` | [Destination](#destination) |             |
| `zone`       | [Zone](#zone)   |             |
| `position`   | [Position](#position) |             |

#### Mission
| Field             | Type                     | Description    |
|-------------------|--------------------------|----------------|
| `type`            | [Type](#missiontypeenum)<br>*enum* |      |
| `goals`           | [Goal](#goal)<br>*repeated* | The list of goals or destinations for the mission.  |
| `override_params` | [MissionParams](#missionparams) | Override parameters for the mission settings, <br> allowing specific configuration for this mission instance. |

#### MissionCommand
| Field       | Type                        | Description |
|-------------|-----------------------------|-------------|
| `mission_id`| string                      |             |
| `command`   | [Command](#missioncommandenum)<br>*enum* |             |

#### MissionParams
`MissionParams` allows one of these 2 `params`

| Field             | Type                    | Description |
|-------------------|-------------------------|-------------|
| `traverse_params` | [TraverseParams](#traverseparams) |             |
| `loop_params`     | [LoopParams](#loopparams) |             |

#### LoopParams
| Field  | Type                    | Description |
|--------|-------------------------|-------------|
| `mode` | [Mode](#loopparamsmodeenum)<br>*repeated* |             |

#### TraverseParams
| Field  | Type                    | Description |
|--------|-------------------------|-------------|
| `mode` | [Mode](#loopparamsmodeenum)<br>*repeated* |             |

#### MissionState
| Field                | Type                                             | Description                                  |
|----------------------|--------------------------------------------------|----------------------------------------------|
| `mission_id`         | string                                           |                                              |
| `state`              | [State](#missionstateenum)<br>*enum*             |                                              |
| `goals`              | [Goal](#goal)<br>*repeated*                      | All goals for a given mission.               |
| `current_goal_index` | int32                                            |                                              |
| `navigation_status`  | [NavigationStatus](#missionstatenavigationstatusenum)<br>*enum* |                                              |
| `feedback`           | Any                                              |                                              |

#### LoopParamsModeEnum
| Name           | Number | Description |
|----------------|--------|-------------|
| MODE_UNKNOWN   | 0      |             |
| MODE_DEFAULT   | 1      |             |
| MODE_BUSSING   | 2      |             |

#### MissionCommandEnum
| Name             | Number | Description |
|------------------|--------|-------------|
| COMMAND_UNKNOWN  | 0      |             |
| COMMAND_CANCEL   | 1      |             |
| COMMAND_PAUSE    | 2      |             |
| COMMAND_RESUME   | 3      |             |
| COMMAND_FINISH   | 4      |             |

#### MissionStateEnum
| Name             | Number | Description |
|------------------|--------|-------------|
| STATE_UNKNOWN    | 0      |             |
| STATE_DEFAULT    | 1      | Initial state when no mission has been run (e.g., empty feedback). |
| STATE_RUNNING    | 2      |             |
| STATE_PAUSED     | 3      |             |
| STATE_CANCELED   | 4      |             |
| STATE_SUCCEEDED  | 5      |             |
| STATE_FAILED     | 6      |             |

#### MissionTypeEnum
| Name               | Number | Description                                                                |
|--------------------|--------|----------------------------------------------------------------------------|
| TYPE_UNKNOWN       | 0      | Default value, indicates an unknown or unspecified mission type.           |
| TYPE_ONEOFF        | 1      | A single-goal mission.                                                     |
| TYPE_ONEOFF_AUTO   | 2      | An automated single-goal mission that selects the best available goal.     |
| TYPE_TRAVERSE      | 3      | A mission involving multiple destinations until a condition, such as weight limit, is met. |
| TYPE_LOOP          | 4      | A mission that repeatedly visits multiple destinations until a condition, such as weight limit, is met. |
| TYPE_WAIT          | 5      | A mission that remains at a specific location until triggered by an external event. |

#### MissionStateNavigationStatusEnum
| Name                  | Number | Description                                                                 |
|-----------------------|--------|-----------------------------------------------------------------------------|
| NAVIGATION_STATUS_UNKNOWN | 0      | Default value, indicates an unknown or undefined navigation status.     |
| NAVIGATION_STATUS_FINISHED| 1      | Indicates the robot has successfully arrived at its goal.               |
| NAVIGATION_STATUS_FAILED  | 2      | Indicates the robot failed to reach its goal.                           |
| NAVIGATION_STATUS_STUCK   | 3      | Indicates the robot is temporarily stuck but has not yet failed.        |
| NAVIGATION_STATUS_DOCKING | 4      | Indicates the robot is in the process of docking.                       |
| NAVIGATION_STATUS_UNDOCKING | 5    | Indicates the robot is undocking.                                       |
| NAVIGATION_STATUS_NAVIGATING | 6   | Indicates the robot is navigating to a destination.                     |
