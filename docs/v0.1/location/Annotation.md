### Message Types

#### Annotation
| Field             | Type  | Description  |
|-------------------|-------|--------------|
| `annotation_id`   | string               | Example: "67305"       |
| `created_time`    | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) |         |
| `display_name`    | string               | Example: "ITCT annotation A"             |
| `obstacles`       | [Obstacle](#obstacle)<br>*repeated*    | Areas on the map that the robot will try to avoid.       |
| `parameter_zones` | [ParameterZone](../location/Zones.md#parameterzone)                 | Areas on the map that have specific parameters.          |
| `destinations`    | [Destination](#destination)<br>*repeated*              | Destinations are used to define the single point of interest.              |
| `preferred_paths` | [PreferredPath](#preferredpath)<br>*repeated*          | Directional and bi-directional paths <br> which robots will try to follow when nearby. |
| `queues`          | [Queue](#queue)<br>*repeated*          | Queues are used to define the waiting area.              |

#### Destination
| Field              | Type  | Description  |
|--------------------|-------|--------------|
| `destination_id`   | string               |         |
| `display_name`     | string               |         |
| `destination_pose` | [PointWithOrientation](../common/Math.md/#pointwithorientation)          | Position on the map where the robot would try to <br> navigate to and orient itself along that direction. |
| `type`             | [Type](#destinationtypeenum)<br>*enum* |         |
| `docking_param`    | [DockingParam](#dockingparam)          | Docking parameters for specifying the docking process <br> at the destination.  |
| `default_type_data`| [StringMapData](#stringmapdata)        | Data specific to the destination type, used by the robot <br> to interact with the destination. |

#### DockingParam
| Field          | Type          | Description             |
|----------------|---------------|-------------------------|
| `type`         | [Type](#dockingparamtypeenum)<br>*enum*    |         |
| `reference`    | [Reference](#dockingparamrefernceenum)<br>*enum* |         |
| `reference_id` | string   |         |
| `tuning_params`| [Point2D](../common/Math.md/#point2d)<br>*repeated* | The tuning parameters used to define relative docking pose to the reference. |

#### StringMapData
| Name   | Type         | Description |
|--------|--------------|-------------|
| `data` | `map<string, string>`          |             |

#### Obstacle
| Field       | Type     | Description             |
|-------------|----------|-------------------------|
| `obstacle_id`| string  |         |
| `points`     | [Point2D](../common/Math.md/#point2d)     | Points that define a polygon where the robot should avoid entering. <br> The minimum number of points is 3. |
| `type`       | [Type](#obstacletypeenum)<br>*enum*       |         |

#### PreferredPath
| Field              | Type         | Description               |
|--------------------|--------------|---------------------------|
| `preferred_path_id`| string       |           |
| `graph_nodes`      | [GraphNode](../location/Types.md/#graphnode)<br>*repeated* | List of graph nodes that make up the preferred path. |

#### Queue
| Field              | Type         | Description         |
|--------------------|--------------|---------------------|
| `queue_id`         | string       |   |
| `queue_poses`      | [GraphNode](../location/Types.md/#graphnode)<br>*repeated* | Represents a list of queuing points where the robot will wait. |
| `destination_ids`  | string<br>*repeated*         | End destinations that a queue can dequeue the robot to. |

#### DestinationTypeEnum
| Name                     | Number | Description                                                                 |
|--------------------------|--------|-----------------------------------------------------------------------------|
| TYPE_UNKNOWN             | 0      |                                                                             |
| TYPE_DEFAULT             | 1      | The default destination type. The robot will try to navigate to this point. |
| TYPE_CONTACT_CHARGER     | 2      | The contact-type charger. The robot can charge through docking.             |
| TYPE_INDUCTIVE_CHARGER   | 3      | The inductive-type charger. The robot can charge through docking without physical contact. |

#### DockingParamTypeEnum
| Name           | Number | Description                                                 |
|----------------|--------|-------------------------------------------------------------|
| TYPE_UNKNOWN   | 0      |                                                             |
| TYPE_DEFAULT   | 1      | The robot will run the default docking process at the destination. |

#### DockingParamRefernceEnum
| Name               | Number | Description                                                                 |
|--------------------|--------|-----------------------------------------------------------------------------|
| REFERENCE_UNKNOWN  | 0      |                                                                             |
| REFERENCE_DEFAULT  | 1      | The default reference for each destination type.                           |
| REFERENCE_QR_CODE  | 2      | The QR code reference is used to specify the docking position.             |
| REFERENCE_VL_MARKER| 3      | The VL marker reference is used to specify the docking position.            |

#### ObstacleTypeEnum
| Name               | Number | Description                                                                 |
|--------------------|--------|-----------------------------------------------------------------------------|
| TYPE_UNKNOWN       | 0      |                                                                             |
| TYPE_DEFAULT       | 1      | The default type for obstacle references.                                  |
| TYPE_QR_CODE       | 2      | Specifies the QR code obstacle type.                                       |
| TYPE_VL_MARKER     | 3      | Specifies the VL marker obstacle type.  