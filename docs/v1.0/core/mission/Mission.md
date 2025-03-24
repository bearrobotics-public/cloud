

### Message Types

##### Mission

| Name | Type | Description |
|------|------|-------------|
| `navigate_mission` | NavigateMission | A simple mission with a predefined goal. |
| `navigate_auto_mission` | NavigateAutoMission | A mission that automatically selects the optimal goal from a list. |
| `servi_mission` | ServiMission | Servi and Carti missions are specific to their respective robot families. |
| `carti_mission` | CartiMission |  |

##### MissionCommand

| Name | Type | Description |
|------|------|-------------|
| `id` | string | ID of the mission to control. |
| `command` | Command |  |

##### NavigateAutoMission

| Name | Type | Description |
|------|------|-------------|
| `destinations` | Destination |  |

##### NavigateMission

| Name | Type | Description |
|------|------|-------------|
| `destination` | Destination |  |