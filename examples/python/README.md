# Bear Robotics Python Examples

This directory contains Python examples for interacting with the Bear Robotics API.

## Prerequisites

- Python 3.x
- pip (Python package manager)

## Setup

1. Install the required Python packages:
```bash
pip install grpcio-tools
```

2. Generate the Protocol Buffer files:
```bash
./generate_protos
```

This will create a `generated` directory containing all the necessary Protocol Buffer files.

## Directory Structure

- `main.py` - Example script demonstrating how to create a mission
- `auth.py` - Authentication utilities
- `unary_wrapper.py` - Wrapper for unary gRPC calls with retry logic
- `streaming_wrapper.py` - Wrapper for streaming gRPC calls
- `generated/` - Generated Protocol Buffer files (created by `generate_protos`)
- `proto/` - Protocol Buffer definition files
- `resources/` - Additional resources used by the examples

## Usage

The main example script (`main.py`) demonstrates how to:
1. Connect to the Bear Robotics API
2. Create a mission for a robot / Subscribe to mission status for a robot
3. Handle authentication and retries

To run the example:
```bash
python3 main.py <robot_id> ["unary"|"streaming"]
```

## Protocol Buffer Generation

The `generate_protos` script handles the generation of Protocol Buffer files. It:
1. Creates a `generated` directory if it doesn't exist
2. Generates Python files from the Protocol Buffer definitions
3. Fixes import paths to work with the generated package structure

If you need to regenerate the Protocol Buffer files, simply run:
```bash
./generate_protos
```

## Notes

- The generated Protocol Buffer files are stored in the `generated` directory
- All imports in the generated files are prefixed with `generated.` to ensure proper package resolution
- Google Protocol Buffer imports are handled specially to use the system-installed protobuf package 