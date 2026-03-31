# Finsible Troubleshooting Guide

This document provides solutions to common issues encountered when developing, deploying, or running Finsible.

## Table of Contents

- [Quick Diagnostic Commands](#quick-diagnostic-commands)
- [Application Startup Issues](#application-startup-issues)
- [Database Issues](#database-issues)
- [Authentication Issues](#authentication-issues)
- [API Issues](#api-issues)
- [Build Issues](#build-issues)
- [Production Issues](#production-issues)
- [Error Code Reference](#error-code-reference)
- [Getting Support](#getting-support)

---

## Quick Diagnostic Commands

### Check Application Health

```bash
# Health endpoint
curl http://localhost:9090/actuator/health
```

### Check Database Connection

```bash
# Test PostgreSQL connection
psql -h localhost -U your_username -d finsible_db -c "SELECT 1"

# Flyway migrations run automatically on application startup.
# Check recent Flyway logs:
grep -i "flyway" logs/finsible.log | tail ~50
# Alternatively, if you have Flyway CLI configured, run (adjust
# config as needed) :
# flyway info
```

### View Application Logs

```bash
# Follow logs in real-time
tail -f logs/finsible.log

# Search for errors
grep -i "error\|exception" logs/finsible.log | tail -50
```

### Check Environment Variables

```bash
# Verify required variables are set
echo "DB_URL: $DB_URL"
echo "DB_USERNAME: $DB_USERNAME"
echo "SECRET_KEY: ${SECRET_KEY:+SET}"
echo "JWT_EXPIRATION: $JWT_EXPIRATION"
```

---

## Application Startup Issues

### Issue: Application fails to start

**Symptoms:**
```
Error starting ApplicationContext
Failed to configure a DataSource
```

**Solution:**

1. Verify database environment variables:
   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/finsible_db"
   export DB_USERNAME="your_username"
   export DB_PASSWORD="your_password"
   ```

2. Check if PostgreSQL is running:
   ```bash
   # macOS
   brew services list | grep postgresql
   
   # Linux
   sudo systemctl status postgresql
   ```

3. Test database connectivity:
   ```bash
   psql -h localhost -U your_username -d finsible_db
   ```

---

### Issue: Port already in use

**Symptoms:**
```
Web server failed to start. Port 9090 was already in use.
```

**Solution:**

1. Find the process using the port:
   ```bash
   # macOS/Linux
   lsof -i :9090
   
   # Kill the process
   kill -9 <PID>
   ```

2. Or change the port:
   ```bash
   ./gradlew bootRun --args='--server.port=8081'
   ```

---

### Issue: JWT_EXPIRATION NumberFormatException

**Symptoms:**
```
java.lang.NumberFormatException: For input string: "null"
```

**Solution:**

Set the `JWT_EXPIRATION` environment variable:
```bash
export JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

---

### Issue: SECRET_KEY is null

**Symptoms:**
```
java.lang.NullPointerException at JwtService.generateToken
```

**Solution:**

Set the `SECRET_KEY` environment variable:
```bash
# Generate a secure key
export SECRET_KEY=$(openssl rand -base64 32)

# Or set a specific key (min 32 characters)
export SECRET_KEY="your-very-secure-secret-key-minimum-32-chars"
```

---

## Database Issues

### Issue: Flyway migration failed

**Symptoms:**
```
FlywayException: Validate failed: Migrations have failed validation
```

**Solution:**
Check status using the Flyway CLI (configured via `flyway.conf` or environment variables):
1. Check migration status:
   ```bash
   ./gradlew flywayInfo
   ```

2. If in development, repair the schema history:
   ```bash
   ./gradlew flywayRepair
   ./gradlew flywayMigrate
   ```

3. For checksum mismatches, update the migration file or baseline:
   ```bash
   ./gradlew flywayBaseline -Pflyway.baselineVersion=2
   ```

---

### Issue: Database connection refused

**Symptoms:**
```
Connection refused to host: localhost, port: 5432
```

**Solution:**

1. Start PostgreSQL:
   ```bash
   # macOS with Homebrew
   brew services start postgresql@15
   
   # Linux
   sudo systemctl start postgresql
   
   # Docker
   docker start finsible-db
   ```

2. Check PostgreSQL is listening:
   ```bash
   pg_isready -h localhost -p 5432
   ```

---

### Issue: Database authentication failed

**Symptoms:**
```
FATAL: password authentication failed for user "finsible"
```

**Solution:**

1. Verify credentials:
   ```bash
   psql -h localhost -U your_username -d finsible_db
   ```

2. Reset password if needed:
   ```sql
   ALTER USER your_username WITH PASSWORD 'new_password';
   ```

3. Check pg_hba.conf for authentication method.

---

### Issue: Table already exists

**Symptoms:**
```
ERROR: relation "users" already exists
```

**Solution:**

1. If starting fresh, drop and recreate database:
   ```bash
   dropdb finsible_db
   createdb finsible_db
   ./gradlew bootRun
   ```

2. Or use Flyway baseline:
   ```bash
   ./gradlew flywayBaseline
   ```

---

## Authentication Issues

### Issue: Invalid JWT signature

**Symptoms:**
```json
{
  "message": "Authentication failed. Please try again.",
  "success": false,
  "data": {
    "status": 401,
    "message": "Invalid JWT signature"
  }
}
```

**Causes:**
1. `SECRET_KEY` changed between token generation and validation
2. Token was modified/corrupted
3. Token from different environment

**Solution:**

1. Ensure `SECRET_KEY` is consistent:
   ```bash
   # Check current value
   echo $SECRET_KEY
   
   # Set it properly
   export SECRET_KEY="consistent-key-across-restarts"
   ```

2. Clear cookies and re-authenticate:
   ```bash
   # Client-side: Clear cookies
   # Then sign in again
   ```

---

### Issue: Token expired

**Symptoms:**
```json
{
  "message": "Authentication failed. Please try again.",
  "success": false,
  "data": {
    "status": 401,
    "message": "JWT expired"
  }
}
```

**Solution:**

1. Re-authenticate to get a new token

2. Increase token expiration if needed:
   ```bash
   export JWT_EXPIRATION=604800000  # 7 days
   ```

---

### Issue: Google authentication failed

**Symptoms:**
```json
{
  "message": "Internal server error",
  "success": false,
  "data": {
    "message": "Security/IO exception"
  }
}
```

**Causes:**
1. Invalid Google client secret
2. Token verification failed
3. Network issues reaching Google

**Solution:**

1. Verify Google Cloud Console configuration:
   - Client ID matches frontend
   - Client secret is correct
   - Authorized origins include your domain

2. Check environment variable:
   ```bash
   echo $REACT_WEB_APP_GOOGLE_CLIENT_SECRET
   ```

3. Test Google connectivity:
   ```bash
   curl https://oauth2.googleapis.com/.well-known/openid-configuration
   ```

---

### Issue: User not found after authentication

**Symptoms:**
```
UserNotFoundException: User not found
```

**Solution:**

This can happen if:
1. User was deleted from database
2. Token contains invalid user ID

Clear cookies and re-authenticate.

---

### Issue: CORS blocked

**Symptoms (Browser Console):**
```
Access to fetch at 'http://localhost:9090/auth/me' from origin 
'http://localhost:5173' has been blocked by CORS policy
```

**Solution:**

1. Add your origin to `SecurityConfiguration.java`:
   ```java
   configuration.setAllowedOriginPatterns(Arrays.asList(
       "http://localhost:5173",
       "http://localhost:3000",  // Add your frontend origin
       "https://your-domain.com"
   ));
   ```

2. Rebuild and restart:
   ```bash
   ./gradlew bootRun
   ```

---

## API Issues

### Issue: 400 Bad Request - Validation Error

**Symptoms:**
```json
{
  "message": "The request could not be understood or was missing required parameters.",
  "success": false,
  "data": {
    "status": 400,
    "message": "Transaction type must be provided"
  }
}
```

**Solution:**

Check the validation rules for the endpoint. Required fields for creation:

**Transaction:**
- `type` (INCOME, EXPENSE, TRANSFER)
- `totalAmount`
- `transactionDate`
- `categoryId`

**Account:**
- `name`

**Category:**
- `name`
- `type`

---

### Issue: 403 Forbidden

**Symptoms:**
```json
{
  "message": "You do not have permission to perform this action.",
  "success": false,
  "data": {
    "status": 403
  }
}
```

**Causes:**
1. Trying to access admin-only endpoint as regular user
2. Trying to modify another user's resources

**Solution:**

1. Check if endpoint requires admin role:
   - `/categories/default/*`
   - `/supported-currencies/`
   - `/supported-languages/`

2. Add user as admin:
   ```sql
   INSERT INTO admins (email, created_at, updated_at, created_by, updated_by)
   VALUES ('user@example.com', NOW(), NOW(), 'system', 'system');
   ```

---

### Issue: 400 Bad Request - Entity

**Symptoms:**
```json
{
  "message": "The request could not be understood or was missing required parameters.",
  "data": {
    "status": 400,
    "message": "Account not found with ID: 999"
  }
}
```

**Solution:**

1. Verify the resource exists:
   ```sql
   SELECT * FROM accounts WHERE id = 999;
   ```

2. Verify the resource belongs to the authenticated user:
   ```sql
   SELECT * FROM accounts WHERE id = 999 AND user_id = 'your-user-id';
   ```

---

### Issue: Request body not being read

**Symptoms:**
```
HttpMessageNotReadableException: Required request body is missing
```

**Solution:**

1. Ensure `Content-Type: application/json` header is set
2. Verify JSON is valid:
   ```bash
   echo '{"name": "test"}' | jq .
   ```

---

## Build Issues

### Issue: Gradle build failed

**Symptoms:**
```
> Task :compileJava FAILED
```

**Solution:**

1. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

2. Check Java version:
   ```bash
   java -version
   # Should be Java 21+
   ```

3. Update Gradle wrapper to the latest supported Gradle version:
   ```bash
   •/gradlew wrapper --gradle-version <latest-supported-version>
   ```
---

### Issue: MapStruct not generating implementations

**Symptoms:**
```
Mapper implementation not found
```

**Solution:**

1. Ensure annotation processor is configured:
   ```kotlin
   // build.gradle.kts
   annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
   ```

2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

3. Check `build/generated/sources/annotationProcessor` for generated files

---

### Issue: Lombok not working

**Symptoms:**
```
cannot find symbol: method getXxx()
```

**Solution:**

1. Ensure Lombok is configured:
   ```kotlin
   compileOnly("org.projectlombok:lombok")
   annotationProcessor("org.projectlombok:lombok")
   ```

2. Enable annotation processing in IDE:
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable

3. Install Lombok plugin in IDE

---

## Production Issues

### Issue: High memory usage

**Symptoms:**
- Application becomes slow
- OutOfMemoryError

**Solution:**

1. Configure JVM memory:
   ```bash
   java -Xms512m -Xmx1024m -jar Finsible.jar
   ```

2. Enable GC logging:
   ```bash
   java -Xlog:gc*:gc.log -jar Finsible.jar
   ```

3. Check for connection pool leaks:
   ```yaml
   spring.datasource.hikari.maximum-pool-size=10
   spring.datasource.hikari.leak-detection-threshold=30000
   ```

---

### Issue: Slow database queries

**Symptoms:**
- API responses are slow
- High database CPU

**Solution:**

1. Enable query logging:
   ```properties
   # application-dev.properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   ```

2. Check for N+1 queries
3. Add missing indexes
4. Use pagination for large result sets

---

### Issue: Cookie not being sent

**Symptoms:**
- Web client not authenticated after sign-in
- Cookie visible in browser but not sent

**Solution:**

1. Check SameSite attribute:
   ```properties
   # For cross-origin: use None (requires Secure)
   app.cookie.same-site=None
   app.cookie.secure=true
   ```

2. Ensure frontend includes credentials:
   ```javascript
   fetch('/api/me', { credentials: 'include' })
   ```

3. Check CORS allows credentials:
   ```java
   configuration.setAllowCredentials(true);
   ```

---

## Error Code Reference

### HTTP Status Codes

| Code | Constant | Common Causes |
|------|----------|---------------|
| 400 | `BAD_REQUEST` | Validation failed, Invalid input |
| 401 | `UNAUTHORIZED_REQUEST` | Missing/invalid/expired token |
| 403 | `FORBIDDEN_REQUEST` | Insufficient permissions |
| 404 | `PAGE_NOT_FOUND` | Resource doesn't exist |
| 500 | `INTERNAL_SERVER_ERROR` | Unhandled exception |

### Error Messages

| Message | Meaning | Action |
|---------|---------|--------|
| "User not found" | Token contains invalid user ID | Re-authenticate |
| "Invalid JWT signature" | Token was tampered or SECRET_KEY changed | Re-authenticate, check SECRET_KEY |
| "Entity not found" | Requested resource doesn't exist | Verify resource ID |
| "Validation error" | Request didn't pass validation | Check required fields |
| "Access denied" | User lacks required role | Check permissions |

### Reference IDs

Every error includes a `referenceId` for support:
```json
{
  "data": {
    "referenceId": "REF-abc123-def456"
  }
}
```

Use this ID when:
1. Checking server logs
2. Contacting support
3. Debugging issues

```bash
# Find error in logs by reference ID
grep "abc123" logs/finsible.log
```

---

## Getting Support

### Before Requesting Support

1. **Check this guide** for common solutions
2. **Review error message** and reference ID
3. **Check application logs** for stack traces
4. **Verify environment variables** are set correctly
5. **Test with minimal reproduction** steps

### Information to Provide

When requesting support, include:

1. **Error message** and reference ID
2. **Request details** (endpoint, method, headers)
3. **Request body** (sanitized of secrets)
4. **Environment** (dev/prod, OS, Java version)
5. **Relevant log entries**
6. **Steps to reproduce**

### Log Locations

| Type | Location |
|------|----------|
| Console output | Terminal/stdout |
| Application logs | `logs/finsible.log` |
| Tomcat logs | Embedded in console |
| Hibernate SQL | Console (when enabled) |

### Useful Log Searches

```bash
# Find errors
grep -i "error\|exception" logs/*.log

# Find by trace ID
grep "REF-abc123" logs/*.log

# Find authentication issues
grep "Authentication\|JWT\|token" logs/*.log

# Find database issues
grep "SQL\|database\|connection" logs/*.log
```

---

## Diagnostic Endpoints

### Health Check
```bash
curl http://localhost:9090/actuator/health
```

### Application Info
```bash
curl http://localhost:9090/actuator/info
```

### API Mappings
```bash
curl http://localhost:9090/actuator/mappings | jq '.contexts.application.mappings'
```

### Environment (if enabled)
```bash
curl http://localhost:9090/actuator/env
```

---

*Last updated: January 31, 2025*
