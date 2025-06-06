import grpc
import sys

from auth import get_credentials
from unary_wrapper import retry_on_disconnect
from streaming_wrapper import stream_with_reconnect
from generated.bearrobotics.api.v1.core.annotation_pb2 import Goal
from generated.bearrobotics.api.v1.core.mission_pb2 import Mission, BaseMission, NavigateMission
from generated.bearrobotics.api.v1.core.fleet_selector_pb2 import RobotSelector
from generated.bearrobotics.api.v1.services.cloud.api_service_pb2 import CreateMissionRequest, SubscribeMissionStatusRequest
from generated.bearrobotics.api.v1.services.cloud.api_service_pb2_grpc import APIServiceStub

def create_mission(stub, robot_id, mission):
    return retry_on_disconnect(
        stub.CreateMission,
        CreateMissionRequest(robot_id=robot_id, mission=mission),
        get_credentials()
    )

def subscribe_mission_status(stub, robot_selector):
    return stream_with_reconnect(
        stub.SubscribeMissionStatus,
        SubscribeMissionStatusRequest(selector=robot_selector),
        get_credentials(),
        on_next=lambda x: print(x)
    )

def main():
    if len(sys.argv) < 1:
        print("Usage: python3 main.py <robot_id> [unary|streaming]")
        return 1

    robot_id = sys.argv[1]
    
    if len(sys.argv) > 2:
        mode = sys.argv[2]
    else:
        mode = "unary"

    # Default values
    host = "api.bearrobotics.ai";
    port = 443;
    destination_id = "Sajjad";

    channel = grpc.secure_channel(
        f"{host}:{port}",
        grpc.ssl_channel_credentials(),
        options=[
            ("grpc.keepalive_time_ms", 30000),
            ("grpc.keepalive_timeout_ms", 10000),
        ]
    )
    stub = APIServiceStub(channel)

    # Call the API
    if mode == "unary":
        mission = Mission(
            base_mission=BaseMission(
                navigate_mission=NavigateMission(
                goal=Goal(
                    destination_id=destination_id
                )
            )
        )
        )
        return create_mission(stub, robot_id, mission)
    elif mode == "streaming":
        selector = RobotSelector(
            robot_ids=RobotSelector.RobotIDs(
                ids=[robot_id]
            )
        )
        return subscribe_mission_status(stub, selector)


if __name__ == "__main__":
    sys.exit(main())
