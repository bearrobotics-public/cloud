using System;
using System.Threading.Tasks;
using Grpc.Core;

namespace BearRoboticsCloudAPI.Auth
{
    public class JwtCallCredentials
    {
        private readonly BearAuthService _authService;
        private readonly CallCredentials _credentials;

        public JwtCallCredentials(BearAuthService authService)
        {
            _authService = authService;
            _credentials = CallCredentials.FromInterceptor(async (context, metadata) =>
            {
                var token = await _authService.GetJwtTokenAsync();
                metadata.Add("authorization", $"Bearer {token}");
            });
        }

        public CallCredentials GetCredentials()
        {
            return _credentials;
        }
    }
}