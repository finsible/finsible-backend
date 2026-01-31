# Finsible Quick Start Guide

Get Finsible up and running in 5 minutes.

## Prerequisites

- Java 21+
- PostgreSQL 15+
- Google Cloud Console account

## 1. Clone & Configure

```bash
# Clone the repository
git clone https://github.com/yourusername/finsible.git
cd finsible

# Copy environment template
cp .env.example .env
```

Edit `.env` with your values:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/finsible_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT (generate a random key)
SECRET_KEY=$(openssl rand -base64 32)
JWT_EXPIRATION=86400000

# Google OAuth (from Google Cloud Console)
REACT_WEB_APP_GOOGLE_CLIENT_SECRET=your_secret

# Admin email
ADMIN_EMAIL=your_email@example.com
```

## 2. Set Up Database

```bash
# Create database
createdb finsible_db

# Or using psql
psql -U postgres -c "CREATE DATABASE finsible_db;"
```

## 3. Run the Application

```bash
# Load environment variables
export $(cat .env | xargs)

# Run with Gradle
./gradlew bootRun
```

The API will be available at `http://localhost:9090`

## 4. Verify Installation

```bash
# Check health
curl http://localhost:9090/actuator/health

# Expected response:
# {"status":"UP"}
```

## 5. Test Authentication

Use your frontend application or curl to test Google authentication:

```bash
curl -X POST "http://localhost:9090/auth/sign-in/google?deviceType=mobile" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your_google_id_token",
    "clientId": "your_client_id",
    "defaultCurrencyCode": "INR",
    "defaultLanguageCode": "en"
  }'
```

## Next Steps

- 📖 Read [API Documentation](API.md)
- 🏗️ Explore [Architecture](ARCHITECTURE.md)
- 🗄️ Review [Database Schema](DATABASE.md)
- 🔒 Understand [Security](SECURITY.md)
- 🔧 Check [Troubleshooting](TROUBLESHOOTING.md)

## Common Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build production JAR
./gradlew bootJar

# Run production JAR
java -jar build/libs/Finsible-0.0.1-SNAPSHOT.jar
```

## Helpful Tips

1. **Development Profile**: Uses port 9090, debug logging enabled
2. **Default Data**: Flyway seeds default currencies, languages, categories, and account groups
3. **Admin Access**: Add your email to the `admins` table to get admin privileges
4. **API Testing**: Use the OpenAPI spec at `docs/openapi.yaml` with Swagger UI or Postman

## Troubleshooting Quick Fixes

| Issue | Quick Fix |
|-------|-----------|
| Port in use | `lsof -i :9090` then `kill -9 <PID>` |
| DB connection failed | Check PostgreSQL is running: `pg_isready` |
| Environment not loaded | `export $(cat .env \| xargs)` |
| JWT errors | Regenerate SECRET_KEY and re-authenticate |

For detailed troubleshooting, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
