using Grpc.Core;

namespace BearRoboticsCloudAPI.Unary
{
    public delegate TResponse UnaryRpcMethod<TRequest, TResponse>(TRequest request);
}