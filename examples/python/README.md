# Bear Robotics Python Examples

This directory contains Python examples for interacting with the Bear Robotics API.

## Prerequisites

- Python 3.x
- pip (Python package manager)

## Directory Structure

- `main.py` - Example script demonstrating how to create a mission or subscribe to mission status
- `auth.py` - Authentication utilities
- `unary_wrapper.py` - Wrapper for unary gRPC calls with retry logic
- `streaming_wrapper.py` - Wrapper for streaming gRPC calls with retry logic
- `bearrobotics/api/` - Protocol Buffer definition files
- `google/api` - Required Protocol Buffer definition files from Google
- `generated/` - Generated Protocol Buffer files (created by `generate_protos`)
- `resources/` - Credentials JSON

## Setup

1. Clone this repository:
    ```
    git clone https://github.com/bearrobotics-public/cloud.git
    cd cloud/examples/python
    ```

2. Set up API credentials:
   Create a file at `resources/credentials.json` with the following format:
   ```json
   {
     "api_key": "your-api-key",
     "secret": "your-api-secret",
     "scope": "Bear"
   }
   ```

3. Generate the Protocol Buffer files:
    ```
    ./generate_protos
    ```
    This will create a `generated` directory containing all the necessary Protocol Buffer files.

## Usage

The main example script (`main.py`) demonstrates how to:
1. Connect to the Bear Robotics API
2. Create a mission for a robot / subscribe to mission status for a robot
3. Handle authentication and retries

To run the example:
```bash
python3 main.py <robot_id> ["unary"|"streaming"]
```

## API Authentication

The Bear Robotics API authentication flow uses JWT (JSON Web Tokens) for secure API access:

1. This example stores credentials in `resources/credentials.json`
2. The `auth.py` module handles authentication by:
   - Loading credentials from the JSON file
   - Making a POST request to `https://api-auth.bearrobotics.ai/authorizeApiAccess`
   - Converting the response into a gRPC access token
3. The authentication token is included as a header in all API calls
4. Token expiration is handled automatically by getting a new token when needed

## Long-Running Connections

This example demonstrates best practices for maintaining long-running gRPC connections:

1. Retry/Reconnection Logic: The client implements automatic retries/reconnection if a unary request/streaming connection fails. This allows it to recover from network issues or server-side disruptions.

2. Automatic Token Refreshing: The client handles authentication errors by getting a new JWT token before retrying, avoiding RPC failures due to token expiry.

3. Keep-Alive Settings: The gRPC channel is configured with keep-alive settings to detect and recover from "half-open" connections, where one side believes the connection is still active but it's actually broken.

## Notes

- `credentials.json` is listed in `.gitignore` to prevent committing credentials.
- All imports in the generated files are prefixed with `generated.` to ensure proper package resolution.*
- *Google Protocol Buffer imports are not prefixed in order to use the system-installed protobuf package.