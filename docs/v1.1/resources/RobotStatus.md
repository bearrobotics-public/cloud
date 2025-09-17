# Robot Status

Streaming endpoints that provide real-time updates on robot health, including battery levels, charging state, and connectivity.

-----------
## GetRobotStatus

Get the latest robot state.

Robot state includes connectivity and operational states.

### Request

##### robot_id `string` `required`
The ID of the robot to get status for.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "pennybot-abc123"
      }
    ```

### Response

##### robot_state `RobotState`
The current robot state including connectivity, battery, emergency stop, mission, pose, and error information.

| Field | Message Type | Description |
|------|------|-------------|
| `connection` | [`RobotConnection`](#robotconnection) | Connection state of the robot. |
| `battery` | [`BatteryState`](#batterystate) | Battery state of the robot. |
| `emergency_stop` | [`EmergencyStopState`](#emergencystopstate) | Emergency stop state of the robot. |
| `mission` | [`MissionState`](../Mission/#mission_state-missionstate) | Mission state of the robot. |
| `pose` | [`Pose`](../LocalizationAndNavigation/#pose) | Current pose of the robot. |
| `error_codes` | [`ErrorCodes`](#errorcodes) | Error codes returned by the robot. |
| `typed_status` | *oneof* | Robot type-specific state information. Only one type may be set at a time. |

##### RobotConnection
Represents the online connection state between the cloud and the robot.

| Field | Message Type | Description |
|------|------|-------------|
| `state` | [`State`](#connectionstate-enum) *enum* | Current connection state of the robot. |

##### ConnectionState `enum`
| Name | Number | Description |
|------|--------|-------------|
| STATE_UNKNOWN | 0 | Default value. It means the `state` field is not returned. |
| STATE_CONNECTED | 1 | The robot is connected to Bear cloud services. |
| STATE_DISCONNECTED | 2 | The robot is offline or unreachable from the cloud. |

##### BatteryState
Represents the state of the robot's battery system.

| Field | Message Type | Description |
|------|------|-------------|
| `charge_percent` | `int32` | State of charge, from 0 (empty) to 100 (fully charged). |
| `state` | [`State`](#batterystate-enum) *enum* | High-level charging state of the battery. |
| `charge_method` | [`ChargeMethod`](#chargemethod-enum) *enum* | Method by which the robot is being charged. |

##### State `enum`
| Name | Number | Description |
|------|--------|-------------|
| STATE_UNKNOWN | 0 | Default value. It means the `state` field is not returned. |
| STATE_CHARGING | 1 | Battery is currently charging. |
| STATE_DISCHARGING | 2 | Robot is not connected to a charger and is consuming battery power. |
| STATE_FULL | 3 | Battery is fully charged while connected to a charger; no additional energy is being stored. |

##### ChargeMethod `enum`
| Name | Number | Description |
|------|--------|-------------|
| CHARGE_METHOD_UNKNOWN | 0 | Default value. It means the `charge_method` field is not returned. |
| CHARGE_METHOD_NONE | 1 | No charging method is currently active or applicable. |
| CHARGE_METHOD_WIRED | 2 | Charging via a wired connection. |
| CHARGE_METHOD_CONTACT | 3 | Charging via contact-based interface (e.g., docking station). |

##### EmergencyStopState
Represents the state of the robot's emergency stop system.

| Field | Message Type | Description |
|------|------|-------------|
| `emergency` | [`Emergency`](#emergency-enum) *enum* | Whether the software level emergency stop is engaged. |
| `button_pressed` | [`Emergency`](#emergency-enum) *enum* | Whether the physical emergency stop button is engaged. |

##### Emergency `enum`
| Name | Number | Description |
|------|--------|-------------|
| EMERGENCY_UNKNOWN | 0 | Default value. It means the `emergency` field is not returned. |
| EMERGENCY_ENGAGED | 1 | Triggers an emergency stop. Overrides and sets navigation-related velocity command to 0 to the motor. |
| EMERGENCY_DISENGAGED | 2 | Wheels will resume acting upon software navigation commands. |

##### ErrorCodes
Represents the error codes returned by the robot.

| Field | Message Type | Description |
|------|------|-------------|
| `codes` | *repeated* `string` | List of error codes currently active on the robot. |

##### typed_status `oneof`
Robot type-specific state information. Only one type may be set at a time.

| Field (*oneof*) | Message Type | Description |
|------|------|-------------|
| `servi_state` | `ServiState` | Servi-specific robot state (only for Servi robots). |
| `carti_state` | `CartiState`| Carti-specific robot state (only for Carti robots). |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "robotState": {
          "connection": {
            "state": "STATE_CONNECTED"
          },
          "battery": {
            "chargePercent": 85,
            "state": "STATE_CHARGING",
            "chargeMethod": "CHARGE_METHOD_CONTACT"
          },
          "emergencyStop": {
            "emergency": "EMERGENCY_DISENGAGED",
            "buttonPressed": "EMERGENCY_DISENGAGED"
          },
          "mission": {
            "missionId": "mission-123",
            "state": "STATE_ACTIVE",
            "goals": [],
            "currentGoalIndex": 0
          },
          "pose": {
            "xMeters": 1.5,
            "yMeters": 2.8,
            "headingRadians": 0.78
          },
          "errorCodes": {
            "codes": ["E001", "E042"]
          },
          "serviState": {
            "trayStates": {
              "trayStates": [
                {
                  "trayName": "top",
                  "loadState": "LOAD_STATE_LOADED",
                  "weightKg": 2.5,
                  "loadRatio": 0.8
                },
                {
                  "trayName": "middle",
                  "loadState": "LOAD_STATE_EMPTY",
                  "weightKg": 0.0,
                  "loadRatio": 0.0
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
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `robot_id` is not empty. |
| `PERMISSION_DENIED` | Attempting to get status for a `robot_id` you don't own. <br /> Tip: check the spelling of the `robot_id` value. |
| `INTERNAL` | Communication failure with the robot. |

-----------
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
| `timestamp` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | `int64` | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with.

##### BatteryState
Represents the state of the robot's battery system.

| Field | Message Type | Description |
|------|------|-------------|
| `charge_percent` | `int32` | State of charge, from 0 (empty) to 100 (fully charged). |
| `state` | [`State`](#state-enum) *enum*|  High-level charging state of the battery. |
| `charge_method` | [`ChargeMethod`](#chargemethod-enum) *enum* | Method by which the robot is being charged.|

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
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `selector` is not null, `robot_ids` is not empty, or `location_id` is not empty. |
| `NOT_FOUND` | No robots found for the specified `location_id`. |
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
| `INTERNAL` | Internal server error occurred while processing the request. |

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
| `timestamp` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | The time when the event was recorded. |
| `sequence_number` | `int64` | An incremental sequence number generated by the robot.<br />The sequence number should never be negative and can be reset to 0.<br />i.e. sequence is valid if it is larger than the previous number or 0. |

##### robot_id `string`
The robotID the message is associated with.

##### robot_state `RobotState`
The current robot state including connectivity, battery, emergency stop, mission, pose, and error information.

| Field | Message Type | Description |
|------|------|-------------|
| `connection` | [`RobotConnection`](#robotconnection) | Connection state of the robot. |
| `battery` | [`BatteryState`](#batterystate) | Battery state of the robot. |
| `emergency_stop` | [`EmergencyStopState`](#emergencystopstate) | Emergency stop state of the robot. |
| `mission` | [`MissionState`](../Mission/#mission_state-missionstate) | Mission state of the robot. |
| `pose` | [`Pose`](../LocalizationAndNavigation/#pose) | Current pose of the robot. |
| `error_codes` | [`ErrorCodes`](#errorcodes) | Error codes returned by the robot. |
| `typed_status` | *oneof* | Robot type-specific state information. Only one type may be set at a time. |

##### JSON Response Example

=== "JSON"
    ```json
    {
      "metadata": {
        "timestamp": "2025-04-01T17:05:00Z",
        "sequenceNumber": 211
      },
      "robotId": "pennybot-123abc",
      "robotState": {
        "connection": {
          "state": "STATE_CONNECTED"
        },
        "battery": {
          "chargePercent": 75,
          "state": "STATE_CHARGING",
          "chargeMethod": "CHARGE_METHOD_CONTACT"
        },
        "emergencyStop": {
          "emergency": "EMERGENCY_DISENGAGED",
          "buttonPressed": "EMERGENCY_DISENGAGED"
        },
        "mission": {
          "missionId": "mission-456",
          "state": "STATE_RUNNING",
          "goals": [
            {
              "goal": {
                "pose": {
                  "xMeters": 5.2,
                  "yMeters": 3.1,
                  "headingRadians": 0.0
                }
              }
            }
          ],
          "currentGoalIndex": 1
        },
        "pose": {
          "xMeters": 2.1,
          "yMeters": 1.8,
          "headingRadians": 1.2
        },
        "errorCodes": {
          "codes": ["E001", "E042"]
        }
      }
    }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `selector` is not null, `robot_ids` is not empty, or `location_id` is not empty. |
