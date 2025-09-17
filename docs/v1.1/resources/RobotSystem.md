# Robot System

System-level operations and information for robots.

-----------
## GetRobotSystemInfo

Get the overall robot system information, including software version, robot family, robot ID, and display name.

### Request

##### robot_id `string` `required`
The ID of the robot to retrieve system information for.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "pennybot-abc123"
      }
    ```

### Response

##### system_info `SystemInfo`
Contains system-level information about the robot.

| Field | Message Type | Description |
|------|------|-------------|
| `software_version` | string | The distribution version currently installed and running on the robot. |
| `robot_family` | RobotFamily *enum* | The classification or family of the robot. |
| `robot_id` | string | Unique identifier for the robot. |
| `display_name` | string | A user-friendly name for the robot, typically used for display purposes. |

##### RobotFamily `enum`
| Name | Number | Description |
|------|--------|-------------|
| ROBOT_FAMILY_UNKNOWN | 0 | Default value. It means the `robot_family` field is not returned. |
| ROBOT_FAMILY_SERVI | 1 | Servi robot family. |
| ROBOT_FAMILY_SERVI_MINI | 2 | Servi Mini robot family. |
| ROBOT_FAMILY_SERVI_PLUS | 3 | Servi Plus robot family. |
| ROBOT_FAMILY_SERVI_LIFT | 4 | Servi Lift robot family. |
| ROBOT_FAMILY_CARTI_100 | 5 | Carti 100 robot family. |
| ROBOT_FAMILY_CARTI_600 | 6 | Carti 600 robot family. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "systemInfo": {
          "softwareVersion": "servi-24.03",
          "robotFamily": "ROBOT_FAMILY_SERVI_PLUS",
          "robotId": "pennybot-abc123",
          "displayName": "Main Kitchen"
        }
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to retrieve system information for a `robot_id` you don't own. <br /> Tip: check the spelling of the `robot_id` value. |
| `NOT_FOUND` | The specified robot ID does not exist or is not accessible. |

-----------
## RunSystemCommand

Execute a OS-level command on a robot.

!!! Note
    When rebooting the robot, a response will return immediately to acknowledge the request but may take several minutes before the robot reconnects.

### Request

##### robot_id `string` `required`
The ID of the robot that will receive this command.

##### system_command `SystemCommand` `required`
The system command to execute on the robot.

##### SystemCommand
| Field (*oneof*) | Message Type | Description |
|------------|-------------| ---|
| `reboot` | `Reboot` | Reboot the robot with specified type. |

##### Reboot
| Field | Message Type | Description |
|------|------|-------------|
| `type` | Type *enum* | The type of reboot to perform. |

##### Type `enum`
| Name | Number | Description |
|------|--------|-------------|
| TYPE_UNKNOWN | 0 | Default value. This should never be used explicitly. |
| TYPE_SOFTWARE_ONLY | 1 | Perform an OS-level reboot without powering off hardware devices. |
| TYPE_WITH_HARDWARE | 2 | Perform a full power-cycle including hardware devices. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "robotId": "pennybot-abc123",
        "systemCommand": {
          "reboot": {
            "type": "TYPE_SOFTWARE_ONLY"
          }
        }
      }
    ```

### Response

*(No fields defined)*

##### JSON Response Example
=== "JSON"
    ```js
      {}
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `PERMISSION_DENIED` | Attempting to execute system command for a `robot_id` you don't own. <br /> Tip: check the spelling of the `robot_id` value. |
| `NOT_FOUND` | The specified robot ID does not exist or is not accessible. |
| `INVALID_ARGUMENT` | The system command is invalid or not supported. |
| `INTERNAL` | The request failed to execute due to internal error. Client should retry the command. |
