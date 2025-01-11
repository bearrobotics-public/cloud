### Message Types

#### BatteryState
| Field         | Type     | Description      |
|---------------|----------|------------------|
| `charge_percent` | int32                              | State of charge from 0 (battery empty) to 100 (battery full).               |
| `state`          | [State](#stateenum)<br>*repeated* |                                                                             |
| `charge_method`  | [ChargeMethod](#chargemethodenum)<br>*repeated* |                                                                             |

#### TrayState
| Field         | Type     | Description      |
|---------------|----------|------------------|
| `load_state`  | [LoadState](#loadstateenum)<br>*repeated* |                                                                                                   |
| `weight_kg`   | float   | Tray load in kilograms. The minimum precision is 10g.                                            |
| `load_ratio`  | float   | Ratio of the load to the maximum load capacity of the tray.<br> The value could exceed 1.0 if the tray is overloaded.<br> Caveats: If the maximum load is not configured correctly (e.g. 0.0),<br> NaN can be returned. |

#### StateEnum
| Name               | Number | Description                                                                 |
|--------------------|--------|-----------------------------------------------------------------------------|
| STATE_UNKNOWN      | 0      |                                                                             |
| STATE_CHARGING     | 1      |                                                                             |
| STATE_DISCHARGING  | 2      | Robot is not connected to the charger and is draining energy from the battery. |
| STATE_FULL         | 3      | While connected to the charger, the battery is fully charged, no more energy can be stored into the battery. |

#### ChargeMethodEnum
| Name                     | Number | Description |
|--------------------------|--------|-------------|
| CHARGE_METHOD_UNKNOWN    | 0      |             |
| CHARGE_METHOD_NOT_CHARGING | 1    |             |
| CHARGE_METHOD_WIRED      | 2      |             |
| CHARGE_METHOD_WIRELESS   | 3      |             |

#### LoadStateEnum
| Name              | Number | Description               |
|-------------------|--------|---------------------------|
| LOAD_STATE_UNKNOWN| 0      | Unknown load state.       |
| LOAD_STATE_LOADED | 1      | The robot is loaded.      |
| LOAD_STATE_EMPTY  | 2      | The robot is empty.       |
| LOAD_STATE_OVERLOADED | 3  | The robot is overloaded.  |
