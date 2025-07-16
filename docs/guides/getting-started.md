# Getting Started

This section aims to provide a fast, code-free way of invoking the APIs. For complete, runnable end-to-end examples in Python and Java, refer to the `examples` under the [**public repository**](https://github.com/bearrobotics-public/cloud/tree/main).

## Get Your Credentials

As a first step, we need to obtain a JWT token from the Auth endpoint, as all APIs require it in the form of a Bearer authentication. Make sure your credentials JSON file is ready in a known path (See the [Authentication Guide](authentication.md) for more information about credentials).

The command below both prints out the token and writes it to a file named `bear-creds.jwt`.

```sh
curl -X POST https://api-auth.bearrobotics.ai/authorizeApiAccess \
    -H "Content-Type: application/json" \
    -d $(cat /path/to/credentials.json)' | tee bear-creds.jwt
```
Once the authentication token and the target robots are ready, you're all set!

## Invoking the APIs
There are two different ways to invoke most APIs, **gRPC** and **plain HTTP**.

gRPC supports a wider range of RPCs with its support for server-side streaming APIs (e.g. `SubscribeMissionStatus`), and provides a typed request/response message definition. However, it may be relatively complex to set up the initial development environment (compiling protobuf files, connection management, etc.) compared to sending plain HTTP requests.

### gRPC

!!! note

    To invoke the gRPC APIs in the command line, [`grpcurl`](https://github.com/fullstorydev/grpcurl) is used in the examples below. Make sure the CLI tool is installed (either as a standalone binary or a docker image) in your environment prior to testing.

The examples below are only a small subset of the available APIs. A full list of the APIs and their documentation can be found under the [v1.0 gRPC API section](../v1.0/resources/Mission.md).

#### Unary RPCs
Listing the list of robots for a given location with `ListRobotIDs` RPC:
```sh
grpcurl -H "authorization: Bearer $(cat bear-dev.jwt)" -d '{
    "filter": {
        "locationId": "<location-id>"
    }
}' api.bearrobotics.ai:443 bearrobotics.api.v1.services.cloud.APIService.ListRobotIDs
```
Creating a mission for a target robot with the `CreateMission` RPC:
```sh
grpcurl -H "authorization: Bearer $(cat bear-creds.jwt)" -d '
  "robot_id": "pennybot-123456",
  "mission": {
    "base_mission": {
      "navigate_mission": {
        "goal": {
          "destination_id": "<destination-id>"
        }
      }
    }
  }
}' api.bearrobotics.ai:443 bearrobotics.api.v1.services.cloud.APIService.CreateMission
```

#### Server-side Streaming RPCs

Subscribe to updates in mission status with the `SubscribeMissionStatus` RPC.
```sh
grpcurl -H "authorization: Bearer $(cat bear-creds.jwt)" -d '
    "selector": {
      "robot_ids": {
        "ids": "pennybot-123456"
      }
    }
}' api.bearrobotics.ai:443 bearrobotics.api.v1.services.cloud.APIService.SubscribeMissionStatus
```
### Plain HTTP

A REST version of the APIs are also available only for **unary** RPCs. The full documentation of the available APIs can be found in our [Swagger page](../v1.0/resources/RestAPI.md).

A simple example request below lists all robots for a given location ID:

```sh
curl -X POST api.bearrobotics.ai/v1/robot-ids/list \
    -H "Content-Type: application/json" \
    -H "authorization: Bearer $(cat bear-creds.jwt)" \
    -d '{
            "filter": {
                "locationId": "<location-id>"
            }
        }'
```

This example request below creates a single-destination mission for `pennybot-123456`:

```sh
curl -X POST api.bearrobotics.ai/v1/mission/create \
    -H "Content-Type: application/json" \
    -H "authorization: Bearer $(cat bear-creds.jwt)" \
    -d '{
          "robotId": "pennybot-123456",
          "mission": {
            "baseMission": {
              "navigateMission": {
                "goal": {
                  "destinationId": "<destination-id>"
                }
              }
            }
          }
        }'
```

