using System;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;
using BearRoboticsCloudAPI.Auth;

namespace BearRoboticsCloudAPI.Streaming
{
    public class StreamingClient<TRequest, TResponse>
    {
        public interface IStreamObserver
        {
            void OnNext(TResponse response);
            void OnError(Exception error);
            void OnCompleted();
        }

        private readonly StreamingRpcMethod<TRequest, TResponse> _rpcMethod;
        private readonly TRequest _request;
        private readonly IStreamObserver _observer;
        private readonly CallOptions _callOptions;
        private readonly CallCredentials _credentials;
        private readonly string _streamName;
        private readonly int _reconnectDelaySeconds;

        private bool _isStreaming;
        private CancellationTokenSource? _cancellationTokenSource;

        private StreamingClient(
            StreamingRpcMethod<TRequest, TResponse> rpcMethod,
            TRequest request,
            IStreamObserver observer,
            CallOptions callOptions,
            CallCredentials credentials,
            string streamName,
            int reconnectDelaySeconds)
        {
            _rpcMethod = rpcMethod;
            _request = request;
            _observer = observer;
            _callOptions = callOptions;
            _credentials = credentials;
            _streamName = streamName;
            _reconnectDelaySeconds = reconnectDelaySeconds;
        }

        public async Task StartStreamingAsync()
        {
            _isStreaming = true;
            _cancellationTokenSource = new CancellationTokenSource();

            while (_isStreaming && !_cancellationTokenSource.Token.IsCancellationRequested)
            {
                try
                {
                    Console.WriteLine($"Starting streaming for {_streamName}...");

                    var call = _rpcMethod(_request, _callOptions);

                    await foreach (var response in call.ResponseStream.ReadAllAsync(_cancellationTokenSource.Token))
                    {
                        _observer.OnNext(response);
                    }

                    _observer.OnCompleted();
                    break;
                }
                catch (RpcException ex) when (ShouldReconnect(ex.StatusCode))
                {
                    Console.WriteLine($"Stream {_streamName} disconnected with status {ex.StatusCode}. Reconnecting in {_reconnectDelaySeconds} seconds...");
                    _observer.OnError(ex);

                    await Task.Delay(TimeSpan.FromSeconds(_reconnectDelaySeconds), _cancellationTokenSource.Token);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Stream {_streamName} encountered error: {ex.Message}");
                    _observer.OnError(ex);
                    break;
                }
            }
        }

        private bool ShouldReconnect(StatusCode statusCode)
        {
            return statusCode == StatusCode.Unavailable ||
                   statusCode == StatusCode.Internal ||
                   statusCode == StatusCode.DeadlineExceeded ||
                   statusCode == StatusCode.Unauthenticated;
        }

        public void StopStreaming()
        {
            _isStreaming = false;
            _cancellationTokenSource?.Cancel();
        }

        public class Builder
        {
            private StreamingRpcMethod<TRequest, TResponse>? _rpcMethod;
            private TRequest? _request;
            private IStreamObserver? _observer;
            private CallOptions _callOptions;
            private CallCredentials? _credentials;
            private string _streamName = "Unnamed Stream";
            private int _reconnectDelaySeconds = 5;

            public Builder RpcMethod(StreamingRpcMethod<TRequest, TResponse> rpcMethod)
            {
                _rpcMethod = rpcMethod;
                return this;
            }

            public Builder Request(TRequest request)
            {
                _request = request;
                return this;
            }

            public Builder Observer(IStreamObserver observer)
            {
                _observer = observer;
                return this;
            }

            public Builder CallOptions(CallOptions callOptions)
            {
                _callOptions = callOptions;
                return this;
            }

            public Builder Credentials(CallCredentials credentials)
            {
                _credentials = credentials;
                return this;
            }

            public Builder StreamName(string name)
            {
                _streamName = name;
                return this;
            }

            public Builder ReconnectDelay(int seconds)
            {
                _reconnectDelaySeconds = seconds;
                return this;
            }

            public StreamingClient<TRequest, TResponse> Build()
            {
                if (_rpcMethod == null) throw new InvalidOperationException("RPC method is required");
                if (_request == null) throw new InvalidOperationException("Request is required");
                if (_observer == null) throw new InvalidOperationException("Observer is required");
                if (_credentials == null) throw new InvalidOperationException("Credentials are required");

                return new StreamingClient<TRequest, TResponse>(
                    _rpcMethod,
                    _request,
                    _observer,
                    _callOptions,
                    _credentials,
                    _streamName,
                    _reconnectDelaySeconds);
            }
        }
    }
}