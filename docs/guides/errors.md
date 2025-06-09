
## Common Errors 
These are common error types you might encounter when using the Bear API. We follow gRPC status codes, and where applicable, include structured error details using google.rpc.Status and related types (e.g., BadRequest, ErrorInfo).
<br />
<br />

#### Robot software incompatible
The robot’s software version does not meet the compatibility requirements of the API. <br />
**Code**: `FAILED_PRECONDITION`<br />
**Resolution**: Check the robot software version compatibility checklist. Starting with API v1, calls return appropriate errors when a version mismatch is detected. <br />
<br />

####  Authenticated error
The request is missing valid authentication credentials (e.g., invalid token, expired session). <br />
**Code**: `UNAUTHENTICATED` <br />
**Resolution**: Confirm that the API key or JWT token is correct and has not expired. Refer to the [authentication guide](authentication.md) for how to obtain and refresh a JWT token using your API key. <br />
<br />

#### Permission Denied
The request was authenticated, but the client does not have permission to access the requested resource. <br />
**Code**: `PERMISSION_DENIED` <br />
**Resolution**: Verify that the API key's user has the necessary permissions for the target robot or location.
Tip: Double-check the spelling of all `robot_id` and `location_id` values.
<br />

#### Request fails validation
The request payload is well-formed, but contains invalid or missing values. <br />
**Code**: `INVALID_ARGUMENT` <br />
**Resolution**: Review the request against the API schema. The error message will typically include FieldViolation entries to help you pinpoint the issue. <br />

