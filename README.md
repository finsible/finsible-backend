# Finsible - Personal Finance Management API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue.svg" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg" alt="Spring Boot 3.4.0"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15+-blue.svg" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"/>
</p>

Finsible is a robust RESTful API backend for personal finance management built with Spring Boot. It provides comprehensive features for tracking income, expenses, managing accounts, categorizing transactions, and supporting multi-currency operations.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

## ✨ Features

- **🔐 Google OAuth Authentication** - Secure sign-in with Google OAuth 2.0
- **💰 Transaction Management** - Track income, expenses, and transfers
- **🏦 Multiple Account Types** - Support for cash, bank accounts, credit cards, debit cards, and loans
- **📊 Category Management** - Organize transactions with customizable categories
- **💱 Multi-Currency Support** - Handle multiple currencies with configurable defaults
- **👥 Space/Group Transactions** - Split expenses and track shared spending
- **📝 Audit Trail** - Full audit logging for all entities

## 🛠 Technology Stack

| Category       | Technology                  |
|----------------|-----------------------------|
| **Language**   | Java 21                     |
| **Framework**  | Spring Boot 3.4.0           |
| **Security**   | Spring Security + JWT       |
| **Database**   | PostgreSQL                  |
| **ORM**        | Spring Data JPA / Hibernate |
| **Migration**  | Flyway                      |
| **Build Tool** | Gradle (Kotlin DSL)         |
| **Logging**    | Logbook + SLF4J             |
| **Mapping**    | MapStruct                   |
| **API Client** | Google API Client           |

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **PostgreSQL 15+**
- **Gradle 8+** (or use the included wrapper)
- **Google Cloud Console** account (for OAuth setup)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/finsible/finsible-backend.git
   cd finsible
   ```

2. **Set up PostgreSQL database**
   ```bash
   # Create database
   createdb finsible_db
   
   # Or using psql
   psql -U postgres
   CREATE DATABASE finsible_db;
   ```

3. **Configure environment variables**
   ```bash
   # Copy the example environment file
   cp .env.example .env
   
   # Edit with your values (see Configuration section)
   ```

4. **Run database migrations**
   ```bash
   ./gradlew flywayMigrate
   ```

5. **Build the application**
   ```bash
   ./gradlew build
   ```

6. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The API will be available at `http://localhost:9090` (dev) or `http://localhost:8080` (default).

### Quick Start with Docker (Optional)

```bash
# Start PostgreSQL with Docker
docker run --name finsible-db -e POSTGRES_PASSWORD=password -e POSTGRES_DB=finsible_db -p 5432:5432 -d postgres:15

# Run the application
./gradlew bootRun
```

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root with the following variables:

| Variable                             | Description                                                                      | Required |
|--------------------------------------|----------------------------------------------------------------------------------|----------|
| `DB_URL`                             | PostgreSQL connection URL (e.g., `jdbc:postgresql://localhost:5432/finsible_db`) | ✅        |
| `DB_USERNAME`                        | Database username                                                                | ✅        |
| `DB_PASSWORD`                        | Database password                                                                | ✅        |
| `SECRET_KEY`                         | JWT signing secret (min 256 bits recommended)                                    | ✅        |
| `JWT_EXPIRATION`                     | JWT token expiration in milliseconds (e.g., `86400000` for 24h)                  | ✅        |
| `REACT_WEB_APP_GOOGLE_CLIENT_SECRET` | Google OAuth client secret                                                       | ✅        |
| `ADMIN_EMAIL`                        | Default admin email address                                                      | ❌        |

### Application Profiles

| Profile | Port | Description                    |
|---------|------|--------------------------------|
| `dev`   | 9090 | Development with debug logging |
| `prod`  | 8080 | Production with secure cookies |

Activate a profile:
```bash
# Via command line
./gradlew bootRun --args='--spring.profiles.active=dev'

# Or set environment variable
export SPRING_PROFILES_ACTIVE=dev
```

### Configuration Files

```
src/main/resources/
├── application.properties          # Common configuration
├── application-dev.properties      # Development overrides
└── application-prod.properties     # Production overrides
```

## 📖 API Documentation

For detailed API documentation, see [docs/API.md](docs/API.md).

### Base URL
- Development: `http://localhost:9090`
- Production: `https://your-domain.com`

### Authentication

All endpoints (except auth endpoints) require JWT authentication:

```bash
# Header-based (for mobile)
Authorization: Bearer <jwt_token>

# Cookie-based (for web)
# Automatically handled via HttpOnly cookies
```

### Quick API Reference

