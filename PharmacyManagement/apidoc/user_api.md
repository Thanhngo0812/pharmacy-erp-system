# User API

## 1. Get My Profile
**Endpoint**: `GET /api/users/profile`

Retrieve the profile information of the currently authenticated user.

### Headers
| Header | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `Authorization` | String | Yes | Bearer Token (JWT) |

### Request Body
None

### Success Response (200 OK)
**Example JSON**:
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "fullName": "Nguyen Van A",
    "email": "nguyenvana@example.com",
    "phone": "0912345678",
    "imageUrl": "https://example.com/avatar.jpg",
    "positionName": "Pharmacist",
    "currentSalary": 15000000.0,
    "hireDate": "2023-01-15"
  }
}
```

### Error Responses

| Status | Condition | Message |
| :--- | :--- | :--- |
| `401 Unauthorized` | Invalid/Missing Token | "Invalid JWT token" / "Expired JWT token" |
| `401 Unauthorized` | User Not Found | "User not found: <username>" |
| `500 Internal Server Error` | Unlinked Employee | "User is not linked to an employee profile" |
| `500 Internal Server Error` | Unexpected Error | "An unexpected error occurred: <details>" |

---
