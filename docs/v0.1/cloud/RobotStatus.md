### Message Types

#### RobotConnection
| Field         | Type                         | Description                           |
|---------------|------------------------------|---------------------------------------|
| `connection`  | [Connection](#connectionenum)<br>*enum* | Connection availability of the robot. |

#### RobotState
| Name               | Type                              | Description                           |
|--------------------|-----------------------------------|---------------------------------------|
| `robot_id`         | string                            |                                       |
| `robot_connection` | [RobotConnection](#robotconnection) | Connection availability of the robot. |

#### ConnectionEnum
| Name                   | Number | Description                                      |
|------------------------|--------|--------------------------------------------------|
| CONNECTION_UNKNOWN     | 0      |                                                  |
| CONNECTION_CONNECTED   | 1      | Whether the robot is connected to Bear cloud services. |
| CONNECTION_DISCONNECTED | 2      | Robot is unreachable/offline.                   |
