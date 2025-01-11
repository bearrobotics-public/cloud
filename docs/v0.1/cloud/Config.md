### Message Types

#### RobotFilter
| Field         | Type   | Description |
|---------------|--------|-------------|
| `location_id` | string | Empty location_id denotes all locations. |

#### RobotIDs
| Field | Type                   | Description       |
|-------|------------------------|-------------------|
| `ids` | string<br>*repeated*   | A list of robotIDs |

#### RobotSelector
| Field</div>   | Type                      | Description    |
|---------------|---------------------------|----------------|
| `robot_ids`   | [RobotIDs](#robotids)     | A list of specific robot IDs to target.|
| `location_id` | string                    | An identifier for a location, representing all robots <br> associated with this location. |
