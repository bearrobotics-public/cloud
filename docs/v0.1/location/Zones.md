### Message Types

#### ParameterZone
| Field        | Type                                   | Description                          |
|--------------|----------------------------------------|--------------------------------------|
| `zone_id`    | string                                 |                                      |
| `points`     | [Point2D](../common/Math.md#point2d)<br>*repeated* | Polygon defining the zone.<br>The minimum number of points is 3. |
|              | [*type oneof*](#oneofzonetype)         |                                      |

#### OneOfZoneType
| Field            | Type                          | Description |
|------------------|-------------------------------|-------------|
| `direction_zone` | [DirectionZone](#directionzone) |             |
| `exclusive_zone` | [ExclusiveZone](#exclusivezone) |             |
| `ramp_zone`      | [RampZone](#rampzone)          |             |
| `sound_zone`     | [SoundZone](#soundzone)        |             |
| `speed_zone`     | [SpeedZone](#speedzone)        |             |

#### DirectionZone
| Field             | Type  | Description                                                                 |
|-------------------|-------|-----------------------------------------------------------------------------|
| `heading_radians` | float | The direction vector's angle in radians, <br> relative to the map's origin and measured from the x-axis. |
| `magnitude`       | int32 | The magnitude of the direction vector.<br>Typically set to 254 for hard direction zones.<br>The larger the magnitude, <br> the more the robot will try to align with the direction. |

#### ExclusiveZone
| Field       | Type  | Description                                        |
|-------------|-------|----------------------------------------------------|
| `max_robots`| int32 | Maximum number of robots allowed to enter the zone. |

#### RampZone
- *(No fields defined)*

#### SoundZone
- *(No fields defined)*

#### SpeedZone
| Field                | Type  | Description            |
|----------------------|-------|------------------------|
| `max_speed_m_per_sec`| float | Speed limit in the zone. |
