### Message Types

#### Point2D
| Field | Type  | Description                                  |
|-------|-------|----------------------------------------------|
| `x`   | float | The x coordinate of the point in a 2D plane |
| `y`   | float | The y coordinate of the point in a 2D plane |

#### PointWithOrientation
| Field        | Type                       | Description                                    |
|--------------|----------------------------|------------------------------------------------|
| `x`          | float                      | The x coordinate of the point                 |
| `y`          | float                      | The y coordinate of the point                 |
| `orientation` | [Quaternion](#quaternion) | The orientation represented as a quaternion   |

#### Quaternion
| Field | Type  | Description                                     |
|-------|-------|-------------------------------------------------|
| `x`   | float | The x component of the quaternion (imaginary part) |
| `y`   | float | The y component of the quaternion (imaginary part) |
| `z`   | float | The z component of the quaternion (imaginary part) |
| `w`   | float | The real (scalar) component of the quaternion  |
