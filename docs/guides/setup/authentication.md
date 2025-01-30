# Authentication and Authorization with API Key

Clients are provided with an API key in JSON format. When this API key is submitted to the authentication server, it returns a JSON Web Token (JWT), which serves as the credential required for authenticating requests to the Bear Cloud API server via gRPC.

The JWT has an expiration time specified by the exp field. To maintain uninterrupted access, it is recommended to refresh the JWT periodically, ideally every 15 to 20 minutes.

## API Key Format

The credentials are provided in the following JSON format:

```json
{
  "api_key": "deca1611-fb00-40b6-be4b-9ed797ae1642",
  "scope": "YOUR_SCOPE",
  "secret": "2cd66c59-a845-4f16-ac1e-50b50e3878ec"
}
```
All three fields must be correctly matched for the credentials to be authorized:

- `api_key`: A unique identifier for the credentials within our system.
- `scope`: A value fixed at the time the API key is issued. It represents the distributor to which the API key is authorized.
- `secret`: A passcode associated with the API key. It is essential that the secret be stored securely.

## JWT Generation Example (with curl):

```
curl -X POST https://api-auth.bearrobotics.ai/authorizeApiAccess \
     -H "Content-Type: application/json" \
     -d '{"api_key":"deca1611-fb00-40b6-be4b-9ed797ae1642",
          "scope":"YOUR_SCOPE",
          "secret":"2cd66c59-a845-4f16-ac1e-50b50e3878ec"}'
```

## JWT Usage

Once the JWT is obtained, it must be included in the Authorization header of each outgoing gRPC request using the following format:

```
Authorization: Bearer <JWT>
```

## Security

All connections to the Bear Cloud API server are secured via TLS. The API server's certificates are signed by Google, a trusted Certificate Authority recognized by most systems.

## Example Client Code (gRPC)

### Go ([grpc-go](https://github.com/grpc/grpc-go/blob/master/Documentation/grpc-auth-support.md))

```go
type TokenCreds struct {
   JWT             atomic.Value
   TransportSecurity bool
}

func GetToken(url string, creds string) (string, error) {
   resp, err := http.Post(url, "application/json", bytes.NewBufferString(creds))
   if err != nil {
       return "", err
   }
   defer resp.Body.Close()

   if resp.StatusCode != 200 {
       return "", fmt.Errorf("failed to get new token: %d", resp.StatusCode)
   }
   body, err := io.ReadAll(resp.Body)
   if err != nil {
       return "", err
   }
   return string(body), nil
}

// GetRequestMetadata gets the current gRPC request metadata,
// with current token. This func gets called before every gRPC query.
func (t *Token) GetRequestMetadata(_ context.Context, _ ...string)
(map[string]string, error) {
  if jwtToken := t.JWT.Load(); jwtToken != nil {
    return map[string]string{
      "Authorization": "Bearer " + jwtToken.(string),
    }, nil
 }
 return nil, fmt.Errorf("token is not set")
}

// RequireTransportSecurity indicates whether the credentials requires
// transport security.
func (t *Token) RequireTransportSecurity() bool {
  return t.TransportSecurity
}

func main() {
   var creds = `{"api_key": "deca1611-fb00-40b6-be4b-9ed797ae1642","scope": "YOUR_SCOPE","secret": "2cd66c59-a845-4f16-ac1e-50b50e3878ec"}`
   token := TokenCreds{TransportSecurity: true}

   ctx, cancelRefresher := context.WithCancel(context.Background())
   go func() {
       ticker := time.NewTicker(10 * time.Minute)
       defer ticker.Stop()

       for {
           select {
           case <-ctx.Done():
               slog.Info("Refresher exiting...")
               return
           case <-ticker.C:
               jwt, err := GetToken(
                   "https://api-auth.bearrobotics.ai/authorizeApiAccess", creds)
               if err != nil {
                   slog.Error("Failed to get new token", "error", err)
               }
               if err == nil {
                   token.JWT.Store(jwt)
               }
           }
       }
   }()

   tls_creds := credentials.NewTLS(&tls.Config{})
   conn, err := grpc.Dial(
       serverAddress,
       grpc.WithTransportCredentials(tls_creds),
       grpc.WithPerRPCCredentials(token))


   // Make gRPC queries using conn...


   // Cancel refresher when not needed.
   cancelRefresher()
}
```

### Python

- [Token-based authentication in gRPC](https://github.com/grpc/grpc/blob/master/examples/python/auth/README.md#token-based-authentication)
- [Adding a JWT as part of the request header](https://grpc.github.io/grpc/python/grpc.html#grpc.access_token_call_credentials)
