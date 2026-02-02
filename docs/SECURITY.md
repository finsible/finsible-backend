# Finsible Security Documentation

This document provides comprehensive security documentation for the Finsible Personal Finance Management API.

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Authorization](#authorization)
- [JWT Token Management](#jwt-token-management)
- [Cookie Security](#cookie-security)
- [CORS Configuration](#cors-configuration)
- [Input Validation](#input-validation)
- [Error Handling](#error-handling)
- [Environment Variables](#environment-variables)
- [Security Checklist](#security-checklist)

---

## Overview

Finsible implements a multi-layered security architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Request                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CORS Filter                                  │
│  - Origin validation                                            │
│  - Allowed methods check                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Request ID Filter                              │
│  - Generates unique request ID for tracing                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               JWT Authentication Filter                          │
│  - Token extraction (Header/Cookie)                              │
│  - Token validation                                              │
│  - User verification                                             │
│  - Security context setup                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                Spring Security Filter Chain                      │
│  - URL-based authorization                                       │
│  - Method-level security (@PreAuthorize)                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Request Validation                            │
│  - Bean Validation (JSR-380)                                    │
│  - Custom validators                                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Controller / Service                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Authentication

### Google OAuth 2.0 Flow

Finsible uses Google OAuth 2.0 for authentication:

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │     │  Google  │     │ Finsible │     │ Database │
└────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
     │                │                │                │
     │ 1. Sign in     │                │                │
     │ ──────────────>│                │                │
     │                │                │                │
     │ 2. ID Token/   │                │                │
     │    Auth Code   │                │                │
     │ <──────────────│                │                │
     │                │                │                │
     │ 3. POST /auth/sign-in/google    │                │
     │ ────────────────────────────────>│                │
     │                │                │                │
     │                │ 4. Verify      │                │
     │                │    Token       │                │
     │                │<───────────────│                │
     │                │                │                │
     │                │ 5. User Info   │                │
     │                │────────────────>│                │
     │                │                │                │
     │                │                │ 6. Create/     │
     │                │                │    Update User │
     │                │                │────────────────>│
     │                │                │                │
     │                │                │ 7. Check Admin │
     │                │                │────────────────>│
     │                │                │                │
     │                │                │ 8. Generate    │
     │                │                │    JWT         │
     │                │                │                │
     │ 9. JWT Token (Cookie/Body)      │                │
     │ <────────────────────────────────│                │
     │                │                │                │
```

### Authentication Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/sign-in/google` | POST | Sign in with Google ID token |
| `/auth/sign-in/google-code` | POST | Sign in with Google auth code |
| `/auth/me` | GET | Get current user (authenticated) |
| `/auth/sign-out` | POST | Sign out user |

### Public Endpoints

The following endpoints do not require authentication:

```java
.requestMatchers(
    "/auth/sign-in/google-code",
    "/auth/sign-in/google",
    "/actuator/health",
).permitAll()
```

---

## Authorization

### Role-Based Access Control (RBAC)

Finsible implements two primary roles:

| Role | Description |
|------|-------------|
| `USER` | Standard user role (default) |
| `ADMIN` | Administrative access |

### Role Assignment

Roles are determined during JWT generation:

```java
// Admin check during sign-in
List<String> roles = adminRepository.findByEmail(email).isPresent()
    ? List.of("ADMIN", "USER")
    : List.of("USER");
```

### Method-Level Security

Admin-only endpoints use `@PreAuthorize`:

```java
@PreAuthorize("hasRole('ROLE_ADMIN')")
@PostMapping("/default")
public ResponseEntity<...> createDefaultCategory(...) {
    // Admin-only operation
}
```

### Protected Admin Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /categories/default` | Create default category |
| `PUT /categories/default/{id}` | Update default category |
| `DELETE /categories/default/{id}` | Delete default category |
| `POST /supported-currencies/` | Add supported currency |
| `DELETE /supported-currencies/{id}` | Remove supported currency |
| `POST /supported-languages/` | Add supported language |
| `DELETE /supported-languages/{id}` | Remove supported language |

---

## JWT Token Management

### Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user_id",
    "roles": ["USER"],
    "iat": 1706745600,
    "exp": 1706832000
  }
}
```

### Token Configuration

| Setting | Environment Variable | Description |
|---------|---------------------|-------------|
| Secret Key | `SECRET_KEY` | HMAC-SHA256 signing key |
| Expiration | `JWT_EXPIRATION` | Token lifetime (milliseconds) |

### Recommended Settings

```bash
# Production secret key (256+ bits)
SECRET_KEY="your-256-bit-secret-key-here-minimum-32-chars"

# 24-hour expiration
JWT_EXPIRATION=86400000

# 7-day expiration (if refresh tokens are implemented)
JWT_EXPIRATION=604800000
```

### Token Generation

```java
public static String generateToken(String userId, List<String> roles) {
    Key key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(key)
        .compact();
}
```

### Token Validation Flow

```java
// 1. Extract token from request
String token = extractFromHeaderOrCookie(request);

// 2. Validate signature and expiration
Claims claims = JwtService.validateToken(token);

// 3. Verify user exists in database
User user = userRepository.findById(claims.getSubject())
    .orElseThrow(() -> new UserNotFoundException("User not found"));

// 4. Set authentication context
SecurityContextHolder.getContext().setAuthentication(authentication);
```

---

## Cookie Security

### Cookie Configuration

| Setting | Development | Production |
|---------|-------------|------------|
| Secure | `false` | `true` |
| SameSite | `Lax` | `Strict` |
| HttpOnly | `true` | `true` |
| Path | `/` | `/` |
| Max-Age | JWT expiration | JWT expiration |

### Configuration Properties

```properties
# application-dev.properties
app.cookie.secure=false
app.cookie.same-site=Lax

# application-prod.properties
app.cookie.secure=true
app.cookie.same-site=Strict
```

### Cookie Handling

```java
// Setting authentication cookies
public void setAuthenticationCookies(HttpServletResponse response, String jwt) {
    ResponseCookie cookie = ResponseCookie.from("jwt_token", jwt)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite(cookieSameSite)
        .path("/")
        .maxAge(Duration.ofMillis(jwtExpiration))
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
}

// Clearing cookies on logout
public void clearAuthenticationCookies(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
}
```

### Device-Specific Token Delivery

| Device Type | Token Delivery |
|-------------|----------------|
| `web` | HttpOnly cookie |
| `mobile` | Response body |

```java
if (deviceType.equals(AppConstants.DEVICE_WEB)) {
    cookieHandler.setAuthenticationCookies(response, jwt);
}
if (!deviceType.equals(AppConstants.DEVICE_MOBILE)) {
    userResponseDTO.setJwt(null); // Clear from body
}
```

---

## CORS Configuration

### Allowed Origins

```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:5173",     // Vite dev server
    "http://127.0.0.1:5173"      // Alternative localhost
));
```

### Adding Production Origins

```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "https://app.finsible.org",  // Production frontend
    "https://*.finsible.org"     // Subdomains
));
```

### Allowed Methods

```java
configuration.setAllowedMethods(Arrays.asList(
    "GET", "POST", "PUT", "DELETE", "OPTIONS"
));
```

### Credentials Support

```java
configuration.setAllowCredentials(true);
```

⚠️ **Important:** When `allowCredentials` is `true`, you cannot use `*` for origins.

---

## Input Validation

### Bean Validation

All request DTOs use JSR-380 (Bean Validation 2.0):

```java
@Data
public class TransactionRequestDTO {
    @NotNull(message = "Transaction type must be provided", groups = Create.class)
    private Type type;

    @NotNull(message = "Total amount must be provided", groups = Create.class)
    @Digits(integer = 15, fraction = 4, message = "...")
    private BigDecimal totalAmount;

    @Size(max = 255, message = "Description can have maximum 255 characters")
    private String description;

    @Size(max = 3, min = 3, message = "Currency code must be exactly 3 characters")
    private String currency;
}
```

### Validation Groups

| Group | Purpose |
|-------|---------|
| `Create` | Validation for POST requests |
| `Update` | Validation for PUT requests |

### Custom Validators

```java
@AtLeastOneFieldNotNull(groups = Update.class)
public class TransactionRequestDTO {
    // At least one field required for updates
}
```

### Validation in Controllers

```java
@PostMapping
public ResponseEntity<...> createTransaction(
    @Validated(Create.class) @RequestBody TransactionRequestDTO dto
) {
    // Request validated before entering method
}
```

---

## Error Handling

### Global Exception Handler

All exceptions are handled by `GlobalExceptionHandler`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleAuthenticationException(...) {
        // 401 Unauthorized
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleAccessDeniedException(...) {
        // 403 Forbidden
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleValidationException(...) {
        // 400 Bad Request with validation errors
    }
}
```

### Error Response Format

```json
{
  "message": "Error description",
  "success": false,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "status": 401,
    "message": "Authentication failed",
    "referenceId": "REF-abc123-def456"
  }
}
```

### Error Reference IDs

Each error includes a unique reference ID for support troubleshooting:

```java
String traceId = MDC.get(RequestIdFilter.REQUEST_ID_KEY);
// If null, generate new UUID
String referenceId = "REF-" + (traceId != null ? traceId : UUID.randomUUID().toString().substring(0, 8));
```

### Security-Related HTTP Status Codes

| Code | Constant | Description |
|------|----------|-------------|
| 400 | `BAD_REQUEST` | Invalid input / Validation failed |
| 401 | `UNAUTHORIZED_REQUEST` | Authentication failed |
| 403 | `FORBIDDEN_REQUEST` | Insufficient permissions |
| 404 | `PAGE_NOT_FOUND` | Resource not found |
| 500 | `INTERNAL_SERVER_ERROR` | Server error |

---

## Environment Variables

### Required Security Variables

| Variable | Description | Security Notes |
|----------|-------------|----------------|
| `SECRET_KEY` | JWT signing secret | Min 256 bits, never commit to VCS |
| `JWT_EXPIRATION` | Token expiration (ms) | Balance security vs UX |
| `DB_PASSWORD` | Database password | Use secrets manager in prod |
| `REACT_WEB_APP_GOOGLE_CLIENT_SECRET` | Google OAuth secret | Keep confidential |

### Recommended Practices

```bash
# Use strong, random secrets
SECRET_KEY=$(openssl rand -base64 32)

# Store in environment, not files
export SECRET_KEY="..."

# Use secrets manager in production
# AWS Secrets Manager, HashiCorp Vault, etc.
```

---

## Security Checklist

### Development

- [ ] Use `.env` file for secrets (add to `.gitignore`)
- [ ] Enable debug logging for security
- [ ] Test authentication flows
- [ ] Validate CORS configuration

### Pre-Production

- [ ] Generate strong `SECRET_KEY` (256+ bits)
- [ ] Set appropriate `JWT_EXPIRATION`
- [ ] Configure production CORS origins
- [ ] Enable HTTPS
- [ ] Set `app.cookie.secure=true`
- [ ] Set `app.cookie.same-site=Strict`
- [ ] Review actuator endpoint exposure

### Production

- [ ] Use secrets manager for credentials
- [ ] Enable TLS 1.3
- [ ] Configure rate limiting
- [ ] Set up monitoring/alerting
- [ ] Review security headers
- [ ] Enable audit logging
- [ ] Regular security updates
- [ ] Penetration testing

### Security Headers (Recommended)

Add these headers in a filter or reverse proxy:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

---

## Security Considerations

### Implemented

✅ JWT-based stateless authentication  
✅ Google OAuth 2.0 integration  
✅ HttpOnly cookies for web clients  
✅ CORS configuration  
✅ Input validation (Bean Validation)  
✅ Role-based access control  
✅ SQL injection prevention (JPA)  
✅ Error handling without information leakage  
✅ Audit logging  

### Recommended Additions

⬜ Rate limiting  
⬜ Refresh tokens  
⬜ Account lockout  
⬜ IP-based blocking  
⬜ Security headers middleware  
⬜ Request/Response encryption  
⬜ API versioning  

---

*Last updated: January 31, 2025*
