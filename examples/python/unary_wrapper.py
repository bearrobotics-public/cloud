import grpc
import time
from auth import get_credentials
from typing import Callable, Any
from grpc import RpcError, StatusCode

def retry_on_disconnect(
    stub_fn: Callable[[Any, grpc.CallCredentials], Any],
    request: Any,
    credentials: grpc.CallCredentials,
    max_retries=5,
    backoff=5.0,
    allowed_statuses=(StatusCode.UNAVAILABLE, StatusCode.INTERNAL, StatusCode.DEADLINE_EXCEEDED, StatusCode.UNAUTHENTICATED),
):
    """
    Retry the given function `fn` on gRPC disconnection errors.
    
    :param stub_fn: Callable to execute (e.g. stub.CreateMission)
    :param request: gRPC request
    :param credentials: gRPC credentials
    :param max_retries: Max retry attempts
    :param backoff: Backoff in seconds
    :param allowed_statuses: gRPC StatusCodes to retry on
    """
    for attempt in range(1, max_retries + 1):
        try:
            return stub_fn(request, credentials=credentials)
        except RpcError as e:
            code = e.code()
            if code in allowed_statuses:
                if code == StatusCode.UNAUTHENTICATED:
                    print("[Retry] Unauthenticated, refreshing auth token...")
                    credentials = get_credentials()
                print(f"[Retry {attempt}] gRPC error {code.name}: {e.details()} — retrying in {backoff:.1f}s...")
                time.sleep(backoff)
            else:
                print(f"[Abort] gRPC error {code.name}: {e.details()}")
                raise
    raise RuntimeError("Exceeded max retries")
