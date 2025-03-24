

### Message Types

##### RobotFilter

| Name | Type | Description |
|------|------|-------------|
| `location_id` | string | An empty location_id matches all locations. |
| `distributor_id` | string | An empty distributor_id defaults to the distributor assigned to the API key user. |

##### RobotSelector

| Name | Type | Description |
|------|------|-------------|
| `robot_ids` | RobotIDs | Selects robots by their specific IDs. |
| `location_id` | string | Selects all robots at the specified location. |

##### RobotIDs

| Name | Type | Description |
|------|------|-------------|
| `ids` | string |  |