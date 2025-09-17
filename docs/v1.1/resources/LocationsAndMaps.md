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
| `map_id`       | `string` | Unique identifier for the map.<br />Example: "9578" |
| `created_time` | `Timestamp` | Indicating when the map was created. |
| `modified_time`| `Timestamp` | Indicating the last time the map was modified. |
| `display_name` | `string` | Display name of the map, matching the name used in [Bear Universe](https://universe.bearrobotics.ai). <br />Example: "ITCT SEOUL" |
| `annotation`   | [`Annotation`](#annotation) | Annotation associated with this map, defining specific <br / >areas and destinations. |

##### Annotation
Annotation defines a specific area on the map, often used to group destinations or assign special parameters. 

| Field | Message Type | Description |
|------|------|-------------|
| `annotation_id` | `string` | Unique identifier for the annotation.<br />Example: "67305" |
| `display_name` | `string` | Descriptive name for the annotation.<br />Example: "ITCT annotation A" |
| `created_time` | `Timestamp` | Timestamp indicating when the annotation <br /> was created. |
| `destinations` | `map<string, Destination>` <br /> See [Destination](#destination) | A collection of destinations associated with <br /> this annotation. Each entry pairs a destination ID (key) <br /> with its corresponding Destination message (value). |

##### Destination
Destination represents a single point of interest on the map that a robot can navigate to and align itself with.

| Field | Message Type | Description |
|------|------|-------------|
| `destination_id` | string | Unique identifier for the destination. |
| `display_name` | string | Human-readable name for the destination. |

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
The location_id is a 4 character alphanumeric identifier for the location. (e.g., "3ROA") <br />

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
| `location_id` | `string` | Unique identifier for the location. |
| `display_name` | `string` | Human-readable name for the location. |
| `floors` | `repeated Floor` | List of floors within this location. |

##### Floor
Represents a single floor within a location.

| Field | Message Type | Description |
|------|------|-------------|
| `floor_id` | `string` | Unique identifier for the floor. |
| `display_name` | `string` | Human-readable name for the floor. |
| `sections` | `repeated Section` | List of sections within this floor. |

##### Section
Represents a section within a floor.

| Field | Message Type | Description |
|------|------|-------------|
| `section_id` | `string` | Unique identifier for the section. |
| `display_name` | `string` | Human-readable name for the section. |
| `map_id` | `string` | ID of the map associated with this section. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "location": {
          "locationId": "3R0A",
          "displayName": "Main Office Building",
          "floors": [
            {
              "floorId": "floor-1",
              "displayName": "Ground Floor",
              "sections": [
                {
                  "sectionId": "section-a",
                  "displayName": "Lobby Area",
                  "mapId": "map-lobby-001"
                }
              ]
            }
          ]
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
| `map_id`       | `string` | Unique identifier for the map. |
| `created_time` | `Timestamp` | Indicating when the map was created. |
| `modified_time`| `Timestamp` | Indicating the last time the map was modified. |
| `display_name` | `string` | Display name of the map. |
| `annotation`   | [`Annotation`](#annotation) | Annotation associated with this map. |

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
          }
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

### Request

##### robot_id `string` `required`
The ID of the robot that will switch maps.

##### map_selector `MapSelector` `required`
Specifies which map to switch to.

| Field | Message Type | Description |
|------|------|-------------|
| `location_id` | `string` | The location ID where the map is located. |
| `floor_id` | `string` | The floor ID within the location. |
| `section_id` | `string` | The section ID within the floor. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robot_id": "pennybot-abc123",
        "map_selector": {
          "location_id": "3R0A",
          "floor_id": "floor-1",
          "section_id": "section-a"
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
| `INVALID_ARGUMENT`| No matching map found for the specified location, floor, and section. |
| `NOT_FOUND`| The robot with the specified ID does not exist. |
