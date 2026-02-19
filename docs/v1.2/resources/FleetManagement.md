# Fleet Management

Utilities for managing multiple robots within an account or workspace.

## ListRobotIDs 
Retrieves a list of robot IDs the user has access to, filtered by optional criteria. <br />

The list includes all known robots, regardless of their current connection state. <br />

### Request
##### filter `RobotFilter` `required`
`RobotFilter` defines the conditions for selecting robots. <br/>
All specified fields are combined using an **AND** condition. 


| Field | Message Type | Description |
|------|------|-------------|
|`location_id`| `string`|An empty location_id return all robots assigned <br />to all locations created and owned by API key user.  |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "filter": {
          "locationId": "location-123"
        }
      }
    ```

### Response
A response message has 2 fields: <br/>

| Field | Message Type | Description |
|------|------|-------------|
|`total_robots`| `int32`| Total number of robots. |
|`robot_ids`| List of `string` | This might not have all the robot IDs if there are too many.<br /> It will have all the robot IDs if the number of <br /> `robot_ids` is same as `total_robots`. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "totalRobots": 5,
        "robotIds": [
          "pennybot-abc123",
          "pennybot-def456",
          "pennybot-ghi789",
          "pennybot-jkl012",
          "pennybot-mno345"
        ]
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid API key or request parameters. |
| `PERMISSION_DENIED` | Attempting to retrieve Robot IDs with a `location_id` you don't own. <br /> Tips: check the spelling of all `location_id` values.|
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------
## GetAvailableLocations
Returns a map of locations the user has access to. <br />

The map consists of the location ID (key) paired with its human-readable name (value).

### Request

*(No fields defined)*

##### JSON Request Example
=== "JSON"
    ```js
      {}
    ```

### Response

##### locations `map<string, string>`
A mapping of location ID (key) with its corresponding, human-readable location name. <br />
e.g. { "1D9X": "785_Platform" }

##### JSON Response Example
=== "JSON"
    ```js
      {
        "locations": {
          "1D9X": "785_Platform",
          "3R0A": "Main Office Building",
          "5K2B": "Warehouse Facility"
        }
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid API key or request parameters. |
| `INTERNAL` | Internal server error occurred while processing the request. |
