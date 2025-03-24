

### Message Types

##### BaseFeedback

| Name | Type | Description |
|------|------|-------------|
| `status` | Status | Current status of the robot. |

##### MissionState

| Name | Type | Description |
|------|------|-------------|
| `id` | string | Unique identifier for the mission. |
| `state` | State |  |
| `destinations` | Destination | All destinations associated with the mission, in the order the request was given. |
| `current_goal_index` | int32 | Index of the currently active goal in the goals list. |
| `feedback` | Feedback | Latest feedback for the mission. |

##### Feedback

| Name | Type | Description |
|------|------|-------------|
| `base_feedback` | BaseFeedback | Generic feedback applicable to base mission types. e,g., NavigateMission. |
| `servi_feedback` | Feedback | Feedback specific to Servi missions. |
| `carti_feedback` | Feedback | Feedback specific to Carti missions. |