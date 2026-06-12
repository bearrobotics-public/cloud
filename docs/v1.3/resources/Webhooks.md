# Webhooks

Webhooks let you receive robot events as outbound HTTP requests instead of
maintaining a long-lived streaming connection. Once a subscription is created,
Bear delivers an HTTP request to your configured URL each time an event matching
the subscription's type and filter occurs on any of the selected robots.

Use webhooks when you want events pushed to your own HTTPS endpoint without
keeping a streaming client connected.

### Capabilities

- **Event-driven delivery** — an HTTP request is sent to your URL whenever a matching event occurs.
- **Robot selection** — target robots by explicit robot IDs, or by location (resolved to robots via Fleet Info).
- **Field filters** — restrict deliveries to events whose fields match conditions you define.
- **Custom payloads** — shape the delivered body with a request template and attach custom HTTP headers.

!!! info
    Detailed endpoint reference for managing webhook subscriptions
    (create / list / delete) will be published when the Webhooks API is released.
