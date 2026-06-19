!!! warning

    - This REST API currently supports a subset of our gRPC APIs and is under active development.

A REST version of the unary gRPC endpoints are available.

!!! note

    - Refer to the [translation table](https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto) to see how a gRPC status code maps to a HTTP status code and vice versa.
    - In cases of an error response (non-200 status code), the detailed error message along with the gRPC status code will be accessible under the `grpc-message` and `grpc-status` **response headers**.
    - Schemas for request and response bodies are available both under the **gRPC API section** (since they share the same request/response body format) or under **each endpoint** (click on the "Schema" text shown in "Request body" and "Responses" section).

To test out the APIs on Swagger, a **valid JWT token** has to be passed to the "Authorize"
Button below. You can obtain one via the command below with a credentials JSON file.

```sh
curl -X POST https://api-auth.bearrobotics.ai/authorizeApiAccess \
    -H "Content-Type: application/json" \
    -d $(cat /path/to/credentials.json)'
```

## Postman Collection

A Postman collection covering all unary (non-streaming) REST endpoints in v1.3 is available
for quick, code-free testing.

[:material-open-in-new: View published documentation](https://documenter.getpostman.com/view/44195110/2sBXwto8ex){ .md-button .md-button--primary }
[:material-download: Download the collection](bear-public-rest-api-v1.3.postman_collection.json){ .md-button download="bear-public-rest-api-v1.3.postman_collection.json" }

**To use it:**

1. In Postman, choose **Import** and select the downloaded
   `bear-public-rest-api-v1.3.postman_collection.json` file.
2. Open the collection's **Variables** tab and set:
    - `base_url` — e.g. `https://api.bearrobotics.ai`
    - `auth_url` — e.g. `https://api-auth.bearrobotics.ai`
    - `api_key`, `api_secret`, `scope` — the values from your credentials JSON file
    - `robot_id` (and `location_id` / `map_id` as needed)
3. Run **Authentication → Get JWT Token (API Key)**. The returned token is saved automatically
   to the `bearer_token` variable and reused by all other requests.

!!! note

    Streaming RPCs (`SubscribeMissionStatus`, `SubscribeOnlineStatus`,
    `SubscribeEmergencyStopStatus`, etc.) are not part of the Postman
    collection — see the gRPC API reference for those.

## Webhooks

Webhook subscriptions are managed over REST as well — `POST /v1/webhook/create`,
`POST /v1/webhook/list`, and `POST /v1/webhook/delete`. The request and response
bodies use the same JSON format shown elsewhere on this page.

These endpoints are **not** part of the Swagger explorer or Postman collection
below. They are documented in full on the dedicated **[Webhooks](Webhooks.md)**
page, which also covers what only applies to webhooks: robot selection, event
filtering, payload templating, custom headers, the delivered request format, and
automatic deactivation after repeated delivery failures.

!!swagger openapi-v1-3.yaml!!
