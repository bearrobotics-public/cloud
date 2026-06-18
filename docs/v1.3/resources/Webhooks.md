# Webhooks

Webhooks deliver robot events as outbound HTTP requests, as an alternative to
maintaining a long-lived streaming connection. Once a subscription is created,
Bear sends an HTTP `POST` to the configured HTTPS endpoint each time an event
matching the subscription's type and filter occurs on any of the selected robots.

-----------

## CreateWebhook
Creates a new webhook subscription. The owning distributor is derived from the
authentication context.

### Request

##### url `string` `required`
The URL to deliver webhook events to. It must use the `https` scheme and resolve
to a publicly reachable address; requests to loopback, private, or link-local
addresses are rejected.

##### event_type `string` `required`
The event type to subscribe to. Supported values are `"mission"` and `"battery"`.
The delivered `state` payload for each type is described under [Event types](#event-types).

##### selector `WebhookRobotSelector` `required`
Selects which robots this webhook targets.

#### WebhookRobotSelector
Exactly one of the two fields must be set; setting both (or neither) returns an
`INVALID_ARGUMENT` error.

| Field | Message Type | Description |
|------|------|-------------|
| `robot_ids` | `RobotIdList` | Selects robots by their explicit IDs. <br /> Example: `{ "ids": ["pennybot-abc123", "pennybot-def456"] }` |
| `location_ids` | `LocationIdList` | Selects all robots at the given locations; the server resolves these to robot IDs via Fleet Info. <br /> Example: `{ "ids": ["1D9X", "3R0A"] }` |

Both `RobotIdList` and `LocationIdList` have a single field, `ids` (a list of `string`).

##### filter `FieldFilter`
Optional. Restricts deliveries to events whose fields match the given condition.
If omitted, every event of the subscribed type is delivered.

#### FieldFilter
A single field-level condition: the event is delivered only when `field` compared
against `values` using `operator` evaluates true.

| Field | Message Type | Description |
|------|------|-------------|
| `field` | `string` | Target field path within the event payload. The path begins with `state`, followed by a `.`-separated list of field names, e.g. `state.current_mission.state`. |
| `operator` | [`FilterOperator`](#filteroperator-enum) *enum* | The comparison operator to apply. |
| `values` | List of `string` | The values to match the target field against. |

#### FilterOperator `enum`
| Name | Number | Description |
|------|--------|-------------|
| FILTER_OPERATOR_UNKNOWN | 0 | Default value; not a valid operator. |
| FILTER_OPERATOR_IN | 1 | Matches when the field value is contained in `values`. |

##### options `WebhookOptions`
Optional. Delivery customizations such as a payload template, custom headers, and
a description.

#### WebhookOptions

| Field | Message Type | Description |
|------|------|-------------|
| `description` | `string` | Optional human-readable description of the webhook. |
| `request_template` | `object` | Optional template that reshapes the delivered body. Keys are output field names; values are template strings that reference event fields using `{{field_path}}` syntax. Only `robot_id` and scalar fields under `state.*` may be referenced. If unspecified, the body is the default [envelope](#payload). |
| `headers` | `map<string, string>` | Optional custom HTTP headers to include in every delivery request. |

##### request_template Example
=== "JSON"
    ```js
      {
        "robot": "{{robot_id}}",
        "mission": "{{state.current_mission.mission_id}}",
        "outcome": "{{state.current_mission.state}}"
      }
    ```

##### JSON Request Example
=== "JSON"
    ```js
      {
        "url": "https://example.com/bear/webhooks",
        "eventType": "mission",
        "selector": {
          "robotIds": {
            "ids": ["pennybot-abc123", "pennybot-def456"]
          }
        },
        "filter": {
          "field": "state.current_mission.state",
          "operator": "FILTER_OPERATOR_IN",
          "values": ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]
        },
        "options": {
          "description": "Notify on mission completion",
          "headers": {
            "x-secret": "my-shared-secret"
          }
        }
      }
    ```

### Response

##### webhook `WebhookSubscription`
The newly created webhook subscription, including its server-assigned `id`.

#### WebhookSubscription
A registered webhook configuration, returned by [CreateWebhook](#createwebhook)
and [ListWebhooks](#listwebhooks).

| Field | Message Type | Description |
|------|------|-------------|
| `id` | `string` | Unique identifier of the webhook subscription, assigned by the server. |
| `distributor_id` | `string` | The distributor (client organization) ID that owns this subscription. |
| `description` | `string` | Optional human-readable description. |
| `url` | `string` | The URL deliveries are sent to. |
| `headers` | `map<string, string>` | Custom HTTP headers included in every delivery. |
| `event_type` | `string` | The subscribed event type, e.g. `"mission"` or `"battery"`. |
| `selector` | [`WebhookRobotSelector`](#webhookrobotselector) | The robot selector provided at creation. |
| `filter` | [`FieldFilter`](#fieldfilter) | The filter provided at creation, if any. |
| `request_template` | `object` | The request template provided at creation, if any. |
| `enabled` | `bool` | Whether the subscription is currently active. |
| `created_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | When the subscription was created. |
| `updated_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | When the subscription was last updated. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "webhook": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "distributorId": "acme-robotics",
          "description": "Notify on mission completion",
          "url": "https://example.com/bear/webhooks",
          "headers": {
            "x-secret": "my-shared-secret"
          },
          "eventType": "mission",
          "selector": {
            "robotIds": {
              "ids": ["pennybot-abc123", "pennybot-def456"]
            }
          },
          "filter": {
            "field": "state.current_mission.state",
            "operator": "FILTER_OPERATOR_IN",
            "values": ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]
          },
          "enabled": true,
          "createdAt": "2026-06-16T12:00:00Z",
          "updatedAt": "2026-06-16T12:00:00Z"
        }
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `url` is set, `event_type` is a supported value, the `selector` is set with `robot_ids` or `location_ids`, and any `filter` or `request_template` is well-formed. |
| `PERMISSION_DENIED` | The authenticated distributor is not authorized for a robot or location named in the `selector`. <br /> Tip: ensure every `robot_id` / `location_id` in the selector belongs to your distributor. |
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------

## ListWebhooks
Lists all webhook subscriptions owned by the authenticated distributor.

### Request

*(No fields defined)*

##### JSON Request Example
=== "JSON"
    ```js
      {}
    ```

### Response

##### webhooks `repeated WebhookSubscription`
The list of webhook subscriptions owned by the distributor. Each item is a
[`WebhookSubscription`](#webhooksubscription).

##### JSON Response Example
=== "JSON"
    ```js
      {
        "webhooks": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "distributorId": "acme-robotics",
            "url": "https://example.com/bear/webhooks",
            "eventType": "mission",
            "selector": {
              "robotIds": {
                "ids": ["pennybot-abc123", "pennybot-def456"]
              }
            },
            "enabled": true,
            "createdAt": "2026-06-16T12:00:00Z",
            "updatedAt": "2026-06-16T12:00:00Z"
          }
        ]
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------

## DeleteWebhook
Deletes an existing webhook subscription by ID.

Webhook subscriptions cannot be edited in place. To change a subscription,
delete it and create a new one.

### Request

##### id `string` `required`
The unique identifier of the webhook subscription to delete.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "id": "550e8400-e29b-41d4-a716-446655440000"
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
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tip: check that `id` is not empty. |
| `NOT_FOUND` | No webhook subscription with the given `id` is accessible to the distributor — either it does not exist or it is owned by another distributor. |
| `INTERNAL` | Internal server error occurred while processing the request. |

-----------

## Delivery

Each delivery is an HTTP `POST` to the subscription's `url` with a
`Content-Type: application/json` body.

### Delivery headers

Every delivery includes the following Bear-generated headers, in addition to any
custom headers configured via [`WebhookOptions`](#webhookoptions):

| Header | Description |
|------|-------------|
| `X-Bear-Webhook-Id` | The ID of the webhook subscription that produced this delivery. |
| `X-Bear-Webhook-Event-Id` | A unique ID for this event. Stable across retries of the same event, so receivers can deduplicate. |
| `Content-Type` | Always `application/json`. |

!!! tip
    Use `X-Bear-Webhook-Event-Id` to make your receiver idempotent: the same
    event ID may be delivered more than once (for example, when a delivery is
    retried after a network error).

### Retries

Deliveries that fail with a network error or a retryable HTTP status are retried
with an exponential backoff. A delivery is considered successful when the
endpoint responds with a `2xx` status.

### Payload

By default, the delivered body is a JSON **envelope** that wraps the event under
a `state` field:

| Field | Type | Description |
|------|------|-------------|
| `robot_id` | `string` | The robot the event originated from. |
| `state` | `object` | The event payload, whose shape depends on the subscription's `event_type`. |

```json
{
  "robotId": "pennybot-abc123",
  "state": {
    "...": "event-specific payload"
  }
}
```

To deliver a differently shaped body, supply a `request_template` in
[`WebhookOptions`](#webhookoptions) when creating the subscription.

### Event types

The `event_type` of a subscription selects which event the webhook delivers.
The currently supported types are:

| `event_type` | `state` payload |
|------|-------------|
| `"mission"` | A snapshot of the robot's mission queue and its currently active mission and goal. |
| `"battery"` | The robot's current battery state. |

#### `"mission"` payload

The `state` field carries the following fields. The nested
[`MissionState`](Mission.md#missionstate) and [`Goal`](LocalizationAndNavigation.md#goal-goal-required)
types are the same as those returned by the Mission API.

| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [`EventMetadata`](Mission.md#eventmetadata) | Event metadata forwarded from the robot, including `timestamp` and `sequence_number`. |
| `missions` | List of [`MissionState`](Mission.md#missionstate) | The robot's full mission queue, in assignment order. Empty when no mission has been assigned. |
| `current_mission_index` | `int32` | Index of the currently active mission in `missions`. `-1` when no mission is active. |
| `current_mission` | [`MissionState`](Mission.md#missionstate) | The currently active mission, i.e. `missions[current_mission_index]`. Omitted when no mission is active. |
| `current_goal` | [`Goal`](LocalizationAndNavigation.md#goal-goal-required) | The currently active goal of the active mission. Omitted when there is no active mission or goal. |

```json
{
  "robotId": "pennybot-abc123",
  "state": {
    "metadata": {
      "timestamp": "2025-04-01T17:20:00Z",
      "sequenceNumber": 42
    },
    "currentMissionIndex": 0,
    "currentMission": {
      "missionId": "cbd47ab1-df21-479e-9f72-677b81ab55b0",
      "state": "STATE_SUCCEEDED"
    },
    "currentGoal": {
      "destinationId": "dest-123"
    }
  }
}
```

#### `"battery"` payload

The `state` field carries the following fields. The nested `BatteryState` type is
the same as the one returned by the Robot Status API.

| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [`EventMetadata`](Mission.md#eventmetadata) | Event metadata, including `timestamp` and `sequence_number`. |
| `battery_state` | [`BatteryState`](RobotStatus.md#batterystate) | The robot's current battery state. |

```json
{
  "robotId": "pennybot-abc123",
  "state": {
    "metadata": {
      "timestamp": "2025-04-01T17:20:00Z",
      "sequenceNumber": 87
    },
    "batteryState": {
      "chargePercent": 85,
      "state": "STATE_CHARGING",
      "chargeMethod": "CHARGE_METHOD_CONTACT"
    }
  }
}
```
