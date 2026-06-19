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
If omitted, every event of the subscribed type is delivered. A subscription
supports a **single** condition — there is no `AND`/`OR` composition; create
separate subscriptions for separate conditions. See [Filtering events](#filtering-events)
for a worked walkthrough.

#### FieldFilter
A single field-level condition: the event is delivered only when `field` compared
against `values` using `operator` evaluates true.

| Field | Message Type | Description |
|------|------|-------------|
| `field` | `string` | Target field path within the event payload, using the **snake_case** field names of the delivered body (see [Payload](#payload)). The path begins with `state`, followed by a `.`-separated list of field names, e.g. `state.current_mission.state`. It must resolve to a **scalar** leaf (string, number, bool, enum, or timestamp) — not a message, list, or map. To reach into a list, append an index: `state.missions.0.mission_id`. `robot_id` cannot be filtered here — scope robots with the [`selector`](#webhookrobotselector) instead. |
| `operator` | [`FilterOperator`](#filteroperator-enum) *enum* | The comparison operator to apply. |
| `values` | List of `string` | The values to compare the target field against (at least one). Each value is validated at creation against the target field's type: for an enum the value must be a valid enum name (e.g. `STATE_SUCCEEDED`); for numeric/bool fields it must parse as that type. |

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
| `request_template` | `object` | Optional JSON object that reshapes the delivered body. Any string inside the object may embed `{{field_path}}` placeholders that are substituted with values from the event at delivery time. The object can be nested (objects and arrays), and non-string values (numbers, booleans, `null`) are copied through unchanged. Placeholders may reference only `robot_id` or a **scalar** field under `state.*`. If unspecified, the body is the default [envelope](#payload). See [Customizing the payload](#customizing-the-payload-templates) for the full rules. |
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
| `enabled` | `bool` | Whether the subscription is currently active. Becomes `false` if Bear [auto-disables](#auto-deactivation) the subscription after sustained delivery failures. |
| `created_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | When the subscription was created. |
| `updated_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | When the subscription was last updated. |
| `disabled_reason` | `string` | Set only when `enabled` is `false`. Explains why the subscription was [auto-disabled](#auto-deactivation), e.g. `"auto-disabled after 20 consecutive delivery failures"`. Absent while the subscription is active. |
| `disabled_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | When the subscription was disabled. Set together with `disabled_reason`; absent while the subscription is active. |

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
          },
          {
            "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
            "distributorId": "acme-robotics",
            "url": "https://stale.example.com/bear/webhooks",
            "eventType": "battery",
            "selector": {
              "locationIds": { "ids": ["1D9X"] }
            },
            "enabled": false,
            "disabledReason": "auto-disabled after 20 consecutive delivery failures",
            "disabledAt": "2026-06-17T09:30:00Z",
            "createdAt": "2026-06-10T12:00:00Z",
            "updatedAt": "2026-06-17T09:30:00Z"
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

### Custom headers

The `headers` map in [`WebhookOptions`](#webhookoptions) is attached to **every**
delivery for the subscription. Use it to authenticate the request on your side —
deliveries are not signed, so a custom header carrying a shared secret or bearer
token (verified together with the delivery arriving over HTTPS) is how your
endpoint confirms a request really came from Bear.

Keep the following in mind:

- **Bear's headers win on conflict.** The headers in the table above are applied
  *after* your custom headers, so you cannot override `Content-Type`,
  `X-Bear-Webhook-Id`, or `X-Bear-Webhook-Event-Id`. (Header names are
  case-insensitive.)
- **Header values are static.** `{{field_path}}` placeholders are substituted
  only in the [body template](#customizing-the-payload-templates), never in
  header values — a placeholder in a header is sent literally.
- **Keep them minimal.** Send only what your receiver needs (e.g. one auth
  header); a limit on the number and total size of custom headers may be
  enforced.

### Retries

A delivery is considered **successful** when your endpoint responds with a `2xx`
status. Anything else is a failure, classified as:

- **Retryable** — network errors, request timeouts, and the statuses `408`,
  `425`, `429`, and any `5xx`. These are retried with exponential backoff.
- **Permanent** — every other non-`2xx` response (most `4xx`, and `3xx`
  redirects, which are not followed). The subscription's URL and headers are
  fixed at creation, so these are not retried.

A delivery's **final outcome** is a failure when it exhausts its retries
(retryable) or fails immediately (permanent). Final failures drive
[auto-deactivation](#auto-deactivation).

### Auto-deactivation

To protect endpoints that have stopped accepting deliveries, Bear automatically
disables a subscription after a sustained run of failures.

- Bear counts **consecutive final-failure** deliveries per subscription. Any
  successful (`2xx`) delivery resets the count to zero.
- When the count reaches the threshold (**currently 20**; subject to change),
  the subscription is disabled: `enabled` flips to `false`, and
  [`disabled_reason`](#webhooksubscription) / `disabled_at` are set (e.g.
  `"auto-disabled after 20 consecutive delivery failures"`). No further events
  are delivered.

!!! warning "A disabled subscription cannot be re-enabled in place"
    The API has no update or re-enable call at the moment. Once a subscription is disabled,
    fix your endpoint, then [delete](#deletewebhook) the subscription and
    [create](#createwebhook) a new one. Poll [`ListWebhooks`](#listwebhooks) and
    watch for `enabled: false` (with a `disabledReason`) to detect deactivation;
    returning `2xx` promptly is the simplest way to avoid it.

### Payload

By default, the delivered body is a JSON **envelope** that wraps the event under
a `state` field:

| Field | Type | Description |
|------|------|-------------|
| `robot_id` | `string` | The robot the event originated from. |
| `state` | `object` | The event payload, whose shape depends on the subscription's `event_type`. |

```json
{
  "robot_id": "pennybot-abc123",
  "state": {
    "...": "event-specific payload"
  }
}
```

!!! warning "Delivered bodies use `snake_case`"
    Unlike the JSON request/response of the management API above (which uses
    `camelCase` keys such as `robotId` and `eventType`), the **delivered webhook
    body uses the original `snake_case` proto field names** — `robot_id`,
    `current_mission_index`, `battery_state`, `charge_percent`, and so on. This
    is the same vocabulary you use for `filter.field` paths and
    `request_template` placeholders, so a path like
    `state.current_mission.state` addresses the body exactly as delivered. Build
    your receiver (and any signature/secret check) against `snake_case` keys.

!!! note "Every scalar field is always present"
    The `state` object is emitted with defaults filled in: scalar fields always
    appear, and
    enum fields are rendered by **name** (e.g. `"STATE_SUCCEEDED"`,
    `"STATE_UNKNOWN"`). Optional nested messages that are not set — such as
    `current_mission` / `current_goal` when nothing is active — are delivered as
    `null`. A `filter` or template path that points into a `null` message
    resolves to an empty value, so it never matches an `IN` filter.

To deliver a differently shaped body, supply a `request_template` in
[`WebhookOptions`](#webhookoptions) when creating the subscription — see
[Customizing the payload](#customizing-the-payload-templates).

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
| `missions` | List of [`MissionState`](Mission.md#missionstate) | The robot's full mission queue, in assignment order. |
| `current_mission_index` | `int32` | Index of the currently active mission in `missions`. |
| `current_mission` | [`MissionState`](Mission.md#missionstate) | The currently active mission, i.e. `missions[current_mission_index]`. |
| `current_goal` | [`Goal`](LocalizationAndNavigation.md#goal-goal-required) | The currently active goal of the active mission. |

```json
{
  "robot_id": "pennybot-abc123",
  "state": {
    "metadata": {
      "timestamp": "2025-04-01T17:20:00Z",
      "sequence_number": 42
    },
    "missions": [
      {
        "mission_id": "cbd47ab1-df21-479e-9f72-677b81ab55b0",
        "state": "STATE_SUCCEEDED"
      }
    ],
    "current_mission_index": 0,
    "current_mission": {
      "mission_id": "cbd47ab1-df21-479e-9f72-677b81ab55b0",
      "state": "STATE_SUCCEEDED"
    },
    "current_goal": {
      "destination_id": "dest-123"
    }
  }
}
```

> The example is abridged — `MissionState` and `Goal` carry additional fields
> (see the linked types). 

#### `"battery"` payload

The `state` field carries the following fields. The nested `BatteryState` type is
the same as the one returned by the Robot Status API.

| Field | Message Type | Description |
|------|------|-------------|
| `metadata` | [`EventMetadata`](Mission.md#eventmetadata) | Event metadata, including `timestamp` and `sequence_number`. |
| `battery_state` | [`BatteryState`](RobotStatus.md#batterystate) | The robot's current battery state. |

```json
{
  "robot_id": "pennybot-abc123",
  "state": {
    "metadata": {
      "timestamp": "2025-04-01T17:20:00Z",
      "sequence_number": 87
    },
    "battery_state": {
      "charge_percent": 85,
      "state": "STATE_CHARGING",
      "charge_method": "CHARGE_METHOD_CONTACT"
    }
  }
}
```

-----------

## Filtering events

A [`filter`](#fieldfilter) narrows a subscription to the events you care about,
so your endpoint only receives the deliveries it needs. Without a filter, every
event of the subscribed type is delivered.

**How to write a filter**

1. Pick the field you want to match from the [event payload](#event-types),
   written as a `state.`-prefixed **snake_case** path — the same names that
   appear in the delivered body. For example, a mission's outcome is
   `state.current_mission.state`.
2. The path must resolve to a **scalar** value (string, number, bool, enum, or
   timestamp). You cannot match a whole object, list, or map. To reach an
   element of a list, append its index: `state.missions.0.mission_id`.
3. Set `operator` to `FILTER_OPERATOR_IN` (the only operator today) and list one
   or more `values`. The event is delivered when the field's value **equals one
   of** the listed values. Enum fields compare by name (`STATE_SUCCEEDED`).

The `field` path and every `value` are checked when you call
[`CreateWebhook`](#createwebhook); an unknown path, a non-scalar target, or a
value that doesn't match the field's type is rejected with `INVALID_ARGUMENT`,
so a saved subscription is always well-formed.

!!! tip "Common patterns"
    - **Only terminal mission states** — `field: state.current_mission.state`,
      `values: ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]`.
    - **Only while discharging** — `field: state.battery_state.state`,
      `values: ["STATE_DISCHARGING"]`.

    A subscription holds **one** condition; there is no `AND`/`OR`. To combine
    conditions, or to scope by robot, create multiple subscriptions or use the
    [`selector`](#webhookrobotselector). `robot_id` is not a filterable field.

=== "JSON"
    ```js
      {
        "field": "state.current_mission.state",
        "operator": "FILTER_OPERATOR_IN",
        "values": ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]
      }
    ```

-----------

## Customizing the payload (templates)

By default the body is the [envelope](#payload). To deliver a shape your system
expects instead, set [`request_template`](#webhookoptions) when creating the
subscription.

A template is any JSON object. The rules are:

- **Placeholders.** Any string may contain `{{field_path}}` placeholders.
  At delivery time each placeholder is replaced with the value at that path in
  the event. Paths use the same **snake_case**, `state.`-prefixed form as
  filters, plus the top-level `robot_id`.
- **Interpolation.** A placeholder can sit inside surrounding text, e.g.
  `"robot {{robot_id}} finished"`.
- **Structure is preserved.** Nest objects and arrays freely; the output keeps
  your shape. Non-string values (numbers, booleans, `null`) are copied through
  unchanged.
- **Scalar leaves only.** A placeholder must resolve to `robot_id` or a scalar
  field under `state.*`. Pointing at a whole message, list, or map is rejected
  at creation with `INVALID_ARGUMENT`. (Index into a list to reach a scalar:
  `{{state.missions.0.mission_id}}`.)
- **Missing values become empty.** If a path is absent at delivery time, the
  placeholder renders as an empty string `""`.

Given the mission payload above, this template:

=== "Template"
    ```js
      {
        "robot": "{{robot_id}}",
        "summary": "mission {{state.current_mission.mission_id}} ended as {{state.current_mission.state}}",
        "details": {
          "outcome": "{{state.current_mission.state}}",
          "goal": "{{state.current_goal.destination_id}}"
        },
        "source": "bear",
        "version": 1
      }
    ```

produces this delivered body:

=== "Delivered body"
    ```js
      {
        "robot": "pennybot-abc123",
        "summary": "mission cbd47ab1-df21-479e-9f72-677b81ab55b0 ended as STATE_SUCCEEDED",
        "details": {
          "outcome": "STATE_SUCCEEDED",
          "goal": "dest-123"
        },
        "source": "bear",
        "version": 1
      }
    ```

!!! note
    Custom headers from [`WebhookOptions`](#webhookoptions) and the Bear-generated
    [delivery headers](#delivery-headers) are sent regardless of the template —
    the template only reshapes the JSON body.
