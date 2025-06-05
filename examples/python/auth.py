import grpc
import json
import os
import requests
from pathlib import Path

AUTH_URL = "https://api-auth.bearrobotics.ai/authorizeApiAccess"
CREDENTIALS_PATH = "resources/credentials.json"

def get_credentials():
    """
    Get a JWT token by loading credentials from credentials.json
    and making a POST request to the auth endpoint.
    
    Returns:
        str: The JWT token
        
    Raises:
        FileNotFoundError: If credentials.json is not found
        KeyError: If required credentials are missing
        requests.RequestException: If the auth request fails
    """        
    with open(CREDENTIALS_PATH) as f:
        credentials = json.load(f)
    
    # Make auth request
    response = requests.post(AUTH_URL, json=credentials)
    response.raise_for_status()  # Raise exception for bad status codes
    
    # Extract and return the JWT token
    return grpc.access_token_call_credentials(response.text)
