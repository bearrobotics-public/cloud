using System;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;

namespace BearRoboticsCloudAPI.Unary
{
    public class UnaryClient<TRequest, TResponse>
    {
        private readonly UnaryRpcMethod<TRequest, TResponse> _rpcMethod;
        private readonly TRequest _request;
        private readonly string _rpcName;
        private readonly int _maxRetries;
        private readonly int _retryDelayMs;

        private UnaryClient(
            UnaryRpcMethod<TRequest, TResponse> rpcMethod,
            TRequest request,
            string rpcName,
            int maxRetries,
            int retryDelayMs)
        {
            _rpcMethod = rpcMethod;
            _request = request;
            _rpcName = rpcName;
            _maxRetries = maxRetries;
            _retryDelayMs = retryDelayMs;
        }

        public async Task<TResponse> CallAsync()
        {
            int attempt = 0;
            Exception? lastException = null;

            while (attempt < _maxRetries)
            {
                attempt++;

                try
                {
                    Console.WriteLine($"Calling {_rpcName} (attempt {attempt}/{_maxRetries})...");
                    var response = await Task.Run(() => _rpcMethod(_request));
                    Console.WriteLine($"{_rpcName} succeeded");
                    return response;
                }
                catch (RpcException ex) when (IsRetryableError(ex.StatusCode) && attempt < _maxRetries)
                {
                    lastException = ex;
                    Console.WriteLine($"{_rpcName} failed with {ex.StatusCode}. Retrying in {_retryDelayMs}ms...");
                    await Task.Delay(_retryDelayMs);
                }
                catch (Exception ex)
                {
                    lastException = ex;
                    Console.WriteLine($"{_rpcName} failed: {ex.Message}");
                    throw;
                }
            }

            throw new InvalidOperationException($"{_rpcName} failed after {_maxRetries} attempts", lastException);
        }

        public TResponse Call()
        {
            return CallAsync().Result;
        }

        private bool IsRetryableError(StatusCode statusCode)
        {
            return statusCode == StatusCode.Unavailable ||
                   statusCode == StatusCode.DeadlineExceeded ||
                   statusCode == StatusCode.ResourceExhausted ||
                   statusCode == StatusCode.Aborted ||
                   statusCode == StatusCode.Internal;
        }

        public class Builder
        {
            private UnaryRpcMethod<TRequest, TResponse>? _rpcMethod;
            private TRequest? _request;
            private string _rpcName = "UnnamedRPC";
            private int _maxRetries = 3;
            private int _retryDelayMs = 1000;

            public Builder RpcMethod(UnaryRpcMethod<TRequest, TResponse> rpcMethod)
            {
                _rpcMethod = rpcMethod;
                return this;
            }

            public Builder Request(TRequest request)
            {
                _request = request;
                return this;
            }

            public Builder RpcName(string name)
            {
                _rpcName = name;
                return this;
            }

            public Builder MaxRetries(int retries)
            {
                _maxRetries = retries;
                return this;
            }

            public Builder RetryDelay(int delayMs)
            {
                _retryDelayMs = delayMs;
                return this;
            }

            public UnaryClient<TRequest, TResponse> Build()
            {
                if (_rpcMethod == null) throw new InvalidOperationException("RPC method is required");
                if (_request == null) throw new InvalidOperationException("Request is required");

                return new UnaryClient<TRequest, TResponse>(
                    _rpcMethod,
                    _request,
                    _rpcName,
                    _maxRetries,
                    _retryDelayMs);
            }
        }
    }
}