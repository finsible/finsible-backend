# Finsible API Documentation

This document provides comprehensive API documentation for the Finsible Personal Finance Management API.

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Base Response Format](#base-response-format)
- [Error Handling](#error-handling)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication-endpoints)
  - [Accounts](#account-endpoints)
  - [Account Groups](#account-group-endpoints)
  - [Transactions](#transaction-endpoints)
  - [Categories](#category-endpoints)
  - [Currencies](#currency-endpoints)
  - [Languages](#language-endpoints)

---

## Overview

### Base URLs

| Environment | URL                        |
|-------------|----------------------------|
| Development | `http://localhost:9090`    |
| Production  | `https://api.finsible.org` |

### Content Type

All requests and responses use JSON:
```
Content-Type: application/json
```

### HTTP Methods

| Method | Description               |
|--------|---------------------------|
| GET    | Retrieve resources        |
| POST   | Create new resources      |
| PUT    | Update existing resources |
| DELETE | Remove resources          |

---

## Authentication

### JWT Token Authentication

Finsible uses JWT (JSON Web Token) for authentication. Tokens can be provided via:

1. **Authorization Header** (Mobile clients)
   ```
   Authorization: Bearer <jwt_token>
   ```

2. **HttpOnly Cookie** (Web clients)
   - Automatically set on sign-in
   - Cleared on sign-out

### Token Claims

```json
{
  "sub": "user_id",
  "roles": ["USER"],
  "iat": 1706745600,
  "exp": 1706832000
}
```

### Protected Endpoints

All endpoints except the following require authentication:
- `POST /auth/sign-in/google`
- `POST /auth/sign-in/google-code`
- `GET /actuator/health`

---

## Base Response Format

### Success Response

```json
{
  "message": "Operation successful",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    // Response payload
  }
}
```

### Error Response

```json
{
  "message": "Error description",
  "success": false,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "status": 400,
    "message": "Detailed error message",
    "referenceId": "REF-abc123-def456"
  }
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Description                          |
|------|--------------------------------------|
| 200  | Success                              |
| 400  | Bad Request - Invalid input          |
| 401  | Unauthorized - Authentication failed |
| 403  | Forbidden - Insufficient permissions |
| 404  | Not Found - Resource doesn't exist   |
| 500  | Internal Server Error                |

### Error Reference IDs

All errors include a `referenceId` for support troubleshooting:
```json
{
  "data": {
    "referenceId": "REF-abc123-def456"
  }
}
```

---

## API Endpoints

---

## Authentication Endpoints

### Sign In with Google Token

Authenticate using a Google ID token.

```
POST /auth/sign-in/google
```

**Query Parameters:**

| Parameter    | Type   | Required | Description       |
|--------------|--------|----------|-------------------|
| `deviceType` | string | Yes      | `web` or `mobile` |

**Request Body:**

```json
{
  "token": "google_id_token",
  "clientId": "your_google_client_id",
  "defaultCurrencyCode": "INR",
  "defaultLanguageCode": "en"
}
```

**Response:**

```json
{
  "message": "You are successfully logged in.",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "isNewUser": false,
    "userId": "google_user_id",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://...",
    "accountCreated": "2025-01-01T00:00:00Z",
    "lastLoggedIn": "2025-01-31T12:00:00Z",
    "jwt": "eyJhbGciOiJIUzI1NiIs...",
    "defaultCurrencyCode": "INR",
    "defaultCurrencySymbol": "₹",
    "defaultLanguageCode": "en"
  }
}
```

**Notes:**
- For `web` device type, JWT is set as HttpOnly cookie
- For `mobile` device type, JWT is returned in response body

---

### Sign In with Google Auth Code

Authenticate using a Google authorization code (OAuth flow).

```
POST /auth/sign-in/google-code
```

**Query Parameters:**

| Parameter    | Type   | Required | Description       |
|--------------|--------|----------|-------------------|
| `deviceType` | string | Yes      | `web` or `mobile` |

**Request Body:**

```json
{
  "code": "google_auth_code",
  "clientId": "your_google_client_id",
  "defaultCurrencyCode": "INR",
  "defaultLanguageCode": "en"
}
```

**Response:** Same as [Sign In with Google Token](#sign-in-with-google-token)

---

### Get Current User

Retrieve the authenticated user's profile.

```
GET /auth/me
```

**Query Parameters:**

| Parameter    | Type   | Required | Description       |
|--------------|--------|----------|-------------------|
| `deviceType` | string | No       | `web` or `mobile` |

**Response:**

```json
{
  "message": "Data fetched successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "isNewUser": false,
    "userId": "google_user_id",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://...",
    "accountCreated": "2025-01-01T00:00:00Z",
    "lastLoggedIn": "2025-01-31T12:00:00Z",
    "defaultCurrencyCode": "INR",
    "defaultCurrencySymbol": "₹",
    "defaultLanguageCode": "en"
  }
}
```

---

### Sign Out

Sign out the current user.

```
POST /auth/sign-out
```

**Query Parameters:**

| Parameter    | Type   | Required | Description       |
|--------------|--------|----------|-------------------|
| `deviceType` | string | Yes      | `web` or `mobile` |

**Response:**

```json
{
  "message": "You have been logged out successfully.",
  "success": true,
  "timestamp": "2025-01-31 12:00:00"
}
```

---

## Account Endpoints

### List Accounts

Get all accounts for the authenticated user.

```
GET /accounts
```

**Response:**

```json
{
  "message": "Accounts fetched successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": [
    {
      "id": 1,
      "name": "Cash",
      "description": "Default cash account",
      "balance": "5000.0000",
      "accountGroupId": 1,
      "icon": "cash_icon",
      "currencyCode": "INR",
      "isActive": true,
      "isSystemDefault": true
    },
    {
      "id": 2,
      "name": "HDFC Credit Card",
      "balance": "-15000.0000",
      "accountGroupId": 3,
      "currencyCode": "INR",
      "isActive": true,
      "isSystemDefault": false,
      "creditLimit": "100000.0000",
      "availableCredit": "85000.0000",
      "billingDate": 5,
      "dueDate": 25,
      "autoPayEnabled": true,
      "autoPayFromAccountId": 3
    }
  ]
}
```

---

### Create Account

Create a new account under an account group.

```
POST /accounts/{accountGroupId}
```

**Path Parameters:**

| Parameter        | Type | Description             |
|------------------|------|-------------------------|
| `accountGroupId` | Long | ID of the account group |

**Request Body:**

```json
{
  "name": "Savings Account",
  "description": "Primary savings account",
  "icon": "bank_icon",
  "balance": 50000.00,
  "currencyCode": "INR",
  "isActive": true
}
```

**Validation Rules:**

| Field          | Rules                                |
|----------------|--------------------------------------|
| `name`         | Required, 1-255 characters           |
| `description`  | Max 255 characters                   |
| `icon`         | Max 255 characters                   |
| `balance`      | Max 15 integral, 4 fractional digits |
| `currencyCode` | Exactly 3 characters (ISO code)      |

**Response:**

```json
{
  "message": "Account created successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "id": 5,
    "name": "Savings Account",
    "description": "Primary savings account",
    "balance": "50000.0000",
    "accountGroupId": 2,
    "currencyCode": "INR",
    "isActive": true,
    "isSystemDefault": false
  }
}
```

---

### Update Account

Update an existing account.

```
PUT /accounts/{accountId}
```

**Path Parameters:**

| Parameter   | Type | Description       |
|-------------|------|-------------------|
| `accountId` | Long | ID of the account |

**Request Body:**

```json
{
  "name": "Updated Account Name",
  "balance": 75000.00
}
```

**Note:** At least one field must be provided for update.

---

### Delete Account

Delete an account.

```
DELETE /accounts/{accountId}
```

**Response:**

```json
{
  "message": "Deleted account",
  "success": true,
  "timestamp": "2025-01-31 12:00:00"
}
```

---

### Create Credit Card Account

Create a credit card account with credit card specific details.

```
POST /accounts/credit-card
```

**Request Body:**

```json
{
  "name": "HDFC Credit Card",
  "description": "Primary credit card",
  "icon": "credit_card_icon",
  "currencyCode": "INR",
  "creditLimit": 100000.00,
  "availableCredit": 100000.00,
  "billingDate": 5,
  "dueDate": 25,
  "autoPayEnabled": true,
  "autoPayFromAccountId": 3
}
```

**Additional Fields:**

| Field                  | Type       | Description                 |
|------------------------|------------|-----------------------------|
| `creditLimit`          | BigDecimal | Credit limit amount         |
| `availableCredit`      | BigDecimal | Available credit            |
| `billingDate`          | Integer    | Billing date (1-31)         |
| `dueDate`              | Integer    | Due date (1-31)             |
| `autoPayEnabled`       | Boolean    | Enable auto-pay             |
| `autoPayFromAccountId` | Long       | Source account for auto-pay |

---

### Update Credit Card Account

```
PUT /accounts/credit-card/{accountId}
```

---

### Create Debit Card Account

```
POST /accounts/debit-card
```

**Request Body:**

```json
{
  "name": "HDFC Debit Card",
  "currencyCode": "INR",
  "linkedBankAccountId": 3
}
```

**Additional Fields:**

| Field                 | Type | Description                    |
|-----------------------|------|--------------------------------|
| `linkedBankAccountId` | Long | Required - Linked bank account |

---

### Update Debit Card Account

```
PUT /accounts/debit-card/{accountId}
```

---

## Account Group Endpoints

### List Account Groups

Get all account groups for the user.

```
GET /account-groups
```

**Response:**

```json
{
  "message": "Account groups fetched successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": [
    {
      "id": 1,
      "name": "Cash",
      "description": "Cash in hand",
      "icon": "cash_icon",
      "color": "#4CAF50",
      "isSystemDefault": true
    },
    {
      "id": 2,
      "name": "Bank Account",
      "description": "Bank savings/current account",
      "icon": "bank_icon",
      "color": "#2196F3",
      "isSystemDefault": true
    }
  ]
}
```

---

### Create Account Group

```
POST /account-groups
```

**Request Body:**

```json
{
  "name": "Investment",
  "description": "Investment accounts",
  "icon": "investment_icon",
  "color": "#9C27B0",
  "displayOrder": 7
}
```

**Validation Rules:**

| Field         | Rules                      |
|---------------|----------------------------|
| `name`        | Required, 1-255 characters |
| `description` | Max 255 characters         |
| `icon`        | Max 255 characters         |
| `color`       | Max 25 characters          |

---

### Update Account Group

```
PUT /account-groups/{accountGroupId}
```

---

### Delete Account Group

```
DELETE /account-groups/{accountGroupId}
```

---

## Transaction Endpoints

### List Transactions

Get paginated transactions with optional filters.

```
GET /transactions
```

**Query Parameters:**

| Parameter        | Type   | Required | Description                                     |
|------------------|--------|----------|-------------------------------------------------|
| `page`           | int    | No       | Page number (default: 0)                        |
| `size`           | int    | No       | Page size (default: 50)                         |
| `startDate`      | Long   | No       | Filter start date (epoch ms)                    |
| `endDate`        | Long   | No       | Filter end date (epoch ms)                      |
| `accountId`      | Long   | No       | Filter by account                               |
| `accountGroupId` | Long   | No       | Filter by account group                         |
| `categoryId`     | Long   | No       | Filter by category                              |
| `type`           | String | No       | Filter by type: `INCOME`, `EXPENSE`, `TRANSFER` |

**Response:**

```json
{
  "message": "Transactions fetched successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "EXPENSE",
        "totalAmount": "500.0000",
        "transactionDate": "1706659200000",
        "categoryId": 5,
        "categoryName": "Food & Dining",
        "description": "Dinner at restaurant",
        "currency": "INR",
        "fromAccountId": 1,
        "fromAccountName": "Cash"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 50
    },
    "totalElements": 150,
    "totalPages": 3
  }
}
```

---

### Get Transaction by ID

```
GET /transactions/{id}
```

---

### Create Transaction

```
POST /transactions
```

**Request Body (Expense):**

```json
{
  "type": "EXPENSE",
  "totalAmount": 500.00,
  "transactionDate": 1706659200000,
  "categoryId": 5,
  "fromAccountId": 1,
  "description": "Dinner at restaurant",
  "currency": "INR"
}
```

**Request Body (Income):**

```json
{
  "type": "INCOME",
  "totalAmount": 50000.00,
  "transactionDate": 1706659200000,
  "categoryId": 1,
  "toAccountId": 3,
  "description": "Monthly salary",
  "currency": "INR"
}
```

**Request Body (Transfer):**

```json
{
  "type": "TRANSFER",
  "totalAmount": 10000.00,
  "transactionDate": 1706659200000,
  "fromAccountId": 1,
  "toAccountId": 3,
  "description": "Transfer to savings",
  "currency": "INR"
}
```

**Validation Rules:**

| Field             | Rules                                           |
|-------------------|-------------------------------------------------|
| `type`            | Required: `INCOME`, `EXPENSE`, `TRANSFER`       |
| `totalAmount`     | Required, max 15 integral + 4 fractional digits |
| `transactionDate` | Required, epoch milliseconds                    |
| `categoryId`      | Required                                        |
| `description`     | Max 255 characters                              |
| `currency`        | Exactly 3 characters                            |

---

### Update Transaction

```
PUT /transactions/{id}
```

**Request Body:**

```json
{
  "totalAmount": 550.00,
  "description": "Updated description"
}
```

**Note:** At least one field must be provided.

---

### Delete Transaction

```
DELETE /transactions/{id}
```

---

## Category Endpoints

### List Categories

Get all categories for the user.

```
GET /categories
```

**Query Parameters:**

| Parameter | Type   | Required | Description                         |
|-----------|--------|----------|-------------------------------------|
| `type`    | String | No       | Filter by type: `INCOME`, `EXPENSE` |

**Response:**

```json
{
  "message": "Categories fetched successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": [
    {
      "id": 1,
      "name": "Salary",
      "icon": "salary_icon",
      "type": "INCOME",
      "isSubCategory": false,
      "parentCategoryId": null
    },
    {
      "id": 5,
      "name": "Food & Dining",
      "icon": "food_icon",
      "type": "EXPENSE",
      "isSubCategory": false,
      "parentCategoryId": null
    }
  ]
}
```

---

### Create User Category

```
POST /categories
```

**Request Body:**

```json
{
  "name": "Subscriptions",
  "icon": "subscription_icon",
  "type": "EXPENSE",
  "isSubCategory": false
}
```

---

### Create Subcategory

```json
{
  "name": "Netflix",
  "icon": "netflix_icon",
  "type": "EXPENSE",
  "isSubCategory": true,
  "parentCategoryId": 15
}
```

---

### Update Category

```
PUT /categories/{id}
```

---

### Delete Category

```
DELETE /categories/{id}
```

---

### Admin: Create Default Category

Requires `ADMIN` role.

```
POST /categories/default
```

---

### Admin: Update Default Category

Requires `ADMIN` role.

```
PUT /categories/default/{id}
```

---

### Admin: Delete Default Category

Requires `ADMIN` role.

```
DELETE /categories/default/{id}
```

---

## Currency Endpoints

### List Supported Currencies

```
GET /supported-currencies
```

**Response:**

```json
{
  "message": "Supported currencies retrieved successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": [
    {
      "id": 1,
      "code": "INR",
      "name": "Indian Rupee",
      "symbol": "₹"
    },
    {
      "id": 2,
      "code": "USD",
      "name": "US Dollar",
      "symbol": "$"
    }
  ]
}
```

---

### Get Currency by Code

```
GET /supported-currencies/{code}
```

---

### Admin: Create Currency

Requires `ADMIN` role.

```
POST /supported-currencies/
```

---

### Admin: Delete Currency

Requires `ADMIN` role.

```
DELETE /supported-currencies/{id}
```

---

## Language Endpoints

### List Supported Languages

```
GET /supported-languages
```

**Response:**

```json
{
  "message": "Supported languages retrieved successfully",
  "success": true,
  "timestamp": "2025-01-31 12:00:00",
  "data": [
    {
      "id": 1,
      "code": "en",
      "name": "English"
    }
  ]
}
```

---

### Get Language by Code

```
GET /supported-languages/{code}
```

---

### Admin: Create Language

Requires `ADMIN` role.

```
POST /supported-languages/
```

---

### Admin: Delete Language

Requires `ADMIN` role.

```
DELETE /supported-languages/{id}
```

---

## Health & Monitoring

### Health Check

```
GET /actuator/health
```

**Response:**

```json
{
  "status": "UP"
}
```

---

### Application Info

```
GET /actuator/info
```

---

### API Mappings

```
GET /actuator/mappings
```

Lists all available API endpoints.

---

## Rate Limiting

Currently, no rate limiting is implemented. Consider implementing rate limiting for production deployments.

---

## Pagination

For paginated endpoints:

**Request:**
```
GET /transactions?page=0&size=20
```

**Response:**
```json
{
  "data": {
    "content": [...],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {...}
    },
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false,
    "numberOfElements": 20
  }
}
```

---

## Changelog

### v0.0.1-SNAPSHOT
- Initial API release
- Google OAuth authentication
- Account and transaction management
- Category management
- Multi-currency support

---

*Last updated: January 31, 2025*
