using System;
using System.Net.Http;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using BearRoboticsCloudAPI.Auth;
using BearRoboticsCloudAPI.Streaming;
using BearRoboticsCloudAPI.Unary;
using Bearrobotics.Api.V1.Services.Cloud;

namespace BearRoboticsCloudAPI
{
    public class BearRoboticsClient : IDisposable
    {
        private readonly GrpcChannel _channel;
        private readonly BearAuthService _authService;
        private readonly CallCredentials _credentials;
        private readonly APIService.APIServiceClient _client;
        private bool _running = true;

        public BearRoboticsClient(string host, int port)
        {
            // Allow insecure connections for development
            var httpHandler = new HttpClientHandler();
            httpHandler.ServerCertificateCustomValidationCallback =
                HttpClientHandler.DangerousAcceptAnyServerCertificateValidator;

            var channelOptions = new GrpcChannelOptions
            {
                HttpHandler = httpHandler
            };

            _channel = GrpcChannel.ForAddress($"https://{host}:{port}", channelOptions);
            _client = new APIService.APIServiceClient(_channel);

            string credentialsPath = "Resources/credentials.json";
            _authService = new BearAuthService(credentialsPath);
            var jwtCreds = new JwtCallCredentials(_authService);
            _credentials = jwtCreds.GetCredentials();
        }

        public APIService.APIServiceClient GetClient()
        {
            return _client;
        }

        public CallOptions GetCallOptions()
        {
            var metadata = new Metadata();
            var token = _authService.GetJwtToken();
            metadata.Add("authorization", $"Bearer {token}");

            return new CallOptions(metadata);
        }

        public UnaryClient<TRequest, TResponse>.Builder CreateUnaryClient<TRequest, TResponse>()
        {
            return new UnaryClient<TRequest, TResponse>.Builder();
        }

        public StreamingClient<TRequest, TResponse>.Builder CreateStreamingClient<TRequest, TResponse>()
        {
            return new StreamingClient<TRequest, TResponse>.Builder()
                .Credentials(_credentials)
                .CallOptions(GetCallOptions());
        }

        public async Task<StreamingClient<TRequest, TResponse>> StartStreamAsync<TRequest, TResponse>(
            StreamingRpcMethod<TRequest, TResponse> rpcMethod,
            TRequest request,
            StreamingClient<TRequest, TResponse>.IStreamObserver observer,
            string streamName)
        {
            var client = CreateStreamingClient<TRequest, TResponse>()
                .RpcMethod(rpcMethod)
                .Request(request)
                .Observer(observer)
                .StreamName(streamName)
                .Build();

            var streamTask = Task.Run(async () =>
            {
                try
                {
                    await client.StartStreamingAsync();
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Streaming thread interrupted for {streamName}: {ex.Message}");
                }
            });

            await Task.Yield(); // Ensure the method is truly async
            return client;
        }

        public bool IsRunning()
        {
            return _running;
        }

        public async Task ShutdownAsync()
        {
            Console.WriteLine("Shutting down client...");
            _running = false;
            _authService.Shutdown();
            await _channel.ShutdownAsync();
            Console.WriteLine("Client shut down complete");
        }

        public void Dispose()
        {
            ShutdownAsync().Wait();
        }
    }
}