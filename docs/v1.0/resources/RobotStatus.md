These are streaming endpoints that provide real-time updates on robot health, including battery levels, charging state, and connectivity.

## SubscribeBatteryStatus
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to get battery state updates for the robot. Upon subscription, the latest battery state is sent immediately. Updates are streamed whenever the state changes.

### Request
##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. <br/>
 It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robot_ids": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        }
      }
    ```

### Response

##### metadata `EventMetadata`

| Field | Message Type | Description |
|------|------|-------------|
| `timestamp` | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | int64 | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with.

##### BatteryState
Represents the state of the robot's battery system.

| Field | Message Type | Description |
|------|------|-------------|
| `charge_percent` | int32 | State of charge, from 0 (empty) to 100 (fully charged). |
| `state` | State *enum*|  High-level charging state of the battery. |
| `charge_method` | ChargeMethod *enum* | Method by which the robot is being charged.|

##### State `enum`
| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATE_UNKNOWN          | 0      | Default value. It means the `state` field is not returned. |
| STATE_CHARGING          | 1      | Battery is currently charging.  |
| STATE_DISCHARGING          | 2      | Robot is not connected to a charger and is consuming battery power.         |
| STATE_FULL           | 3      | Battery is fully charged while connected to a charger; <br />no additional energy is being stored.                    |

##### ChargeMethod `enum`
| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| CHARGE_METHOD_UNKNOWN          | 0      | Default value. It means the `charge_method` field is not returned. |
| CHARGE_METHOD_NONE          | 1      | No charging method is currently active or applicable. |
| CHARGE_METHOD_WIRED          | 2      | Charging via a wired connection.     |
| CHARGE_METHOD_CONTACT           | 3      | Charging via contact-based interface (e.g., docking station).|

##### JSON Response Examples
=== "JSON"
    ```json
    {
      "metadata": {
        "timestamp": "2025-04-01T16:45:00Z",
        "sequenceNumber": 112
      },
      "robotId": "pennybot-123abc",
      "batteryState": {
        "chargePercent": 78,
        "state": "STATE_CHARGING",
        "chargeMethod": "CHARGE_METHOD_CONTACT"
      }
    }
    {
      "metadata": {
        "timestamp": "2025-04-01T16:45:02Z",
        "sequenceNumber": 113
      },
      "robotId": "pennybot-abc123",
      "batteryState": {
        "chargePercent": 42,
        "state": "STATE_DISCHARGING",
        "chargeMethod": "CHARGE_METHOD_NONE"
      }
    }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|

-----------
## SubscribeRobotStatus
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to get the robot's connectivity and operational state. Upon subscription, the latest battery state is sent immediately. Updates are streamed as the robot's state changes.

### Request
##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. <br/>
 It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robot_ids": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        }
      }
    ```
### Response
##### metadata `EventMetadata`

| Field | Message Type | Description |
|------|------|-------------|
| `timestamp` | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | int64 | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with.

##### robot_state.connetion.state `enum`
Represents the online connection state between the cloud and the robot.

| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| STATE_UNKNOWN          | 0      | Default value. It means the `state` field is not returned. |
| STATE_CONNECTED          | 1      | The robot is connected to Bear cloud services. |
| STATE_DISCONNECTED          | 2      | The robot is offline or unreachable from the cloud.   |

##### JSON Response Example

=== "JSON"
    ```json
    {
      "metadata": {
        "timestamp": "2025-04-01T17:05:00Z",
        "sequenceNumber": 211
      },
      "robotId": "pennybot-123abc",
      "robotStates": {
        "connection": {
          "state": "STATE_CONNECTED"
        }
      }
    },
    {
      "metadata": {
        "timestamp": "2025-04-01T17:05:05Z",
        "sequenceNumber": 212
      },
      "robotId": "pennybot-abc123",
      "robotStates": {
        "connection": {
          "state": "STATE_DISCONNECTED"
        }
      }
    }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
