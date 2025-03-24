

### Message Types

##### BussingMission

| Name | Type | Description |
|------|------|-------------|
| `destinations` | Destination |  |
| `params` | BussingParams |  |

##### BussingParams
- *(No fields defined)*

##### BussingPatrolMission

| Name | Type | Description |
|------|------|-------------|
| `destinations` | Destination |  |
| `params` | BussingPatrolParams |  |

##### BussingPatrolParams
- *(No fields defined)*

##### DeliveryMission

| Name | Type | Description |
|------|------|-------------|
| `destinations` | Destination |  |
| `params` | DeliveryParams |  |

##### DeliveryParams
- *(No fields defined)*

##### DeliveryPatrolMission

| Name | Type | Description |
|------|------|-------------|
| `destinations` | Destination |  |
| `params` | DeliveryPatrolParams |  |

##### DeliveryPatrolParams
- *(No fields defined)*

##### Feedback

| Name | Type | Description |
|------|------|-------------|
| `status` | Status | Current status of the robot. |

##### ServiMission

| Name | Type | Description |
|------|------|-------------|
| `delivery_mission` | DeliveryMission | A delivery mission with stop conditions based on time or weight removal. |
| `bussing_mission` | BussingMission | A bussing mission with stop conditions based on time or weight addition. |
| `delivery_patrol_mission` | DeliveryPatrolMission | A delivery patrol mission that loops until all weight is removed. |
| `bussing_patrol_mission` | BussingPatrolMission | A bussing patrol mission that loops until weight exceeds a threshold. |