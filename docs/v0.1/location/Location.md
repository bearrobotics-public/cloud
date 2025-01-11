### Message Types

#### Location
| Field          | Type   | Description      |
|----------------|--------|------------------|
| `location_id`  | string | Example: "4RVF"  |
| `created_time` | [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) |                                                                                                 |
| `modified_time`| [Timestamp](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) |                                                                                                 |
| `display_name` | string  | Examples: "City Deli & Grill", "KNTH" |
| `floors`       | `map<string, string>` | Map of floor identifiers to floor information.<br>The key is the floor level, and the value is the floor information. |

#### Floor
| Field         | Type                       | Description             |
|---------------|----------------------------|-------------------------|
| `display_name`| string                     | Example: "Ground"       |
| `sections`    | [Section](#section)<br>*repeated* |                         |

#### Section
| Field             | Type             | Description                                              |
|-------------------|------------------|----------------------------------------------------------|
| `display_name`    | string           | Usually `display_name` will be empty if the section is not named. |
| `map_ids`         | string<br>*repeated* | List of map identifiers associated with this section.   |
| `current_map_id`  | string           | Current map identifier.                                  |
