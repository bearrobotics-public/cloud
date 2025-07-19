!!! warning

    - This REST API currently supports a subset of our gRPC APIs and is under active development.
    - API specifications may change without prior notice during the development phase.

Starting from the v1.0 API, REST endpoints are also available for unary RPCs. Given an identical payload and target RPC, a REST API call would perform the exact same operation with that of gRPC.

!!! note

    Refer to the [translation table](https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto) to see how a gRPC status code maps to a HTTP status code and vice versa.

To test out the APIs on Swagger, a **valid JWT token** has to be passed to the "Authorize"
Button below. You can obtain one via the command below with a credentials JSON file.

```sh
curl -X POST https://api-auth.bearrobotics.ai/authorizeApiAccess \
    -H "Content-Type: application/json" \
    -d $(cat /path/to/credentials.json)'
```
Schemas for request and response bodies are available both under each endpoint (click on the "Schema" text shown in "Request body" and "Responses" section) and at the bottom of this page.

!!swagger openapi.yaml!!