| Method             | Endpoint                     | Description                                                           |
|--------------------|------------------------------|-----------------------------------------------------------------------|
| **Authentication** |                              |                                                                       |
| POST               | `/auth/sign-in/google`       | Sign in with Google ID token (used in one tap login and mobile login) |
| POST               | `/auth/sign-in/google-code`  | Sign in with Google auth code (popup login)                           |
| GET                | `/auth/me`                   | Get current user profile                                              |
| POST               | `/auth/sign-out`             | Sign out user                                                         |
| **Accounts**       |                              |                                                                       |
| GET                | `/accounts`                  | List user's all accounts                                              |
| POST               | `/accounts/{accountGroupId}` | Create new account                                                    |
| PUT                | `/accounts/{accountId}`      | Update account                                                        |
| DELETE             | `/accounts/{accountId}`      | Delete account                                                        |
| **Transactions**   |                              |                                                                       |
| GET                | `/transactions`              | List transactions (paginated)                                         |
| GET                | `/transactions/{id}`         | Get transaction by ID                                                 |
| POST               | `/transactions`              | Create transaction                                                    |
| PUT                | `/transactions/{id}`         | Update transaction                                                    |
| DELETE             | `/transactions/{id}`         | Delete transaction                                                    |
| **Categories**     |                              |                                                                       |
| GET                | `/categories`                | List all categories                                                   |
| POST               | `/categories`                | Create user category                                                  |
| PUT                | `/categories/{id}`           | Update category                                                       |
| DELETE             | `/categories/{id}`           | Delete category                                                       |
| **Account Groups** |                              |                                                                       |
| GET                | `/account-groups`            | List account groups                                                   |
| POST               | `/account-groups`            | Create account group                                                  |
| PUT                | `/account-groups/{id}`       | Update account group                                                  |
| DELETE             | `/account-groups/{id}`       | Delete account group                                                  |

### Response Format

All responses follow a standard format:

```json
{
  "message": "Operation successful",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": { ... }
}
```

### Error Response Format

```json
{
  "message": "Error description",
  "success": false,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "status": 400,
    "message": "Detailed error message",
    "referenceId": "REF-abc123..."
  }
}
```

## 🗄 Database Schema

For detailed database documentation, see [docs/DATABASE.md](docs/DATABASE.md).

### Entity Relationship Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│     Users       │────<│    Accounts     │────<│   Transactions   │
└─────────────────┘     └─────────────────┘     └──────────────────┘
        │                       │                        │
        │                       │                        │
        v                       v                        v
┌─────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│   Categories    │     │  Account Groups │     │     Spaces       │
└─────────────────┘     └─────────────────┘     └──────────────────┘
```

### Core Tables

| Table                  | Description                            |
|------------------------|----------------------------------------|
| `users`                | User accounts and preferences          |
| `accounts`             | Financial accounts (cash, bank, cards) |
| `account_groups`       | Account type groupings                 |
| `transactions`         | Income, expense, and transfer records  |
| `categories`           | Transaction categorization             |
| `spaces`               | Shared expense groups                  |
| `supported_currencies` | Available currencies                   |
| `supported_languages`  | Available languages                    |

## 🔒 Security

For detailed security documentation, see [docs/SECURITY.md](docs/SECURITY.md).

### Authentication Flow

1. Client initiates Google OAuth sign-in
2. Google returns ID token or auth code
3. Backend validates token with Google
4. Backend generates JWT and returns to client
5. Client includes JWT in subsequent requests

### Security Features

- **JWT Authentication** - Stateless token-based auth
- **HttpOnly Cookies** - Secure cookie storage for web clients
- **CORS Configuration** - Restricted origin access
- **Role-Based Access** - Admin and User roles
- **Input Validation** - Request validation with Bean Validation
- **SQL Injection Prevention** - JPA parameterized queries
- **Audit Logging** - Track entity changes

## 🔧 Troubleshooting

For comprehensive troubleshooting, see [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md).

### Common Issues

#### Database Connection Failed
```
Error: Unable to obtain connection from database
```
**Solution:** Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables.

#### JWT Token Invalid
```
Error: Invalid JWT signature
```
**Solution:** Ensure `SECRET_KEY` is consistent across restarts and has sufficient length.

#### Google Auth Failed
```
Error: Security/IO exception
```
**Solution:** Verify `REACT_WEB_APP_GOOGLE_CLIENT_SECRET` matches your Google Cloud Console configuration.

#### CORS Errors
```
Error: Blocked by CORS policy
```
**Solution:** Add your frontend origin to `SecurityConfiguration.java`:
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:5173",
    "https://your-domain.com"
));
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "TransactionServiceTest"
```

## 📦 Deployment

### Production Checklist

1. ✅ Set `spring.profiles.active=prod`
2. ✅ Configure secure `SECRET_KEY` (256+ bits)
3. ✅ Enable HTTPS/TLS
4. ✅ Set `app.cookie.secure=true`
5. ✅ Configure production database
6. ✅ Set appropriate CORS origins
7. ✅ Review and limit actuator endpoints

### Build for Production

```bash
# Build JAR
./gradlew bootJar

# Run JAR
java -jar build/libs/Finsible-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

- 📧 Email: maitrymakwana196@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/finsible/issues)

---

<p align="center">Made with ❤️ by the Finsible Team</p>
