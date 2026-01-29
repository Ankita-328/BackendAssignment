# API Reference - Vert.x & Ebean Backend

A comprehensive REST API for user management, KYC processing with AI-powered document analysis, and bulk operations.

## Base URL
```
http://localhost:8000/api
```

## Authentication
All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

---

## Endpoint Map

| Method | Path | Description | Auth Required | Role |
|--------|------|-------------|---------------|------|
| POST | `/auth/login` | User authentication | ❌ | - |
| POST | `/admin/onboard/student` | Create student account | ✅ | ADMIN |
| POST | `/admin/onboard/teacher` | Create teacher account | ✅ | ADMIN |
| GET | `/admin/profile` | Get admin profile | ✅ | ADMIN |
| PUT | `/admin/profile` | Update admin profile | ✅ | ADMIN |
| PUT | `/student/profile` | Update student profile | ✅ | STUDENT |
| PUT | `/teacher/profile` | Update teacher profile | ✅ | TEACHER |
| GET | `/admin/users` | List users by role | ✅ | ADMIN |
| PUT | `/admin/users/{userId}/status` | Toggle user status | ✅ | ADMIN |
| POST | `/kyc/submit` | Submit KYC document | ✅ | ALL |
| GET | `/kyc/status` | Get user's KYC status | ✅ | ALL |
| GET | `/admin/kyc` | List all KYC submissions | ✅ | ADMIN |
| GET | `/admin/kyc/{id}` | Get KYC by ID | ✅ | ADMIN |
| PUT | `/admin/kyc/{id}/review` | Review KYC submission | ✅ | ADMIN |
| GET | `/admin/bulk-upload/template` | Download CSV template | ❌ | - |
| POST | `/admin/bulk-upload` | Upload CSV for bulk user creation | ✅ | ADMIN |
| GET | `/admin/bulk-upload/{id}` | Get bulk upload status | ✅ | ADMIN |
| GET | `/admin/bulk-upload/{id}/errors` | Get bulk upload errors | ✅ | ADMIN |

---

##  Authentication

### Login
**POST** `/auth/login`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `400`: Missing email or password
- `401`: Invalid credentials

---

## 👥 User Management

### Create Student
**POST** `/admin/onboard/student`

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "initialPassword": "tempPassword123"
}
```

**Response (201):**
```json
{
  "message": "STUDENT created successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Create Teacher
**POST** `/admin/onboard/teacher`

**Request Body:**
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "mobileNumber": "+1234567891",
  "initialPassword": "tempPassword123"
}
```

**Response (201):**
```json
{
  "message": "TEACHER created successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440001"
}
```

### Get Profile
**GET** `/admin/profile`

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Admin User",
  "email": "admin@example.com",
  "mobileNumber": "+1234567890",
  "role": "ADMIN",
  "active": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Update Profile
**PUT** `/admin/profile` | `/student/profile` | `/teacher/profile`

**Request Body (Student):**
```json
{
  "fullName": "Updated Name",
  "mobileNumber": "+1234567892",
  "courseName": "Computer Science"
}
```

**Request Body (Teacher):**
```json
{
  "fullName": "Updated Name",
  "mobileNumber": "+1234567892",
  "qualification": "PhD in Computer Science",
  "experienceYears": 5,
  "emailId": "updated@example.com"
}
```

### List Users
**GET** `/admin/users?role={STUDENT|TEACHER|ADMIN}`

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "role": "STUDENT",
    "active": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

### Toggle User Status
**PUT** `/admin/users/{userId}/status`

**Request Body:**
```json
{
  "active": false
}
```

**Response (200):**
```json
{
  "message": "User status updated successfully"
}
```

---

## 📄 KYC Management

### Submit KYC Document
**POST** `/kyc/submit`

**Content-Type:** `multipart/form-data`

**Form Data:**
- `file`: Document image (JPEG/PNG)
- `documentType`: Type of document (e.g., "passport", "driver_license")
- `documentNumber`: Document identification number

**Response (201):**
```json
{
  "message": "KYC Submitted Successfully",
  "status": "PENDING"
}
```

### Get KYC Status
**GET** `/kyc/status`

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "documentType": "passport",
  "documentNumber": "AB123456",
  "status": "PENDING",
  "aiStatus": "AI_CLEAR",
  "aiAnalysis": "{\"confidenceScore\": 95, \"riskFlags\": [], \"recommendation\": \"APPROVE\"}",
  "submittedAt": "2024-01-15T10:30:00Z"
}
```

