### Message Types

#### Pose
| Field             | Type  | Description                                                                                   |
|-------------------|-------|-----------------------------------------------------------------------------------------------|
| `x_meters`        | float | x, y coordinate inside the Map.                                                               |
| `y_meters`        | float |                                                                                               |
| `heading_radians` | float | The heading of the robot in radians. <br> Ranges from -PI to PI, with 0.0 pointing along the x-axis. |

#### PoseWithCovariance
| Field        | Type                 | Description |
|--------------|----------------------|-------------|
| `pose`       | [Pose](#pose)        |             |
| `covariance` | double<br>*repeated* | A 36-element array, which is a flattened 6×6 <br> covariance matrix in row-major order. Each element represents <br> the covariance between state variables (X, Y, Z, X-axis rotation, <br> Y-axis rotation, Z-axis rotation). |
