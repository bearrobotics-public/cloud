# Authentication and Authorization with API Key

Bear Robotics provides clients with an API key in JSON format. When this API key is submitted to the authentication server, it returns a JSON Web Token (JWT), which serves as the credential required for authenticating requests to the Bear Cloud API server via gRPC.

## API Key Format

The credentials are provided in the following JSON format:

```json
{
  "api_key": "your-api-key",
  "secret": "your-api-secret",
  "scope": "your-scope"
}
```

All three fields must be correctly matched for the credentials to be authorized:

- `api_key`: A unique identifier for the credentials within our system.
- `scope`: A value fixed at the time the API key is issued. It represents the distributor to which the API key is authorized.
- `secret`: A passcode associated with the API key. It is essential that the secret be stored securely.

## JWT Generation Examples:

=== "CLI"

    ```
    curl -X POST https://api-auth.bearrobotics.ai/authorizeApiAccess \
        -H "Content-Type: application/json" \
        -d $(cat /path/to/credentials.json)'
    ```

=== "Java"

    Please see the Java client code example in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main/examples/java).

=== "Python"

    Please see the Python client code example in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main/examples/python).

## JWT Usage

Once the JWT is obtained, it must be included in the metadata of each outgoing gRPC request using the following format:

```
Authorization: Bearer <JWT>
```

For details, please see the following documentation:

- [gRPC Metadata guide](https://grpc.io/docs/guides/metadata/)

- [gRPC Authentication guide](https://grpc.io/docs/guides/auth/)

#### JWT Usage Examples

=== "CLI"

    ```
    grpcurl -H "authorization: Bearer $(cat /path/to/jwt)" -d '{}' \
    api.bearrobotics.ai:443 bearrobotics.api.v1.services.cloud.APIService.ListRobotIDs
    ```

=== "Java"

    Please see the Java client code example in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main/examples/java).

=== "Python"

    Please see the Python client code example in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main/examples/python).

## JWT Expiry

The JWT has an expiration time, specified by the `exp` field. To maintain uninterrupted access, it is recommended to refresh the JWT periodically, ideally every 30 minutes.

#### JWT Refresh Examples

=== "Java"

    Please see the Java client code example in our [public repository](https://github.com/bearrobotics-public/cloud/tree/main/examples/java).

## Security

All connections to the Bear Cloud API server are secured via TLS. The API server's certificates are signed by Google, a trusted Certificate Authority recognized by most systems.

## Python References

- [Token-based authentication in gRPC](https://github.com/grpc/grpc/blob/master/examples/python/auth/README.md#token-based-authentication)
- [Adding a JWT as part of the request header](https://grpc.github.io/grpc/python/grpc.html#grpc.access_token_call_credentials)
