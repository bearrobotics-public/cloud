# Webhooks

Webhooks let you receive robot events as outbound HTTP requests instead of
maintaining a long-lived streaming connection. Once a subscription is created,
Bear delivers an HTTP request to your configured URL each time an event matching
the subscription's type and filter occurs on any of the selected robots.

The delivery payload is customizable via a request template, and you may attach
custom HTTP headers to every delivery request.

!!! note
    Supported `event_type` values are `"mission"` and `"battery"`.

------------
## CreateWebhook
Creates a webhook subscription. After creation, Bear delivers an HTTP request to
the given URL each time an event matching the given type and filter occurs on any
of the selected robots.

### Request

##### distributor `string` `required`
Human-readable identifier code assigned to the distributor.

##### url `string` `required`
The URL to deliver webhook events to.

##### event_type `string` `required`
Event type to subscribe to. Supported values:

| Value | Description |
|------|-------------|
| `mission` | Mission status events. |
| `battery` | Battery status events. |

##### selector `WebhookRobotSelector` `required`
Specifies which robots this webhook targets. The caller must provide **exactly one**
of the two options.

| Field (*oneof*) | Message Type | Description |
|------|------|-------------|
| `robot_ids` | [`RobotIdList`](#robotidlist) | Explicit list of robot IDs. |
| `location_ids` | [`LocationIdList`](#locationidlist) | List of location IDs; the server resolves these to robot IDs via Fleet Info. |

#### RobotIdList
| Field | Message Type | Description |
|------|------|-------------|
| `ids` | *repeated* `string` | List of robot IDs. |

#### LocationIdList
| Field | Message Type | Description |
|------|------|-------------|
| `ids` | *repeated* `string` | List of location IDs. |

##### filter `FieldFilter` `optional`
Optional filter to control which events trigger delivery. See [FieldFilter](#fieldfilter).

##### options `WebhookOptions` `optional`
Optional delivery customizations (template, headers, description). See [WebhookOptions](#webhookoptions).

#### FieldFilter
Defines a single field-level condition for event filtering.

| Field | Message Type | Description |
|------|------|-------------|
| `field` | `string` | Target field path to apply the filter on. The path should begin with `state`, followed by a `.`-separated list of field names determined by the payload structure. |
| `operator` | [`FilterOperator`](#filteroperator-enum) *enum* | Comparison operator to use. |
| `values` | *repeated* `string` | Values to match against the target field. |

#### FilterOperator `enum`
| Name | Number | Description |
|------|--------|-------------|
| FILTER_OPERATOR_UNKNOWN | 0 | Default value. It means the `operator` field is not set. |
| FILTER_OPERATOR_IN | 1 | Matches when the field value is contained in the provided values list. |

#### WebhookOptions
Optional delivery customizations for a webhook subscription.

| Field | Message Type | Description |
|------|------|-------------|
| `description` | `string` *optional* | Optional human-readable description of the webhook. |
| `request_template` | [`Struct`](https://protobuf.dev/reference/protobuf/google.protobuf/#struct) *optional* | Optional request template for customizing the delivered payload. Keys are output field names; values are template strings with placeholders referencing event fields using `{{field_path}}` syntax. If unspecified, the payload is the original message in JSON format. |
| `headers` | `map<string, string>` | Optional custom HTTP headers to include in webhook delivery requests. |

##### JSON Request Example
=== "JSON"
    ```js
      {
        "distributor": "acme-corp",
        "url": "https://example.com/bear/webhook",
        "eventType": "mission",
        "selector": {
          "robotIds": {
            "ids": ["pennybot-abc123", "pennybot-123abc"]
          }
        },
        "filter": {
          "field": "state.mission_state.state",
          "operator": "FILTER_OPERATOR_IN",
          "values": ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]
        },
        "options": {
          "description": "Notify on terminal mission states",
          "requestTemplate": {
            "robot": "{{state.robot_id}}",
            "mission": "{{state.mission_state.mission_id}}",
            "outcome": "{{state.mission_state.state}}"
          },
          "headers": {
            "X-Api-Key": "secret-token"
          }
        }
      }
    ```

### Response

##### webhook `WebhookSubscription`
The newly created webhook subscription. See [WebhookSubscription](#webhooksubscription).

##### JSON Response Example
=== "JSON"
    ```js
      {
        "webhook": {
          "id": "wh-7f3a2b10",
          "distributorId": "acme-corp",
          "description": "Notify on terminal mission states",
          "url": "https://example.com/bear/webhook",
          "headers": { "X-Api-Key": "secret-token" },
          "eventType": "mission",
          "selector": {
            "robotIds": { "ids": ["pennybot-abc123", "pennybot-123abc"] }
          },
          "filter": {
            "field": "state.mission_state.state",
            "operator": "FILTER_OPERATOR_IN",
            "values": ["STATE_SUCCEEDED", "STATE_FAILED", "STATE_CANCELED"]
          },
          "enabled": true,
          "createdAt": "2026-06-04T10:00:00Z",
          "updatedAt": "2026-06-04T10:00:00Z"
        }
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | The client supplied a request with invalid format. This covers empty `url`, unsupported `event_type`, a `selector` that does not set exactly one of `robot_ids` / `location_ids`, or a malformed `filter`. |
| `PERMISSION_DENIED` | Attempting to subscribe to a `robot_id` or `location_id` you don't own. |
| `INTERNAL` | The request failed to execute due to an internal error. Client should retry. |

------------
## ListWebhooks
Lists all webhook subscriptions for a distributor.

### Request

##### distributor `string` `required`
Human-readable identifier code assigned to the distributor.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "distributor": "acme-corp"
      }
    ```

### Response

##### webhooks `repeated WebhookSubscription`
The list of webhook subscriptions for the distributor. See [WebhookSubscription](#webhooksubscription).

#### WebhookSubscription
Represents a registered webhook configuration.

| Field | Message Type | Description |
|------|------|-------------|
| `id` | `string` | Unique identifier of the webhook subscription. |
| `distributor_id` | `string` | The distributor (client organization) ID that owns this subscription. |
| `description` | `string` *optional* | Optional human-readable description of the webhook. |
| `url` | `string` | The URL to deliver webhook events to. |
| `headers` | `map<string, string>` | Optional custom HTTP headers to include in webhook delivery requests. |
| `event_type` | `string` | Event type subscribed to (e.g., `mission`, `battery`). |
| `selector` | [`WebhookRobotSelector`](#selector-webhookrobotselector-required) | The robot selector that was provided when creating the subscription. |
| `filter` | [`FieldFilter`](#fieldfilter) | Optional filter to control which events trigger delivery. |
| `request_template` | [`Struct`](https://protobuf.dev/reference/protobuf/google.protobuf/#struct) | Optional request template for customizing the delivered payload. |
| `enabled` | `bool` | Whether this subscription is currently active. |
| `created_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp when the subscription was created. |
| `updated_at` | [`Timestamp`](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/timestamp.proto) | Timestamp when the subscription was last updated. |

##### JSON Response Example
=== "JSON"
    ```js
      {
        "webhooks": [
          {
            "id": "wh-7f3a2b10",
            "distributorId": "acme-corp",
            "url": "https://example.com/bear/webhook",
            "eventType": "mission",
            "selector": {
              "robotIds": { "ids": ["pennybot-abc123"] }
            },
            "enabled": true,
            "createdAt": "2026-06-04T10:00:00Z",
            "updatedAt": "2026-06-04T10:00:00Z"
          }
        ]
      }
    ```

### Errors
| ErrorCode  | Description |
|------------|-------------|
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `distributor` is not empty. |
| `PERMISSION_DENIED` | Attempting to list webhooks for a distributor you don't own. |
| `INTERNAL` | The request failed to execute due to an internal error. Client should retry. |

------------
## DeleteWebhook
Deletes an existing webhook subscription by ID (soft delete).

### Request

##### id `string` `required`
Unique identifier of the webhook subscription to delete.

##### distributor `string` `required`
Human-readable identifier code assigned to the distributor.

##### JSON Request Example
=== "JSON"
    ```js
      {
        "id": "wh-7f3a2b10",
        "distributor": "acme-corp"
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
| `INVALID_ARGUMENT` | Invalid request parameters. <br /> Tips: check that `id` and `distributor` are not empty. |
| `NOT_FOUND` | No webhook subscription found for the specified `id`. |
| `PERMISSION_DENIED` | Attempting to delete a webhook for a distributor you don't own. |
| `INTERNAL` | The request failed to execute due to an internal error. Client should retry. |
