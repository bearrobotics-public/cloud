

### Message Types

##### TrayState

| Name | Type | Description |
|------|------|-------------|
| `load_state` | LoadState | Current load state of the tray. |
| `weight_kg` | float | Weight on the tray in kilograms. Minimum precision is 10g. |
| `load_ratio` | float | Ratio of the current load to the tray’s maximum load capacity.<br>This value may exceed 1.0 if the tray is overloaded.<br>Caveats:<br>- If the maximum load is misconfigured (e.g., set to 0.0),<br>  this value may return NaN. |

##### TrayStates

| Name | Type | Description |
|------|------|-------------|
| `tray_states` | TrayStatesEntry |  |

##### TrayStatesEntry

| Name | Type | Description |
|------|------|-------------|
| `key` | string |  |
| `value` | TrayState |  |