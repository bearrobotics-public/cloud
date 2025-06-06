import grpc
import time
from auth import get_credentials
from typing import Callable, Any
from grpc import RpcError, StatusCode

def stream_with_reconnect(
    stub_fn: Callable[[Any, grpc.CallCredentials], Any],
    request: Any,
    credentials: grpc.CallCredentials,
    on_next: Callable[[Any], None],
    max_retries=5,
    backoff=5.0,
    allowed_statuses=(StatusCode.UNAVAILABLE, StatusCode.INTERNAL, StatusCode.DEADLINE_EXCEEDED, StatusCode.UNAUTHENTICATED),
):
    """
    Wraps a gRPC streaming call with auto-reconnect on disconnection errors.

    :param stub_fn: Callable to execute (e.g. stub.GetMissionStatus)
    :param request: gRPC request
    :param credentials: gRPC credentials
    :param on_next: A function to handle each message received
    :param max_retries: Max reconnection attempts
    :param backoff: Backoff in seconds
    :param allowed_statuses: gRPC status codes to reconnect on
    """
    retries = 0

    while retries <= max_retries:
        try:
            print("[stream] Connecting...")
            stream = stub_fn(request, credentials=credentials)
            for item in stream:
                on_next(item)
            print("[stream] Completed.")
            return  # Normal end of stream
        except RpcError as e:
            code = e.code()
            if code in allowed_statuses:
                if code == StatusCode.UNAUTHENTICATED:
                    print("[stream] Unauthenticated, refreshing auth token...")
                    credentials = get_credentials()
                print(f"[stream] Disconnected ({code.name}), retrying in {backoff:.1f}s...")
                time.sleep(backoff)
                retries += 1
            else:
                print(f"[stream] Fatal gRPC error ({code.name}): {e.details()}")
                break
        except Exception as e:
            print(f"[stream] Unexpected error: {e}")
            break

    raise RuntimeError("Exceeded maximum retries for stream.")
