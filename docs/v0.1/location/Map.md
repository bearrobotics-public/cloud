### Message Types

#### Annotation
| Field         | Type                                                   | Description |
|---------------|--------------------------------------------------------|-------------|
| `destinations`| [Destination](../location/Annotation.md/#destination)<br>*repeated* |             |

#### Map
| Field                  | Type           | Description               |
|------------------------|----------------|---------------------------|
| `map_id`               | string         | Example: "9578"           |
| `created_time`         | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) |                                                                             |
| `modified_time`        | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) |                                                                             |
| `display_name`         | string    | Example: "ITCT SEOUL"                                                      |
| `map_data_id`          | string    | Current map data identifier that represents this map.                      |
| `annotation_ids`       | string<br>*repeated*  | List of annotation identifiers associated with this map.       |
| `current_annotation_id`| string    | Current annotation identifier.                                             |

#### MapContent
| Field         | Type                                | Description |
|---------------|-------------------------------------|-------------|
| `map_id`      | string                              |             |
| `map_data`    | [MapData](#mapcontentmapdata)       |             |
| `annotation`  | [Annotation](#annotation)<br>*repeated* |             |

#### MapContent.MapData
| Field         | Type    | Description                                                                 |
|---------------|---------|-----------------------------------------------------------------------------|
| `map_data_id` | string  |                                                                             |
| `url`         | string  | URL to the image data for the map.                                         |
| `origin`      | Origin  | The Origin of the map.                                                     |
| `m_per_pixel` | float   | Maps real-world size to pixelated size (meters per pixel).<br>Equivalent to the "resolution" defined in the ROS map server: [ROS Map Server](https://wiki.ros.org/map_server) |

#### MapData
| Field         | Type             | Description                                                                 |
|---------------|------------------|-----------------------------------------------------------------------------|
| `data`        | bytes            | The image PNG data for the map.                                            |
| `origin`      | [Origin](#origin)|                                                                             |
| `m_per_pixel` | float            | Maps real-world size to pixelated size (meters per pixel).<br>Equivalent to the "resolution" defined in the ROS map server: [ROS Map Server](https://wiki.ros.org/map_server) |

#### Origin
| Field        | Type  | Description                                                                 |
|--------------|-------|-----------------------------------------------------------------------------|
| `x_m`        | float | The x-coordinate of the origin of the map in the world frame.              |
| `y_m`        | float | The y-coordinate of the origin of the map in the world frame.              |
| `yaw_radians`| float | Rotation around the z-axis (counterclockwise) of the map <br> with respect to the world frame.<br>A yaw of 0 means no rotation. |
