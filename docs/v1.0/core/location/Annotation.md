

### Message Types

##### Annotation

| Name | Type | Description |
|------|------|-------------|
| `id` | string | Unique identifier for the annotation.<br>Example: "67305" |
| `name` | string | Descriptive name for the annotation.<br>Example: "ITCT annotation A" |
| `created_time` | Timestamp | Timestamp indicating when the annotation was created. |
| `destinations` | DestinationsEntry | A collection of destinations associated with this annotation. |

##### DestinationsEntry

| Name | Type | Description |
|------|------|-------------|
| `key` | string |  |
| `value` | Destination |  |

##### Destination

| Name | Type | Description |
|------|------|-------------|
| `id` | string | Unique identifier for the destination. |
| `name` | string | Human-readable name for the destination. |
| `pose` | Pose | Position on the map where the robot should navigate to,<br>including orientation (heading) at that location. |