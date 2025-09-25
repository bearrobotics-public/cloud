using System;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace BearRoboticsCloudAPI.Auth
{
    public class BearAuthService
    {
        private const string JWT_AUTH_URL = "https://api-auth.bearrobotics.ai/authorizeApiAccess";
        private const int TOKEN_REFRESH_INTERVAL_MINUTES = 30;

        private string _apiKey = string.Empty;
        private string _secret = string.Empty;
        private string _scope = string.Empty;
        private string? _jwtToken;
        private DateTime _tokenExpiryTime;
        private readonly Timer _refreshTimer;
        private readonly object _tokenLock = new object();
        private readonly HttpClient _httpClient;

        public BearAuthService(string credentialsFilePath)
        {
            LoadCredentials(credentialsFilePath);
            _httpClient = new HttpClient();
            _httpClient.Timeout = TimeSpan.FromSeconds(10);

            // Initialize token
            FetchNewToken().Wait();

            // Setup auto-refresh timer
            _refreshTimer = new Timer(
                _ => RefreshTokenAsync().Wait(),
                null,
                TimeSpan.FromMinutes(TOKEN_REFRESH_INTERVAL_MINUTES),
                TimeSpan.FromMinutes(TOKEN_REFRESH_INTERVAL_MINUTES)
            );
        }

        private async Task RefreshTokenAsync()
        {
            try
            {
                await FetchNewToken();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Failed to refresh token: {ex.Message}");
            }
        }

        private void LoadCredentials(string filePath)
        {
            try
            {
                if (!File.Exists(filePath))
                {
                    throw new FileNotFoundException($"Credentials file not found: {filePath}");
                }

                string jsonString = File.ReadAllText(filePath);
                dynamic credentials = JsonConvert.DeserializeObject(jsonString)!;

                _apiKey = credentials.api_key;
                _secret = credentials.secret;
                _scope = credentials.scope;

                Console.WriteLine("Credentials loaded successfully");
            }
            catch (JsonException ex)
            {
                throw new InvalidOperationException("Failed to parse credentials file", ex);
            }
        }

        public async Task<string> GetJwtTokenAsync()
        {
            return await Task.Run(() =>
            {
                lock (_tokenLock)
                {
                    if (_jwtToken == null || ShouldRefreshToken())
                    {
                        FetchNewToken().Wait();
                    }
                    return _jwtToken!;
                }
            });
        }

        public string GetJwtToken()
        {
            return GetJwtTokenAsync().Result;
        }

        private bool ShouldRefreshToken()
        {
            return DateTime.UtcNow >= _tokenExpiryTime;
        }

        private async Task FetchNewToken()
        {
            Console.WriteLine("Fetching new JWT token...");

            var requestBody = new JObject
            {
                ["api_key"] = _apiKey,
                ["secret"] = _secret,
                ["scope"] = _scope
            };

            try
            {
                var content = new StringContent(
                    requestBody.ToString(),
                    Encoding.UTF8,
                    "application/json"
                );

                Console.WriteLine($"Request URL: {JWT_AUTH_URL}");
                var response = await _httpClient.PostAsync(JWT_AUTH_URL, content);

                if (response.IsSuccessStatusCode)
                {
                    string responseBody = await response.Content.ReadAsStringAsync();

                    // Check if response is already a JWT token string (not JSON)
                    if (responseBody.StartsWith("eyJ"))  // JWT tokens start with eyJ
                    {
                        lock (_tokenLock)
                        {
                            _jwtToken = responseBody.Trim();
                            _tokenExpiryTime = DateTime.UtcNow.AddMinutes(TOKEN_REFRESH_INTERVAL_MINUTES - 5);
                        }

                        Console.WriteLine("JWT token obtained successfully (direct token response)");
                    }
                    else
                    {
                        // Try to parse as JSON
                        try
                        {
                            dynamic jsonResponse = JsonConvert.DeserializeObject(responseBody)!;

                            lock (_tokenLock)
                            {
                                // Try different possible field names
                                _jwtToken = jsonResponse.jwt?.ToString() ??
                                          jsonResponse.token?.ToString() ??
                                          jsonResponse.access_token?.ToString() ??
                                          throw new InvalidOperationException("No token field found in response");

                                _tokenExpiryTime = DateTime.UtcNow.AddMinutes(TOKEN_REFRESH_INTERVAL_MINUTES - 5);
                            }

                            Console.WriteLine("JWT token obtained successfully (JSON response)");
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"Failed to parse JWT response. Response was: {responseBody.Substring(0, Math.Min(responseBody.Length, 100))}...");
                            throw new InvalidOperationException($"Invalid JWT response format: {ex.Message}", ex);
                        }
                    }
                }
                else
                {
                    string errorBody = await response.Content.ReadAsStringAsync();
                    Console.WriteLine($"Authentication failed with status {response.StatusCode}");
                    Console.WriteLine($"Error response: {errorBody}");

                    // Try to parse error response as JSON
                    try
                    {
                        dynamic errorJson = JsonConvert.DeserializeObject(errorBody)!;
                        string errorMessage = errorJson.error?.ToString() ?? errorJson.message?.ToString() ?? "Unknown error";
                        throw new InvalidOperationException($"Authentication failed: {errorMessage}");
                    }
                    catch (JsonException)
                    {
                        // If not JSON, use the raw error body
                        throw new InvalidOperationException($"Authentication failed: {errorBody}");
                    }
                }
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine($"Network error fetching JWT token: {ex.Message}");
                throw;
            }
        }

        public void Shutdown()
        {
            _refreshTimer?.Dispose();
            _httpClient?.Dispose();
        }
    }
}