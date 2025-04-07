Retrieve data about a robot's operating environment, including [maps](../../concepts/location.md#maps), floors, [annotations]((../../concepts/location.md#annotation)), and navigable [destinations](../../concepts/location.md#destination).

------------
## GetCurrentMap 
Retrieves the current map used by the robot. <br />

The returned map includes annotations and destinations, which can be used in mission destination, localization goals, and navigation. <br />

### Request

##### robot_id `string` `required`
The robot ID used to request the map currently loaded on the robot.

### Response

##### map `Map`
Map represents a navigable map used by robots, including metadata and associated annotations.

| Field | Message Type | Description |
|------|------|-------------|
| `map_id`       | `string` | Unique identifier for the map.<br />Example: "9578" |
| `created_time` | `Timestamp` | Indicating when the map was created. |
| `modified_time`| `Timestamp` | Indicating the last time the map was modified. |
| `display_name` | `string` | Display name of the map, matching the name used in [Bear Universe](universe.bearrobotics.ai). <br />Example: "ITCT SEOUL" |
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

### Errors

| ErrorCode  | Description |
|------------|-------------|
| `NOT_FOUND`| The annotation (of the requested map) does not exist. |

### Examples

##### Response
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

=== "Protobuf"
    ###### Refer to our [public protobuf repo](https://github.com/bearrobotics/public-protos) for actual package names and full definitions.
    
    ```proto
    message Destination {
      string destination_id = 1;

      string display_name = 2;

    }

    message Annotation {

      string annotation_id = 1;

      string display_name = 2;

      google.protobuf.Timestamp created_time = 3;

      map<string, Destination> destinations = 4;
    }

    message Map {

      string map_id = 1;

      google.protobuf.Timestamp created_time = 2;

      google.protobuf.Timestamp modified_time = 3;

      string display_name = 4;

      Annotation annotation = 5;
    }
    ```



