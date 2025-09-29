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

!!swagger openapi-v1-1.yaml!!

