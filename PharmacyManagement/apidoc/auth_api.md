# Authentication API

## 1. Login
**Endpoint**: `POST /api/auth/login`

Start the user session and retrieve the JWT access token.

### Request Body
| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `username` | String | Yes | User's login username |
| `password` | String | Yes | User's password (min 6 chars) |

**Example JSON**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

### Success Response (200 OK)
**Example JSON**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "employeeId": 1,
    "fullName": "Nguyen Van A",
    "roles": [
      "Admin",
      "Manager"
    ],
    "expiresIn": 86400000
  }
}
```

### Error Responses

| Status | Condition | Message |
| :--- | :--- | :--- |
| `401 Unauthorized` | Invalid Username/Password | "Invalid username or password" |
| `403 Forbidden` | Account Disabled | "Account is disabled" |
| `423 Locked` | Account Locked | "Account is locked" |

---
