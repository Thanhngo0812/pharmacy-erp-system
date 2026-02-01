# Lock Account API

## 1. Lock Account
**Endpoint**: `POST /api/users/{id}/lock`

Locks a user account, preventing them from logging in.

### Headers
| Header | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `Authorization` | String | Yes | Bearer Token (JWT) |

### Path Variables
| Variable | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `id` | Integer | Yes | ID of the user to lock |

### Request Body
None

### Success Response (200 OK)
**Example JSON**:
```json
{
  "success": true,
  "message": "Account locked successfully",
  "data": null
}
```

### Error Responses

| Status | Condition | Message |
| :--- | :--- | :--- |
| `401 Unauthorized` | Missing/Invalid Token | "User not authenticated" |
| `403 Forbidden` | Insufficient Permissions | "Access denied: You do not have permission to lock accounts" / "Access denied: HR cannot lock Manager or Warehouse Manager accounts" |
| `404 Not Found` | User Not Found | "User not found" |
| `500 Internal Server Error` | Unexpected Error | "An unexpected error occurred: <details>" |
