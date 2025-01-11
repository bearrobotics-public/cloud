### Message Types

#### RobotSystemInfo
| Field             | Type    | Description        |
|-------------------|---------|--------------------|
| `software_version` | string | The distribution version currently installed and running on the robot.<br>e.g., "servi-24.03". |
| `robot_family`    | [RobotFamily](#robotfamilyenum)<br>*enum* |   |
| `robot_id`        | string  | Unique identifier for the robot. e.g., "pennybot-abc123".                   |
| `display_name`    | string  | A user-friendly name for the robot, typically used for display purposes.    |

#### RobotFamilyEnum
| Name                      | Number | Description |
|---------------------------|--------|-------------|
| ROBOT_FAMILY_UNKNOWN      | 0      |             |
| ROBOT_FAMILY_SERVI        | 1      |             |
| ROBOT_FAMILY_SERVI_MINI   | 2      |             |
| ROBOT_FAMILY_SERVI_AIR    | 3      |             |
| ROBOT_FAMILY_SERVI_PLUS   | 4      |             |
| ROBOT_FAMILY_SERVI_LIFT   | 5      |             |
| ROBOT_FAMILY_CARTI_100    | 6      |             |
| ROBOT_FAMILY_CARTI_600    | 7      |             |