| `NOT_FOUND` | No robots found for the specified `location_id`. |
| `PERMISSION_DENIED` | Attempting to request status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------
## SubscribeErrorCodes
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to subscribe to error codes returned by the robot. Upon subscription, the latest known error codes are sent immediately. Updates are streamed when error codes change.

### Request

##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robotIds": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        }
      }
    ```

### Response

##### error_codes `map<string, ErrorCodesWithMetadata>`
A mapping of error codes returned by individual robots.
Each entry pairs a robot ID (key) with a corresponding error code and metadata.
Note that each robot maintains its own metadata, so messages should be correlated if and only if they correspond to the same robot ID.

##### ErrorCodesWithMetadata
| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [`EventMetadata`](#metadata-eventmetadata) | Metadata associated with the error codes. |
| `codes` | *repeated* [`ErrorCode`](#errorcode) | The error codes reported by the robot. |

##### ErrorCode
| Field | Message Type | Description |
|------|------|-------------|
| `code` | `int32` | Integer code indicating the type of error. Does not indicate severity. |
| `severity` | [`Severity`](#severity-enum) *enum* | Level of criticality of an error. |
| `message` | `string` | Message about the error e.g. "Up camera process error." |

##### Severity `enum`
| Name | Number | Description |
|------|--------|-------------|
| SEVERITY_UNKNOWN | 0 | Default value. It means the `severity` field is not returned. |
| SEVERITY_LOW | 1 | Low severity indicates an identified issue that is not visible. |
| SEVERITY_MEDIUM | 2 | Medium severity indicates an identified issue that does not block operation. |
| SEVERITY_HIGH | 3 | High severity indicates an identified issue that blocks operation. |

##### JSON Response Example
=== "JSON"
    ```js
    {
      "errorCodes": {
        "pennybot-abc123": {
          "metadata": {
            "timestamp": "2025-04-01T17:10:00Z",
            "sequenceNumber": 215
          },
          "codes": [
            {
              "code": 1001,
              "severity": "SEVERITY_MEDIUM",
              "message": "Low battery warning"
            },
            {
              "code": 2042,
              "severity": "SEVERITY_HIGH",
              "message": "Camera connection lost"
            }
          ]
        },
        "pennybot-123abc": {
          "metadata": {
            "timestamp": "2025-04-01T17:10:00Z",
            "sequenceNumber": 216
          },
          "codes": []
        }
      }
    }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `selector` is not null, `robot_ids` is not empty, or `location_id` is not empty. |
