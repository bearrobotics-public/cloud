using System.Threading.Tasks;
using Grpc.Core;

namespace BearRoboticsCloudAPI.Streaming
{
    public delegate AsyncServerStreamingCall<TResponse> StreamingRpcMethod<TRequest, TResponse>(
        TRequest request, CallOptions options);
}