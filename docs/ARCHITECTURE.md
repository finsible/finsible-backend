# Finsible System Architecture

This document provides an overview of the Finsible system architecture, including component diagrams, service interactions, and design patterns.

## Table of Contents

- [High-Level Architecture](#high-level-architecture)
- [Application Layers](#application-layers)
- [Component Diagram](#component-diagram)
- [Request Flow](#request-flow)
- [Authentication Flow](#authentication-flow)
- [Data Flow](#data-flow)
- [Technology Stack](#technology-stack)
- [Package Structure](#package-structure)
- [Design Patterns](#design-patterns)
- [Scalability Considerations](#scalability-considerations)

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Client Applications                             │
├───────────────────────────────────┬─────────────────────────────────────────┤
│           Web (React/Vite)        │           Mobile (Kotlin)          │
│       http://localhost:5173       │              Native Apps                 │
└───────────────────────────────────┴─────────────────────────────────────────┘
                                    │
                                    │ HTTPS / HTTP
                                    │ JWT (Cookie or Header)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Finsible Backend API                                 │
│                      Spring Boot 3.4.0 / Java 21                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   Security   │  │  Controllers │  │   Services   │  │ Repositories │    │
│  │   Filters    │  │     (REST)   │  │   (Logic)    │  │    (JPA)     │    │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
│    PostgreSQL     │    │   Google OAuth    │    │    Logbook        │
│    Database       │    │   (External)      │    │   (Logging)       │
│   Port: 5432      │    │                   │    │                   │
└───────────────────┘    └───────────────────┘    └───────────────────┘
```

---

## Application Layers

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Presentation Layer                                │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         REST Controllers                            │   │
│  │  AuthController, AccountController, TransactionController,          │   │
│  │  CategoryController, AccountGroupController, CurrencyController,    │   │
│  │  LanguageController                                                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      DTOs (Request/Response)                        │   │
│  │  TransactionRequestDTO, AccountResponseDTO, UserResponseDTO, etc.  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Business Layer                                    │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Services                                  │   │
│  │  AuthService, AccountService, TransactionService, CategoryService, │   │
│  │  AccountGroupService, CurrencyService, LanguageService, JwtService │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Mappers                                   │   │
│  │  AccountMapper, TransactionMapper, CategoryMapper, UserMapper, etc.│   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                          Validators                                 │   │
│  │  Custom validators (AtLeastOneFieldNotNull, etc.)                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Persistence Layer                                   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         Repositories                                │   │
│  │  UserRepository, AccountRepository, TransactionRepository,         │   │
│  │  CategoryRepository, AccountGroupRepository, etc.                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Entities                                  │   │
│  │  User, Account, Transaction, Category, AccountGroup, Space,        │   │
│  │  CreditCardDetail, DebitCardDetail, LoanDetail, etc.               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Infrastructure Layer                                │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         Configuration                               │   │
│  │  SecurityConfiguration, JpaConfig                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Filters                                   │   │
│  │  JwtAuthenticationFilter, RequestIdFilter                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       Cross-Cutting Concerns                        │   │
│  │  GlobalExceptionHandler, AuditorAwareImpl, CookieHandler           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Finsible Backend                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │                     Security Components                           │    │
│   │                                                                   │    │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │    │
│   │  │ RequestIdFilter │─▶│   JwtAuth       │─▶│ SecurityFilter  │  │    │
│   │  │                 │  │   Filter        │  │   Chain         │  │    │
│   │  └─────────────────┘  └─────────────────┘  └─────────────────┘  │    │
│   │           │                    │                    │            │    │
│   │           ▼                    ▼                    ▼            │    │
│   │  ┌─────────────────────────────────────────────────────────┐    │    │
│   │  │                    JwtService                           │    │    │
│   │  │  - generateToken()                                      │    │    │
│   │  │  - validateToken()                                      │    │    │
│   │  └─────────────────────────────────────────────────────────┘    │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                                                                             │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │                         Controllers                               │    │
│   │                                                                   │    │
│   │  ┌──────────┐ ┌───────────┐ ┌─────────────┐ ┌──────────────┐    │    │
│   │  │   Auth   │ │  Account  │ │ Transaction │ │   Category   │    │    │
│   │  │Controller│ │ Controller│ │  Controller │ │  Controller  │    │    │
│   │  └──────────┘ └───────────┘ └─────────────┘ └──────────────┘    │    │
│   │  ┌───────────────┐ ┌──────────────┐ ┌──────────────┐            │    │
│   │  │ AccountGroup  │ │   Currency   │ │   Language   │            │    │
│   │  │  Controller   │ │  Controller  │ │  Controller  │            │    │
│   │  └───────────────┘ └──────────────┘ └──────────────┘            │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                                    │                                        │
│                                    ▼                                        │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │                          Services                                 │    │
│   │                                                                   │    │
│   │  ┌──────────┐ ┌───────────┐ ┌─────────────┐ ┌──────────────┐    │    │
│   │  │   Auth   │ │  Account  │ │ Transaction │ │   Category   │    │    │
│   │  │ Service  │ │  Service  │ │   Service   │ │   Service    │    │    │
│   │  └──────────┘ └───────────┘ └─────────────┘ └──────────────┘    │    │
│   │  ┌───────────────┐ ┌──────────────┐ ┌──────────────┐            │    │
│   │  │ AccountGroup  │ │   Currency   │ │   Language   │            │    │
│   │  │   Service     │ │   Service    │ │   Service    │            │    │
│   │  └───────────────┘ └──────────────┘ └──────────────┘            │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                                    │                                        │
│                                    ▼                                        │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │                        Repositories                               │    │
│   │                                                                   │    │
│   │  UserRepository, AccountRepository, TransactionRepository,       │    │
│   │  CategoryRepository, AccountGroupRepository, AdminRepository,    │    │
│   │  CurrencyRepository, LanguageRepository, CreditCardRepository,  │    │
│   │  DebitCardRepository, LoanRepository                             │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                                    │                                        │
└────────────────────────────────────┼────────────────────────────────────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │      PostgreSQL       │
                         │       Database        │
                         └───────────────────────┘
```

---

## Request Flow

### Typical API Request Flow

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Client  │────▶│ CORS Filter  │────▶│ RequestId    │────▶│ JWT Auth     │
│          │     │              │     │ Filter       │     │ Filter       │
└──────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                                                                  │
                      ┌───────────────────────────────────────────┘
                      │
                      ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Security     │────▶│ Controller   │────▶│ Service      │────▶│ Repository   │
│ Filter Chain │     │              │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                                                                       │
                      ┌────────────────────────────────────────────────┘
                      │
                      ▼
              ┌──────────────┐
              │  PostgreSQL  │
              │   Database   │
              └──────────────┘

```

### Detailed Request Processing

```
1. Client Request
       │
       ▼
2. CORS Filter
   - Validate origin
   - Check allowed methods
       │
       ▼
3. RequestIdFilter
   - Generate unique request ID
   - Store in MDC for logging
       │
       ▼
4. JwtAuthenticationFilter
   - Extract JWT from header/cookie
   - Validate token signature
   - Verify user exists
   - Set SecurityContext
       │
       ▼
5. Spring Security Filter Chain
   - Check URL authorization
   - Check method-level @PreAuthorize
       │
       ▼
6. Controller
   - Extract path variables, params
   - Validate request body (@Validated)
   - Call service method
       │
       ▼
7. Service
   - Business logic
   - Call repository methods
   - Use mappers for DTO conversion
       │
       ▼
8. Repository
   - JPA/Hibernate operations
   - Database queries
       │
       ▼
9. Response
   - Wrap in BaseResponse
   - Return to client
```

---

## Authentication Flow

### Google OAuth Sign-In Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │     │   Google    │     │  Finsible   │     │  Database   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │
       │ 1. Initiate       │                   │                   │
       │    Google Sign-In │                   │                   │
       │──────────────────▶│                   │                   │
       │                   │                   │                   │
       │ 2. User consents  │                   │                   │
       │◀──────────────────│                   │                   │
       │                   │                   │                   │
       │ 3. Return ID Token or Auth Code       │                   │
       │◀──────────────────│                   │                   │
       │                   │                   │                   │
       │ 4. POST /auth/sign-in/google          │                   │
       │   {token, clientId, currency, lang}   │                   │
       │──────────────────────────────────────▶│                   │
       │                   │                   │                   │
       │                   │ 5. Verify token   │                   │
       │                   │◀──────────────────│                   │
       │                   │                   │                   │
       │                   │ 6. User info      │                   │
       │                   │──────────────────▶│                   │
       │                   │                   │                   │
       │                   │                   │ 7. Find/Create   │
       │                   │                   │    User          │
       │                   │                   │──────────────────▶│
       │                   │                   │                   │
       │                   │                   │ 8. Check Admin   │
       │                   │                   │──────────────────▶│
       │                   │                   │                   │
       │                   │                   │ 9. Generate JWT  │
       │                   │                   │                   │
       │ 10. Set Cookie (web) or Return JWT (mobile)               │
       │◀──────────────────────────────────────│                   │
       │                   │                   │                   │
```

---

## Data Flow

### Transaction Creation Flow

```
┌───────────────────────────────────────────────────────────────────────────┐
│                        Transaction Creation                                │
└───────────────────────────────────────────────────────────────────────────┘

1. Request:
   POST /transactions
   {
     "type": "EXPENSE",
     "totalAmount": 500.00,
     "transactionDate": 1706659200000,
     "categoryId": 5,
     "fromAccountId": 1,
     "description": "Dinner"
   }
           │
           ▼
2. TransactionController
   - Validate TransactionRequestDTO
   - Extract userId from request attribute
           │
           ▼
3. TransactionService.createTransaction()
   │
   ├──▶ Validate category exists and belongs to user
   │         │
   │         ▼
   │    CategoryRepository.findById()
   │
   ├──▶ Validate fromAccount exists and belongs to user
   │         │
   │         ▼
   │    AccountRepository.findByIdAndUser()
   │
   ├──▶ Get currency
   │         │
   │         ▼
   │    CurrencyService.getCurrencyOrDefault()
   │
   ├──▶ Build Transaction entity
   │
   ├──▶ Save Transaction
   │         │
   │         ▼
   │    TransactionRepository.save()
   │
   └──▶ Map to Response DTO
             │
             ▼
        TransactionMapper.toResponseDTO()
           │
           ▼
4. Response:
   {
     "message": "Transaction created successfully",
     "success": true,
     "data": {
       "id": 123,
       "type": "EXPENSE",
       "totalAmount": "500.0000",
       ...
     }
   }
```

---

## Technology Stack

### Core Framework

| Component  | Technology  | Version          |
|------------|-------------|------------------|
| Runtime    | Java        | 21               |
| Framework  | Spring Boot | 3.4.0            |
| Build Tool | Gradle      | 8.x (Kotlin DSL) |

### Security

| Component      | Technology        |
|----------------|-------------------|
| Authentication | Spring Security   |
| Token          | JWT (jjwt 0.11.5) |
| OAuth          | Google API Client |

### Data

| Component       | Technology      |
|-----------------|-----------------|
| Database        | PostgreSQL 15+  |
| ORM             | Hibernate / JPA |
| Migrations      | Flyway          |
| Connection Pool | HikariCP        |

### Utilities

| Component   | Technology          |
|-------------|---------------------|
| DTO Mapping | MapStruct 1.5.5     |
| Boilerplate | Lombok              |
| Logging     | Logbook + SLF4J     |
| Validation  | Bean Validation 3.0 |

---

## Package Structure

```
org.finsible.backend/
├── FinsibleApplication.java       # Main application entry point
├── AppConstants.java              # Application constants
├── BaseResponse.java              # Standard response wrapper
├── ErrorDetails.java              # Error response details
├── GlobalExceptionHandler.java    # Centralized exception handling
├── AuditorAwareImpl.java          # JPA auditing implementation
│
├── configuration/
│   ├── SecurityConfiguration.java # Spring Security config
│   └── JpaConfig.java             # JPA/Hibernate config
│
├── controller/
│   ├── AuthController.java        # Authentication endpoints
│   ├── AccountController.java     # Account CRUD
│   ├── AccountGroupController.java
│   ├── TransactionController.java
│   ├── CategoryController.java
│   ├── CurrencyController.java
│   └── LanguageController.java
│
├── service/
│   ├── AuthService.java
│   ├── AccountService.java
│   ├── AccountGroupService.java
│   ├── TransactionService.java
│   ├── CategoryService.java
│   ├── CurrencyService.java
│   ├── LanguageService.java
│   └── JwtService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   ├── AccountGroupRepository.java
│   ├── TransactionRepository.java
│   ├── CategoryRepository.java
│   ├── AdminRepository.java
│   └── ... (other repositories)
│
├── entity/
│   ├── User.java
│   ├── Account.java
│   ├── AccountGroup.java
│   ├── Transaction.java
│   ├── Category.java
│   ├── Type.java                  # Enum: INCOME, EXPENSE, TRANSFER
│   ├── Space.java
│   ├── SupportedCurrency.java
│   ├── SupportedLanguage.java
│   ├── CreditCardDetail.java
│   ├── DebitCardDetail.java
│   ├── LoanDetail.java
│   └── Admin.java
│
├── dto/
│   ├── request/
│   │   ├── TransactionRequestDTO.java
│   │   ├── AccountRequestDTO.java
│   │   ├── CategoryRequestDTO.java
│   │   └── ... (other request DTOs)
│   └── response/
│       ├── TransactionResponseDTO.java
│       ├── AccountResponseDTO.java
│       ├── CategoryResponseDTO.java
│       └── ... (other response DTOs)
│
├── mapper/
│   ├── AccountMapper.java
│   ├── TransactionMapper.java
│   ├── CategoryMapper.java
│   └── ... (other MapStruct mappers)
│
├── filter/
│   ├── JwtAuthenticationFilter.java
│   └── RequestIdFilter.java
│
├── utility/
│   └── CookieHandler.java
│
├── validator/
│   └── AtLeastOneFieldNotNull.java
│
├── CustomExceptionHandler/
│   ├── EntityNotFoundException.java
│   ├── InvalidTokenException.java
│   └── UserNotFoundException.java
│
└── Logger/
    └── (logging utilities)
```

---

## Design Patterns

### Patterns Used

| Pattern          | Location                          | Purpose                      |
|------------------|-----------------------------------|------------------------------|
| **MVC**          | Controllers/Services/Repositories | Separation of concerns       |
| **Repository**   | Repository layer                  | Data access abstraction      |
| **DTO**          | dto package                       | Data transfer between layers |
| **Builder**      | Entities (Lombok @Builder)        | Object construction          |
| **Filter Chain** | Security filters                  | Request processing pipeline  |
| **Factory**      | JwtService                        | Token generation             |
| **Strategy**     | Validation groups                 | Different validation rules   |
| **Singleton**    | Spring Beans                      | Managed components           |

### Layer Dependencies

```
Controllers ──▶ Services ──▶ Repositories ──▶ Database
     │              │              │
     ▼              ▼              ▼
   DTOs          Mappers       Entities
```

### Dependency Injection

All components use constructor injection:

```java
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }
}
```

---

## Scalability Considerations

### Current Architecture

- **Stateless Design**: JWT-based auth, no session storage
- **Connection Pooling**: HikariCP for database connections
- **Efficient Queries**: Indexed database columns, JPA specifications

### Horizontal Scaling

```
                          ┌─────────────────┐
                          │  Load Balancer  │
                          └────────┬────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           │                       │                       │
           ▼                       ▼                       ▼
    ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
    │  Finsible   │         │  Finsible   │         │  Finsible   │
    │ Instance 1  │         │ Instance 2  │         │ Instance 3  │
    └─────────────┘         └─────────────┘         └─────────────┘
           │                       │                       │
           └───────────────────────┼───────────────────────┘
                                   │
                                   ▼
                          ┌─────────────────┐
                          │   PostgreSQL    │
                          │    (Primary)    │
                          └────────┬────────┘
                                   │
                          ┌────────┴────────┐
                          ▼                 ▼
                   ┌──────────────┐ ┌──────────────┐
                   │   Replica 1  │ │   Replica 2  │
                   └──────────────┘ └──────────────┘
```

### Future Improvements

1. **Caching Layer**: Redis for frequently accessed data
2. **Message Queue**: Async processing for heavy operations
3. **Read Replicas**: Scale database reads
4. **API Gateway**: Rate limiting, routing, monitoring
5. **Containerization**: Docker + Kubernetes deployment

---

*Last updated: January 31, 2025*
