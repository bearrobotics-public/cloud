### Message Types

#### LocalizationGoal
| Field | Type                             | Description |
|-------|----------------------------------|-------------|
| `pose`| [Pose](../robot/Robot.md/#pose) |             |

#### LocalizationState
| Field  | Type              | Description |
|--------|-------------------|-------------|
| `state`| [State](#stateenum)<br>*enum* |             |

#### StateEnum
| Name             | Number | Description                                               |
|------------------|--------|-----------------------------------------------------------|
| STATE_UNKNOWN    | 0      |                                                           |
| STATE_PREEMPTED  | 1      | Happens when the localization process is preempted before completion. |
| STATE_FAILED     | 2      |                                                           |
| STATE_SUCCEEDED  | 3      |                                                           |
| STATE_LOCALIZING | 4      | The robot is actively performing localization.            |
