# Manual Authentication & Authorization System

A comprehensive three-phase security implementation in Spring Boot with React frontend, featuring manual authentication, two-factor authentication, role-based access control, and Just-in-Time temporary access.

## Features

### Phase 1: Manual Authentication
- ✅ Custom user registration with manual validation
- ✅ BCrypt password hashing (no Spring Security auto-config)
- ✅ Manual login with credential verification
- ✅ Secure password storage

### Phase 2: Two-Factor Authentication
- ✅ Email verification on registration
- ✅ 2FA code verification on login
- ✅ Custom session management with HTTP-only cookies
- ✅ Console-based email simulation (no SMTP required)

### Phase 3: Role-Based Access Control
- ✅ Organizational role hierarchy (ADMIN > MANAGER > USER)
- ✅ Resource-specific roles (DOCUMENT_VIEWER, DOCUMENT_EDITOR)
- ✅ Permission-based access control
- ✅ Just-in-Time (JIT) temporary access with automatic expiration

## Tech Stack

**Backend:**
- Spring Boot 4.0.0
- H2 Database
- BCrypt for password hashing
- Custom session management

**Frontend:**
- React 18
- Vite
- React Router
- Axios
- Modern dark theme UI

## Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- Maven (or use included Maven wrapper)

### Backend Setup

1. Navigate to project directory:
```bash
cd security-implementations
```

2. Run the Spring Boot application:
```bash
.\mvnw.cmd spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies (if not already done):
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## Testing the Application

### Test Admin User
A test admin user is automatically created:
- **Username:** `admin`
- **Email:** `admin@example.com`
- **Password:** `admin123`

### Registration Flow
1. Go to `http://localhost:5173/register`
2. Fill in username, email, and password
3. Check the **Spring Boot console** for the verification code
4. Enter the code to verify your email

### Login Flow
1. Go to `http://localhost:5173/login`
2. Enter username and password
3. Check the **Spring Boot console** for the 2FA code
4. Enter the code to complete login

### Testing RBAC
1. Login and navigate to "Test Protected Resources"
2. Try accessing different resource levels:
   - Admin Resource (requires ADMIN role)
   - Manager Resource (requires MANAGER or ADMIN)
   - User Resource (requires USER or higher)
   - Document Access (requires permission or JIT access)

### Testing JIT Access
1. Navigate to "JIT Access Management"
2. Request temporary access to a resource (e.g., `doc-123`)
3. Set duration (e.g., 5 minutes)
4. Observe the countdown timer
5. Test document access while JIT access is active
6. Access automatically expires after the duration

## Important Notes

- **Email Codes:** Verification and 2FA codes are printed to the Spring Boot console (no email server required)
- **Database:** H2 database is stored in `./data/securitydb` (file-based)
- **H2 Console:** Available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/securitydb`)
- **Sessions:** 30-minute timeout (configurable in `application.properties`)
- **JIT Access:** Automatic cleanup runs every 5 minutes

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/verify-email` - Verify email with code
- `POST /api/auth/login` - Login (step 1: password)
- `POST /api/auth/verify-2fa` - Verify 2FA code (step 2)
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Get current user

### Roles
- `POST /api/roles/assign` - Assign role to user (admin only)
- `DELETE /api/roles/revoke` - Revoke role from user (admin only)
- `GET /api/roles/user/{userId}` - Get user's roles
- `GET /api/roles/all` - Get all roles

### Protected Resources
- `GET /api/resources/admin` - Admin-only resource
- `GET /api/resources/manager` - Manager-level resource
- `GET /api/resources/user` - User-level resource
- `GET /api/resources/document/{id}` - Document access (requires permission or JIT)

### JIT Access
- `POST /api/jit/request` - Request temporary access
- `GET /api/jit/status/{resourceId}` - Check access status
- `POST /api/jit/revoke/{accessId}` - Revoke access
- `GET /api/jit/my-access` - Get all temporary access grants

## Default Roles & Permissions

### Organizational Roles
- **ADMIN**: Full system access, all permissions
- **MANAGER**: READ_DOCUMENTS, WRITE_DOCUMENTS
- **USER**: READ_DOCUMENTS

### Resource-Specific Roles
- **DOCUMENT_VIEWER**: READ_DOCUMENTS
- **DOCUMENT_EDITOR**: READ_DOCUMENTS, WRITE_DOCUMENTS

### Permissions
- READ_DOCUMENTS
- WRITE_DOCUMENTS
- DELETE_DOCUMENTS
- MANAGE_USERS
- ASSIGN_ROLES

## Project Structure

```
security-implementations/
├── src/main/java/infosec/securityimplementations/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities
│   ├── repository/      # JPA repositories
│   └── service/         # Business logic
├── frontend/
│   └── src/
│       ├── context/     # React context (Auth)
│       ├── pages/       # React pages
│       └── services/    # API service
└── data/                # H2 database files
```

## Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed
- Check if port 8080 is available
- Delete `./data` folder and restart to reset database

### Frontend won't start
- Run `npm install` in the frontend directory
- Check if port 5173 is available
- Clear browser cache and cookies

### Can't see verification codes
- Check the Spring Boot console/terminal output
- Codes are printed with clear separators (====)
- Codes expire after 10 minutes

### Session issues
- Clear browser cookies
- Logout and login again
- Check if backend is running

## License

This project is for educational purposes.