| `NOT_FOUND` | No robots found for the specified `location_id`. |
| `PERMISSION_DENIED` | Attempting to request error codes for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values.|
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------
## SubscribeNetworkStatus
A [server side streaming RPC](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) endpoint to subscribe to network status updates for robots. Upon subscription, the latest known network states are sent immediately. Updates are streamed when any network state changes.

### Request

##### selector `RobotSelector` `required`
`RobotSelector` is used to select specific robots. It supports selection by a list of robot IDs **OR** all robots at a given location.

| Field | Message Type | Description |
|------|------|-------------|
|`robot_ids`| `RobotIDs`| Selects robots by their specific IDs. <br/> Example: `["pennybot-123abc", "pennybot-abc123"]` |
|`location_id`|`string` |  Selects all robots at the specified location. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "selector": {
          "robotIds": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        }
      }
    ```

### Response

##### network_states `map<string, NetworkStateWithMetadata>`
A mapping of network states reported by individual robots.
Each entry pairs a robot ID (key) with its corresponding network state and metadata.
Note that each robot maintains its own metadata, so messages should be correlated if and only if they correspond to the same robot ID.

##### NetworkStateWithMetadata
| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [`EventMetadata`](#metadata-eventmetadata) | Metadata associated with the network state. |
| `connected_wifi` | [`Wifi`](#wifi) *optional* | Current network connection of the robot. If the field is not set, it indicates that the robot is not connected to any Wi-Fi networks. |

##### Wifi
Represents the Wifi connection of a robot.

| Field | Message Type | Description |
|------|------|-------------|
| `ssid` | `string` | SSID of the Wi-Fi network (not necessarily unique). |
| `signal_level_dbm` | `int32` | Signal level of the Wifi in dBm. |
| `link_quality` | `int32` | Link quality of the Wifi. Ranges from 0 to 70, where 70 is highest quality. |
| `security` | [`Security`](#security-enum) *enum* | Security requirements for the network. |

##### Security `enum`
| Name | Number | Description |
|------|--------|-------------|
| SECURITY_UNKNOWN | 0 | Default value. It means the `security` field is not returned. |
| SECURITY_UNSECURED | 1 | Unsecured network that do not require any authentication. |
| SECURITY_PASSWORD_SECURED | 2 | Password secured network. e.g. WPA2, WPA3 and WEP networks |
| SECURITY_USERNAME_PASSWORD_SECURED | 3 | Login required network. i.e. Enterprise networks |

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `selector` is not null, `robot_ids` is not empty, or `location_id` is not empty. |
| `NOT_FOUND` | No robots found for the specified `location_id`. |
| `PERMISSION_DENIED` | Attempting to request network status for a `robot_id` or `location_id` you don't own. <br /> Tip: check the spelling of all `robot_id` or `location_id` values. |
| `INTERNAL` | Internal server error occurred while processing the request. |

##### JSON Response Example
=== "JSON"
    ```js
    {
      "networkStates": {
        "pennybot-abc123": {
          "metadata": {
            "timestamp": "2025-04-01T16:00:00Z",
            "sequenceNumber": 105
          },
          "connectedWifi": {
            "ssid": "BearRobotics-Office",
            "signalLevelDbm": -45,
            "linkQuality": 65,
            "security": "SECURITY_PASSWORD_SECURED"
          }
        },
        "pennybot-123abc": {
          "metadata": {
            "timestamp": "2025-04-01T16:00:00Z",
            "sequenceNumber": 106
          }
        }
      }
    }
    ```