### List All KYC Submissions (Admin)
**GET** `/admin/kyc`

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "role": "STUDENT"
    },
    "documentType": "passport",
    "status": "PENDING",
    "aiStatus": "AI_CLEAR",
    "submittedAt": "2024-01-15T10:30:00Z"
  }
]
```

### Get KYC by ID
**GET** `/admin/kyc/{id}`

**Response (200):**
```json
{
  "kycId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "role": "STUDENT"
}
```

### Review KYC Submission
**PUT** `/admin/kyc/{id}/review`

**Request Body:**
```json
{
  "status": "APPROVED"
}
```

**Response (200):**
```json
{
  "message": "KYC status updated to APPROVED",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 📊 Bulk Operations

### Download CSV Template
**GET** `/admin/bulk-upload/template`

**Response:** CSV file download with headers:
```csv
fullName,email,mobileNumber,role,initialPassword
```

### Upload CSV for Bulk User Creation
**POST** `/admin/bulk-upload`

**Content-Type:** `multipart/form-data`

**Form Data:**
- `file`: CSV file with user data

**Response (200):**
```json
{
  "message": "Upload started",
  "uploadId": "bulk_550e8400-e29b-41d4-a716-446655440000"
}
```

### Get Bulk Upload Status
**GET** `/admin/bulk-upload/{id}`

**Response (200):**
```json
{
  "id": "bulk_550e8400-e29b-41d4-a716-446655440000",
  "adminId": "550e8400-e29b-41d4-a716-446655440001",
  "totalRecords": 100,
  "successCount": 95,
  "failureCount": 5,
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:35:00Z"
}
```

### Get Bulk Upload Errors
**GET** `/admin/bulk-upload/{id}/errors`

**Response (200):**
```json
[
  {
    "rowNumber": 15,
    "email": "invalid@email",
    "error": "Invalid email format"
  },
  {
    "rowNumber": 23,
    "email": "duplicate@example.com",
    "error": "Email already exists"
  }
]
```

---

## 🤖 AI Service Workflow

### Visual KYC Analysis via OpenRouter

The AI service performs automated document verification using Grok-4-Fast model through OpenRouter API:

#### Process Flow:
1. **Document Upload**: User submits KYC document via `/kyc/submit`
2. **File Processing**: System saves uploaded file and triggers AI analysis
3. **Image Encoding**: Document image is converted to Base64 format
4. **AI Analysis**:
   - Sends multimodal request (text prompt + image) to OpenRouter
   - Uses Grok-4-Fast model for vision analysis
   - Analyzes document authenticity, readability, and type matching
5. **Result Processing**: AI response is parsed and stored in database
6. **Status Update**: KYC record updated with AI analysis results

#### AI Prompt Structure:
```
You are a professional KYC API. Analyze the attached image of a {documentType}
for a user with the role '{userRole}'.
Verify if the document is authentic, readable, and matches the declared type.
Output ONLY JSON with: 'confidenceScore' (0-100), 'riskFlags' (list),
and 'recommendation' (APPROVE/MANUAL_REVIEW).
```

#### AI Response Format:
```json
{
  "confidenceScore": 95,
  "riskFlags": ["low_resolution", "partial_obstruction"],
  "recommendation": "APPROVE"
}
```

#### AI Status Categories:
- `AI_CLEAR`: Document passed AI verification
- `AI_FLAGGED`: Document requires manual review
- `AI_FAILED`: AI analysis failed due to technical issues
- `AI_ERROR`: Network or processing error occurred

---

## 📊 Data Models

### User Model
```json
{
  "id": "UUID",
  "role": "ADMIN|TEACHER|STUDENT",
  "email": "string (unique)",
  "fullName": "string",
  "mobileNumber": "string",
  "active": "boolean",
  "createdAt": "ISO 8601 timestamp",
  "updatedAt": "ISO 8601 timestamp"
}
```

### KycSubmission Model
```json
{
  "id": "UUID",
  "user": "User object",
  "documentType": "string",
  "documentNumber": "string",
  "documentPath": "string",
  "status": "PENDING|SUBMITTED|APPROVED|REJECTED",
  "aiStatus": "PENDING|AI_CLEAR|AI_FLAGGED|AI_FAILED",
  "aiAnalysis": "JSON string",
  "submittedAt": "ISO 8601 timestamp"
}
```

### StudentDetails Model
```json
{
  "id": "UUID",
  "user": "User object",
  "courseEnrolled": "string"
}
```

### TeacherDetails Model
```json
{
  "id": "UUID",
  "user": "User object",
  "qualification": "string",
  "experienceYears": "integer"
}
```

### BulkUpload Model
```json
{
  "id": "string",
  "adminId": "string",
  "totalRecords": "integer",
  "successCount": "integer",
  "failureCount": "integer",
  "status": "string",
  "createdAt": "ISO 8601 timestamp",
  "completedAt": "ISO 8601 timestamp"
}
```

---

## 🚨 Status Codes & Error Handling

### Success Codes
- **200 OK**: Request successful
- **201 Created**: Resource created successfully

### Client Error Codes
- **400 Bad Request**: Invalid request format or missing required fields
  ```json
  {
    "error": "Email and Password are required"
  }
  ```

- **401 Unauthorized**: Invalid credentials or expired token
  ```json
  {
    "error": "Invalid credentials"
  }
  ```

- **403 Forbidden**: Insufficient permissions for requested resource

- **404 Not Found**: Resource not found
  ```json
  {
    "error": "KYC ID not found"
  }
  ```

### Server Error Codes
- **500 Internal Server Error**: Unexpected server error
  ```json
  {
    "error": "Internal Server Error"
  }
  ```

### Common Error Scenarios

#### Authentication Errors
- Missing Authorization header
- Invalid JWT token format
- Expired JWT token
- User account deactivated

#### Validation Errors
- Missing required fields in request body
- Invalid email format
- Invalid role specification
- Invalid KYC status values

#### Business Logic Errors
- Duplicate email registration
- KYC already submitted for user
- Invalid file upload (no file provided)
- CSV parsing errors in bulk upload

#### AI Service Errors
- OpenRouter API rate limiting
- Invalid API key
- Network connectivity issues
- Image processing failures

---

## 🔧 Configuration

### Environment Variables
```properties
# Database Configuration
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=

# JWT Configuration
JWT_SECRET=your-secret-key
JWT_EXPIRY=3600

# AI Service Configuration
OPENROUTER_API_KEY=sk-or-v1-...
AI_MODEL=x-ai/grok-4-fast

# Server Configuration
SERVER_PORT=8000
UPLOAD_DIRECTORY=uploads
```

### Dependencies
- **Vert.x**: Reactive web framework
- **Ebean ORM**: Database persistence
- **JWT**: Authentication tokens
- **Jackson**: JSON processing
- **RxJava**: Reactive programming
- **H2 Database**: In-memory database (development)

---

## 📝 Usage Examples

### Complete User Onboarding Flow
```bash
# 1. Admin login
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'

# 2. Create student
curl -X POST http://localhost:8000/api/admin/onboard/student \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","email":"john@example.com","mobileNumber":"+1234567890","initialPassword":"temp123"}'

# 3. Student login
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"temp123"}'

# 4. Submit KYC
curl -X POST http://localhost:8000/api/kyc/submit \
  -H "Authorization: Bearer <student_token>" \
  -F "file=@passport.jpg" \
  -F "documentType=passport" \
  -F "documentNumber=AB123456"
```

### Bulk User Upload Flow
```bash
# 1. Download template
curl -O http://localhost:8000/api/admin/bulk-upload/template

# 2. Upload filled CSV
curl -X POST http://localhost:8000/api/admin/bulk-upload \
  -H "Authorization: Bearer <admin_token>" \
  -F "file=@users.csv"

# 3. Check status
curl -X GET http://localhost:8000/api/admin/bulk-upload/{uploadId} \
  -H "Authorization: Bearer <admin_token>"
```

---

*Generated for Vert.x & Ebean Backend API v1.0*
