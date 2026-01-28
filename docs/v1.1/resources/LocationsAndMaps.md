Retrieve data about a robot's operating environment, including [maps](../../concepts/location.md#maps), floors, [annotations](../../concepts/location.md#annotation), and navigable [destinations](../../concepts/location.md#destination).

------------
## GetCurrentMap 
Retrieves the current map used by the robot. <br />

The returned map includes annotations and destinations, which can be used in mission destination, localization goals, and navigation. <br />

### Request

##### robot_id `string` `required`
The robot ID used to request the map currently loaded on the robot.


##### JSON Request Example
=== "JSON"
    ```js
      {
        "robot_id": "pennybot-abc123"
      }
    ```

### Response

##### map `Map`
Map represents a navigable map used by robots, including metadata and associated annotations.

| Field | Message Type | Description |
|------|------|-------------|
| `map_id`       | `string` | Unique identifier for the map.<br />e.g., "9578" |
| `created_time` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Indicating when the map was created. |
| `modified_time`| [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Indicating the last time the map was modified. |
| `display_name` | `string` | Display name of the map, matching the name used in [Bear Universe](https://universe.bearrobotics.ai). <br />e.g., "ITCT SEOUL" |
| `annotation`   | [`Annotation`](#annotation) | Annotation associated with this map, defining specific <br / >areas and destinations. |

#### Annotation
Annotation defines a specific area on the map, often used to group destinations or assign special parameters. 

| Field | Message Type | Description |
|------|------|-------------|
| `annotation_id` | `string` | Unique identifier for the annotation.<br />e.g., "67305" |
| `display_name` | `string` | Descriptive name for the annotation.<br />e.g., "ITCT annotation A" |
| `created_time` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp indicating when the annotation <br /> was created. |
| `destinations` | `map<string, Destination>` <br /> See [Destination](#destination) | A collection of destinations associated with <br /> this annotation. Each entry pairs a destination ID (key) <br /> with its corresponding Destination message (value). |

#### Destination
Destination represents a single point of interest on the map that a robot can navigate to and align itself with.

| Field | Message Type | Description |
|------|------|-------------|
| `destination_id` | `string` | **Note**: In `GetCurrentMap`, this field returns the destination's display name. |
| `display_name` | `string` | Human-readable name for the destination. |

##### JSON Response Example
=== "JSON"
    ```js
       {
        "mapId": "map-01",
        "createdTime": "2025-03-28T12:00:00Z",
        "modifiedTime": "2025-04-01T09:45:00Z",
        "displayName": "Main Floor Map",
        "annotation": {
          "annotationId": "annot-123",
          "displayName": "Service Area Zones",
          "createdTime": "2025-03-30T10:15:00Z",
          "destinations": {
            "Loading Dock 1": {
              // destinationId and displayName are the same values. 
              "destinationId": "Loading Dock 1",
              "displayName": "Loading Dock 1"
            },
            "Main Kitchen": {
              "destinationId": "Main Kitchen",
              "displayName": "Main Kitchen"
            }
          }
        }
      }
    ```
### Errors

| ErrorCode  | Description |
|------------|-------------|
| `NOT_FOUND`| The annotation (of the requested map) does not exist. |

-----------
## GetLocationInfo
Retrieves information about a specific location by its ID. <br />

The location includes metadata such as floors, sections, and their associated maps. <br />

### Request

##### location_id `string` `required`
The location_id is a 4 character alphanumeric identifier for the location. <br />
e.g., "3R0A"

##### JSON Request Example
=== "JSON"
    ```js
      {
        "location_id": "3R0A"
      }
    ```

### Response

##### location `Location`
Location represents a physical space where robots operate, containing floors, sections, and maps.

| Field | Message Type | Description |
|------|------|-------------|
| `location_id` | `string` | A 4 character alphanumeric identifier for the location. <br />e.g., "3R0A" |
| `created_time` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp indicating when the location was created. |
| `modified_time` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp indicating the last time the location was modified. |
| `display_name` | `string` | Display name of the location, matching the name shown in Universe. <br />e.g., "City Deli & Grill", "KNTH" |
| `floors` | map<`int32`, [`Floor`](#floor)> | Map of floors in the location, keyed by their floor level. <br />The floor level is any non-negative integer starting from 0. |

#### Floor
Represents a single floor within a location.

| Field | Message Type | Description |
|------|------|-------------|
| `display_name` | `string` | Display name of the floor, matching the name shown in Universe. |
| `sections` | *repeated* [`Section`](#section) | List of sections on this floor. |

#### Section
Represents a section within a floor.

| Field | Message Type | Description |
|------|------|-------------|
| `display_name` | `string` | Display name of the section, matching the name shown in Universe. |
| `current_map_id` | `string` | ID of the current map associated with the section. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "location": {
          "locationId": "3R0A",
          "createdTime": "2025-03-28T12:00:00Z",
          "modifiedTime": "2025-04-01T09:45:00Z",
          "displayName": "Main Office Building",
          "floors": {
            "0": {
              "displayName": "Ground Floor",
              "sections": [
                {
                  "displayName": "Lobby Area",
                  "currentMapId": "map-lobby-001"
                }
              ]
            },
            "1": {
              "displayName": "Second Floor",
              "sections": [
                {
                  "displayName": "Office Area",
                  "currentMapId": "map-office-001"
                }
              ]
            }
          }
        }
      }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
| `NOT_FOUND`| The location with the specified ID does not exist. |

-----------
## GetMap
Retrieves a saved map by map_id from the cloud. <br />

Use this to fetch a stored map from the cloud database. <br />

### Request

##### map_id `string` `required`
The unique identifier of the map to retrieve.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "map_id": "map-001"
      }
    ```

### Response

##### map `Map`
Map represents a navigable map used by robots, including metadata and associated annotations.

| Field | Message Type | Description |
|------|------|-------------|
| `map_id`       | `string` | Unique identifier for the map. <br />e.g., "9578" |
| `created_time` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Indicating when the map was created. |
| `modified_time`| [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Indicating the last time the map was modified. |
| `display_name` | `string` | Display name of the map, matching the name used in [Bear Universe](https://universe.bearrobotics.ai). <br />e.g., "ITCT SEOUL" |
| `annotation`   | [`Annotation`](#annotation) | Annotation associated with this map, defining specific areas and destinations. |
| `image_download_info` | [`MapImageDownloadInfo`](#mapimagedownloadinfo) | Information for downloading the map image, including signed URL and file metadata. <br />This field is typically populated by the cloud service. |
| `origin` | [`Origin`](#origin) | Origin of the map relative to the map frame. |
| `resolution` | `float` | Resolution of the map in meters per pixel. |

#### MapImageDownloadInfo
Contains the information needed to download a map image.

| Field | Message Type | Description |
|------|------|-------------|
| `file_info` | [`MapImageFileInfo`](#mapimagefileinfo) | Information about the map image file for integrity verification. |
| `download_url` | [`SignedURL`](#signedurl) | The signed URL for downloading the map image. |

#### Origin
Represents the starting point of the map in terms of its coordinates and orientation.

| Field | Message Type | Description |
|------|------|-------------|
| `x_m` | `float` | X-coordinate of the map origin in meters. |
| `y_m` | `float` | Y-coordinate of the map origin in meters. |
| `yaw_radians` | `float` | Orientation (yaw) of the map origin in radians. |

#### MapImageFileInfo
Contains metadata about the map image file for integrity verification.

| Field | Message Type | Description |
|------|------|-------------|
| `checksum` | `uint32` | CRC32C checksum of the map image file. |
| `size` | `int64` | Size of the map image file in bytes. |

#### SignedURL
Represents a signed URL for file access with expiration.

| Field | Message Type | Description |
|------|------|-------------|
| `url` | `string` | The signed URL for file access. |
| `expires_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp when the signed URL expires. |


##### JSON Response Example
=== "JSON"
    ```js
      {
        "map": {
          "mapId": "map-001",
          "createdTime": "2025-03-28T12:00:00Z",
          "modifiedTime": "2025-04-01T09:45:00Z",
          "displayName": "Office Floor Plan",
          "annotation": {
            "annotationId": "annot-001",
            "displayName": "Office Zones",
            "createdTime": "2025-03-30T10:15:00Z",
            "destinations": {
              "Reception": {
                "destinationId": "Reception",
                "displayName": "Reception Desk"
              }
            }
          },
          "imageDownloadInfo": {
            "fileInfo": {
              "checksum": 1234567890,
              "size": 2048576
            },
            "downloadUrl": {
              "url": "https://storage.googleapis.com/maps/map-001.png?signature=...",
              "expiresAt": "2025-04-01T12:00:00Z"
            }
          },
          "origin": {
            "xM": 0.0,
            "yM": 0.0,
            "yawRadians": 0.0
          },
          "resolution": 0.05
        }
      }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
| `NOT_FOUND`| The map with the specified ID does not exist. |

-----------
## SwitchMap
Switch the robot's current map to a specified map. <br />

The request must specify the floor level and section index of the desired map. <br />

### Request

##### robot_id `string` `required`
The ID of the robot that will switch maps.

##### map_selector `MapSelector` `required`
Specifies which map to switch to.

| Field | Message Type | Description |
|------|------|-------------|
| `floor_level` | `int32` | Positive integer floor level begins at 1. |
| `section_index` | `int32` | Non-negative integer section index begins at 0. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robot_id": "pennybot-abc123",
        "map_selector": {
          "floor_level": 1,
          "section_index": 0
        }
      }
    ```

### Response

##### map_id `string`
The ID of the new map that the robot is now using.

##### JSON Response Example
=== "JSON"
    ```js
      {
        "map_id": "map-lobby-001"
      }
    ```

### Errors

| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT`| No matching map found for the specified floor level and section index. |
| `NOT_FOUND`| The robot with the specified ID does not exist. |
