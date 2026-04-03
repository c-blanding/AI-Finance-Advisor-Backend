# Finance Advisor Application

A full-stack AI-powered personal finance platform built on a Spring Boot microservice architecture. The system integrates with real banking data via the Plaid API, categorizes and analyzes transactions using a dedicated AI microservice, and delivers actionable insights including overspending alerts, savings recommendations, and predictive spending forecasts.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Reference](#api-reference)
  - [Authentication](#authentication)
  - [User](#user)
  - [Transactions](#transactions)
  - [Plaid Banking Integration](#plaid-banking-integration)
  - [AI Spending Analysis](#ai-spending-analysis)
- [Data Models](#data-models)
  - [Entities](#entities)
  - [DTOs](#dtos)
- [Security Architecture](#security-architecture)
  - [JWT Authentication Flow](#jwt-authentication-flow)
  - [Filter Chain](#filter-chain)
  - [CORS Policy](#cors-policy)
- [Service Layer](#service-layer)
  - [AuthService](#authservice)
  - [UserService](#userservice)
  - [TransactionService](#transactionservice)
  - [PlaidService](#plaidservice)
  - [AIService](#aiservice)
  - [AnalysisService](#analysisservice)
- [AI Microservice Integration](#ai-microservice-integration)
  - [Communication Protocol](#communication-protocol)
  - [Analysis Pipeline](#analysis-pipeline)
  - [Response Transformation](#response-transformation)
- [Database Design](#database-design)
  - [Schema](#schema)
  - [Repository Layer](#repository-layer)
- [Plaid Integration Deep Dive](#plaid-integration-deep-dive)
  - [Link Flow](#link-flow)
  - [Incremental Transaction Sync](#incremental-transaction-sync)
- [Configuration Reference](#configuration-reference)

---

## Architecture Overview

The application is composed of two independently running services that communicate over HTTP:

```
┌───────────────────────────────────────────────────────────────┐
│                        Frontend (Port 3000)                   │
│                     React / Next.js Client                    │
└─────────────────────────────┬─────────────────────────────────┘
                              │ HTTP + JWT
                              ▼
┌───────────────────────────────────────────────────────────────┐
│               Spring Boot Backend (Port 8080)                 │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Auth Layer   │  │ REST API     │  │ Service Layer    │   │
│  │ (JWT Filter) │  │ Controllers  │  │ (Business Logic) │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐    │
│  │                    Data Access Layer                  │    │
│  │            Spring Data JPA + Repositories            │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────────┬───────────────────────────┬────────────────────┘
               │                           │
               ▼                           ▼
┌──────────────────────┐     ┌─────────────────────────────────┐
│  PostgreSQL (Neon)   │     │  Python AI Microservice (8000)  │
│  Cloud Database      │     │  Spending Analysis Engine       │
└──────────────────────┘     └─────────────────────────────────┘
               ▲
               │
┌──────────────────────┐
│   Plaid API (Bank    │
│   Data Integration)  │
└──────────────────────┘
```

### Key Design Decisions

- **Stateless backend**: No server-side sessions. Every request is authenticated via JWT, making the service horizontally scalable.
- **Microservice separation**: AI analysis is decoupled into its own Python service at port 8000. This isolates ML dependencies from the core Java backend and allows independent scaling/deployment.
- **Incremental Plaid sync**: Transaction imports use a cursor-based pagination system to avoid re-importing historical data on every sync.
- **Dual transaction sources**: Transactions can originate from Plaid (bank-imported) or manual user entry. Both coexist in the same table via a `source` discriminator column.

---

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.5 |
| Security | Spring Security + JJWT | 0.12.3 |
| ORM | Spring Data JPA / Hibernate | (Boot-managed) |
| Database | PostgreSQL (Neon cloud) | — |
| Banking API | Plaid Java SDK | 29.0.0 |
| Build Tool | Apache Maven | — |
| Code Generation | Lombok | — |
| AI Microservice | Python (separate service) | — |
| AI Provider | OpenAI API (via Python service) | — |

---

## Project Structure

```
FinanceAdvisorApplication/
├── pom.xml                                         # Maven build configuration
├── src/
│   └── main/
│       ├── java/com/financeadvisor/financeadvisorapplication/
│       │   ├── FinanceAdvisorApplication.java      # Spring Boot entry point
│       │   │
│       │   ├── config/
│       │   │   ├── AppConfig.java                  # RestTemplate bean
│       │   │   ├── CorsConfig.java                 # CORS policy (allow localhost:3000)
│       │   │   ├── PlaidConfig.java                # Plaid API client bean
│       │   │   └── SecurityConfig.java             # Security filter chain + BCrypt
│       │   │
│       │   ├── controller/
│       │   │   ├── AuthController.java             # /api/auth/** (public)
│       │   │   ├── UserController.java             # /api/user/**
│       │   │   ├── TransactionController.java      # /api/transactions/**
│       │   │   ├── PlaidController.java            # /api/plaid/**
│       │   │   └── AnalysisController.java         # /api/analyze/**
│       │   │
│       │   ├── dto/
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── AuthResponse.java
│       │   │   ├── UserResponse.java
│       │   │   ├── TransactionRequest.java
│       │   │   ├── TransactionDTO.java
│       │   │   ├── TransactionResponse.java
│       │   │   ├── TransactionEditRequest.java
│       │   │   ├── ExchangeTokenRequest.java
│       │   │   ├── PlaidAccountResponse.java
│       │   │   ├── AnalyzeRequest.java
│       │   │   ├── AnalyzeResponse.java
│       │   │   ├── OverspendingFlag.java
│       │   │   ├── SavingsRecommendation.java
│       │   │   └── SpendingPrediction.java
│       │   │
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── UserTransaction.java
│       │   │   └── AnalysisData.java
│       │   │
│       │   ├── enums/
│       │   │   └── TransactionSource.java          # PLAID | MANUAL
│       │   │
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── TransactionRepository.java
│       │   │   └── AnalysisRepository.java
│       │   │
│       │   ├── security/
│       │   │   ├── JwtUtil.java                    # Token generation + validation
│       │   │   └── JwtFilter.java                  # Per-request JWT extraction
│       │   │
│       │   └── service/
│       │       ├── AuthService.java
│       │       ├── UserService.java
│       │       ├── TransactionService.java
│       │       ├── PlaidService.java
│       │       ├── AIService.java
│       │       └── AnalysisService.java
│       │
│       └── resources/
│           └── application.properties              # All runtime configuration
│
└── AiFinanceAdvisor/                               # Python AI microservice (separate)
```

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL** database (or a [Neon](https://neon.tech) cloud database)
- **Plaid developer account** — [Sign up at plaid.com](https://plaid.com) to obtain sandbox credentials
- **Python AI microservice** running on port 8000 (see `AiFinanceAdvisor/`)
- **OpenAI API key** (consumed by the Python service)

### Configuration

All configuration lives in `src/main/resources/application.properties`. Before running, set the following values:

```properties
# --- Database ---
spring.datasource.url=jdbc:postgresql://<host>/<database>?sslmode=require
spring.datasource.username=<db_user>
spring.datasource.password=<db_password>

# --- Hibernate ---
spring.jpa.hibernate.ddl-auto=update       # Auto-creates/updates schema
spring.jpa.show-sql=true                   # Logs all SQL (disable in production)

# --- JWT ---
jwt.secret=<base64-encoded-secret>         # At least 256-bit key
jwt.expiration=86400000                    # 24 hours in milliseconds

# --- Plaid ---
plaid.client-id=<your_plaid_client_id>
plaid.secret=<your_plaid_sandbox_secret>
plaid.env=sandbox                          # sandbox | development | production

# --- AI Microservice ---
ai.service.url=http://127.0.0.1:8000
```

> **Security Note**: Do not commit real credentials to version control. Use environment variables or a secrets manager in production. The properties file shown in the repository uses sandbox/development values only.

### Running the Application

**1. Start the Python AI microservice** (must be running before the Java service attempts analysis):

```bash
cd AiFinanceAdvisor
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

**2. Build and run the Spring Boot application:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Reference

All protected endpoints require an `Authorization: Bearer <token>` header. Tokens are obtained from the `/api/auth/register` or `/api/auth/login` endpoints.

---

### Authentication

**Base path**: `/api/auth` — No authentication required.

#### Register

```
POST /api/auth/register
```

Creates a new user account, hashes the password with BCrypt, and returns a JWT token.

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error cases:**
- `400` — Email already registered

---

#### Login

```
POST /api/auth/login
```

Authenticates credentials and returns a JWT token valid for 24 hours.

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error cases:**
- `401` — Invalid credentials

---

### User

**Base path**: `/api/user` — Authentication required.

#### Get Current User

```
GET /api/user/me
```

Returns the profile of the authenticated user. The user ID is extracted from the JWT — no user ID parameter is needed.

**Response `200 OK`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": null,
  "email": "user@example.com"
}
```

---

### Transactions

**Base path**: `/api/transactions` — Authentication required.

#### Get All Transactions

```
GET /api/transactions
```

Returns all transactions for the authenticated user, sorted by date descending. Includes both Plaid-imported and manually entered transactions.

**Response `200 OK`:**
```json
[
  {
    "id": "a1b2c3d4-...",
    "amount": 42.50,
    "description": "WHOLE FOODS MARKET",
    "date": "2026-03-28",
    "category": "Groceries",
    "source": "PLAID"
  },
  {
    "id": "e5f6g7h8-...",
    "amount": 15.00,
    "description": "Coffee meeting",
    "date": "2026-03-27",
    "category": "Dining",
    "source": "MANUAL"
  }
]
```

---

#### Create Manual Transaction

```
POST /api/transactions
```

Creates a new manually entered transaction. The `source` is automatically set to `MANUAL`.

**Request body:**
```json
{
  "amount": 25.00,
  "description": "Gym membership",
  "category": "Health & Fitness",
  "date": "2026-03-29"
}
```

**Response `200 OK`:**
```json
{
  "id": "new-uuid-...",
  "amount": 25.00,
  "description": "Gym membership",
  "date": "2026-03-29",
  "category": "Health & Fitness",
  "source": "MANUAL"
}
```

---

#### Delete Transaction

```
DELETE /api/transactions/{transactionId}
```

Permanently deletes a transaction. The user ID is validated against the transaction so users cannot delete other users' records.

**Response `204 No Content`**

---

### Plaid Banking Integration

**Base path**: `/api/plaid` — Authentication required.

The Plaid integration follows the official [Plaid Link flow](https://plaid.com/docs/link/):

```
1. GET  /api/plaid/link      → Frontend receives link_token
2. [User completes Plaid Link UI in browser]
3. POST /api/plaid/exchange  → Backend exchanges public_token for access_token
4. POST /api/plaid/sync      → Backend imports transactions
5. GET  /api/plaid/accounts  → Frontend displays linked accounts
```

---

#### Get Link Token

```
GET /api/plaid/link
```

Initiates the Plaid Link flow by creating a `link_token`. The frontend passes this token to the Plaid Link SDK to render the bank connection UI.

**Response `200 OK`:** Plaid `LinkTokenCreateResponse` object containing `link_token`.

---

#### Exchange Public Token

```
POST /api/plaid/exchange
```

After the user completes the Plaid Link flow, the frontend receives a short-lived `public_token`. This endpoint exchanges it for a long-lived `access_token` which is stored on the user record for future syncs.

**Request body:**
```json
{
  "publicToken": "public-sandbox-abc123..."
}
```

**Response `200 OK`:** Plaid `ItemPublicTokenExchangeResponse` object.

---

#### Sync Transactions

```
POST /api/plaid/sync
```

Fetches new and modified transactions from Plaid since the last sync. Uses a **cursor** stored on the user record for incremental updates — only data that has changed since the previous sync is fetched. Handles Plaid's pagination automatically (`hasMore` loop).

Imported transactions are saved with `source = PLAID` and are deduplicated by Plaid's own transaction IDs.

**Response `200 OK`**

---

#### Get Linked Accounts

```
GET /api/plaid/accounts
```

Returns the bank accounts linked to the authenticated user's Plaid item.

**Response `200 OK`:**
```json
[
  {
    "id": "plaid-account-id",
    "name": "Plaid Checking",
    "type": "depository",
    "mask": "0000",
    "balance": 1234.56
  }
]
```

---

### AI Spending Analysis

**Base path**: `/api/analyze` — Authentication required.

#### Run Analysis

```
POST /api/analyze
```

Triggers the full AI analysis pipeline:

1. Fetches the current period's transactions (last 2 months)
2. Fetches historical transactions (last 3 months prior) for baseline comparison
3. Sends both datasets to the Python AI microservice
4. AI categorizes each transaction, detects overspending, generates recommendations, and predicts next-period spending
5. Updates each transaction's category in the database based on AI classification
6. Persists the full analysis result
7. Returns the analysis with transaction descriptions (not raw IDs)

**Response `200 OK`:**
```json
{
  "categories": {
    "Whole Foods Market": "Groceries",
    "Netflix": "Entertainment",
    "Shell Gas Station": "Transportation"
  },
  "overspending": [
    {
      "category": "Dining Out",
      "currentSpend": 420.00,
      "averageSpend": 210.00,
      "severity": "HIGH",
      "insight": "You are spending 2x your historical average on dining this month."
    }
  ],
  "recommendations": [
    {
      "category": "Subscriptions",
      "suggestion": "Review your streaming subscriptions — you have 4 active.",
      "potentialSaving": 35.00,
      "difficulty": "EASY",
      "timeToImpact": "IMMEDIATE"
    }
  ],
  "predictions": [
    {
      "category": "Groceries",
      "currentSpend": 380.00,
      "predictedAmount": 400.00,
      "historicalAverage": 350.00,
      "onTrack": false,
      "variance": 14.3,
      "alert": "Slightly above historical average, monitor closely."
    }
  ]
}
```

---

#### Get Latest Analysis

```
GET /api/analyze/latest
```

Retrieves the most recently saved analysis for the authenticated user without re-running the AI pipeline. Useful for displaying cached results on page load.

**Response `200 OK`:** Same structure as the `POST /api/analyze` response above.

---

## Data Models

### Entities

#### User

Stores core user identity and Plaid integration state.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | Primary key, auto-generated |
| `name` | String | Display name |
| `email` | String | Unique, used for login |
| `passwordHash` | String | BCrypt-hashed password |
| `createdAt` | LocalDateTime | Auto-set on creation |
| `plaidAccessToken` | String | Long-lived Plaid access token |
| `plaidCursor` | String | Cursor for incremental Plaid sync |

---

#### UserTransaction

Stores all financial transactions regardless of source.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | Primary key, auto-generated |
| `userId` | UUID | Foreign key to User |
| `amount` | double | Transaction amount |
| `description` | String | Merchant name or user description |
| `category` | String | Assigned by AI or user |
| `source` | Enum | `PLAID` or `MANUAL` |
| `date` | LocalDate | Transaction date |
| `createdAt` | LocalDateTime | Auto-set on record creation |

---

#### AnalysisData

Persists AI analysis results as serialized JSON for historical retrieval.

| Column | Type | Notes |
|--------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `userId` | UUID | Foreign key to User |
| `categories` | TEXT | JSON: Map<transactionDescription, category> |
| `overspending` | TEXT | JSON: List<OverspendingFlag> |
| `recommendations` | TEXT | JSON: List<SavingsRecommendation> |
| `predictions` | TEXT | JSON: List<SpendingPrediction> |
| `createdAt` | LocalDateTime | Auto-set on creation |

---

### DTOs

#### Auth DTOs

```java
// Request
LoginRequest    { email, password }
RegisterRequest { email, password }

// Response
AuthResponse    { token, userId }
```

#### Transaction DTOs

```java
// Inbound (create)
TransactionRequest  { amount, description, category, date }

// Outbound (read)
TransactionResponse { id, amount, description, date, category, source }
TransactionDTO      { id, amount, description, date, category, source }

// Inbound (edit)
TransactionEditRequest { transactionId, amount, description, category, date }
```

#### Plaid DTOs

```java
ExchangeTokenRequest  { publicToken }
PlaidAccountResponse  { id, name, type, mask, balance }
```

#### Analysis DTOs

```java
// Outbound to AI service
AnalyzeRequest { userId, currentTransactions: List<TransactionDTO>, historicalTransactions: List<TransactionDTO> }

// Inbound from AI service / returned to frontend
AnalyzeResponse {
    categories:      Map<String, String>          // transactionId → category
    overspending:    List<OverspendingFlag>
    recommendations: List<SavingsRecommendation>
    predictions:     List<SpendingPrediction>
}

OverspendingFlag {
    category, currentSpend, averageSpend, severity, insight
}

SavingsRecommendation {
    category, suggestion, potentialSaving, difficulty, timeToImpact
}

SpendingPrediction {
    category, currentSpend, predictedAmount, historicalAverage,
    onTrack, variance, alert
}
```

---

## Security Architecture

### JWT Authentication Flow

```
Client                          Backend
  │                                │
  │  POST /api/auth/login          │
  │  { email, password }           │
  │ ─────────────────────────────► │
  │                                │  1. Load user by email
  │                                │  2. BCrypt.matches(password, hash)
  │                                │  3. Generate JWT (HMAC-SHA, 24h TTL)
  │  { token, userId }             │
  │ ◄───────────────────────────── │
  │                                │
  │  GET /api/transactions         │
  │  Authorization: Bearer <token> │
  │ ─────────────────────────────► │
  │                                │  4. JwtFilter extracts token
  │                                │  5. Validate signature + expiry
  │                                │  6. Set SecurityContext + userId attr
  │                                │  7. Controller reads userId attr
  │  [ transactions ]              │
  │ ◄───────────────────────────── │
```

**Token contents:**
- **Subject**: User UUID (string form)
- **Issued at**: Current timestamp
- **Expiration**: 24 hours from issuance
- **Algorithm**: HMAC-SHA (key derived from Base64-decoded secret)

### Filter Chain

The `JwtFilter` extends `OncePerRequestFilter` and is inserted **before** `UsernamePasswordAuthenticationFilter` in the Spring Security chain:

```
Incoming Request
       │
       ▼
┌─────────────────┐
│   JwtFilter     │  Extracts "Bearer " token from Authorization header
│                 │  → Validates token (signature + expiry)
│                 │  → Extracts userId claim
│                 │  → Sets UsernamePasswordAuthenticationToken in SecurityContext
│                 │  → Stores userId as request attribute "userId"
└────────┬────────┘
         │ (passes through even if no/invalid token — auth enforcement is declarative)
         ▼
┌─────────────────┐
│ SecurityConfig  │  /api/auth/** → permitAll()
│                 │  All other    → authenticated()
└────────┬────────┘
         │
         ▼
    Controller
    (reads userId via @RequestAttribute("userId"))
```

### CORS Policy

Configured in `CorsConfig.java`:

| Setting | Value |
|---------|-------|
| Allowed origins | `http://localhost:3000` |
| Allowed methods | All (`*`) |
| Allowed headers | All (`*`) |
| Allow credentials | `true` |

> Production deployments must update this to the actual frontend domain.

---

## Service Layer

### AuthService

Implements `UserDetailsService` for Spring Security integration.

**`register(RegisterRequest)`**
1. Checks that the email is not already registered
2. Hashes the password with BCryptPasswordEncoder
3. Persists the new User entity
4. Generates and returns a JWT token + userId

**`login(LoginRequest)`**
1. Looks up user by email
2. Compares submitted password against stored BCrypt hash
3. Generates and returns a JWT token + userId

---

### UserService

Thin service for reading user data.

- `getUserById(UUID)` — Fetch user entity by primary key
- `getUserCursor(UUID)` — Read the `plaidCursor` field for use in incremental sync

---

### TransactionService

Manages all CRUD operations on `UserTransaction` records.

- `getUserTransactions(UUID)` — All transactions, sorted newest-first
- `getTransactionsByDateRange(LocalDate, LocalDate, UUID)` — Date range filter
- `getTransactionsByDate(LocalDate, UUID)` — Exact date filter
- `getUserTransactionsByUserIdAndSource(UUID, TransactionSource)` — Source filter (PLAID/MANUAL)
- `createTransaction(UUID, TransactionRequest)` — Create manual transaction with `source=MANUAL`
- `deleteTransactions(UUID, UUID)` — Delete by transaction ID, scoped to user

---

### PlaidService

Handles the complete Plaid banking integration lifecycle.

**`createLinkToken(UUID)`**
- Calls `PlaidApi.linkTokenCreate()` with the user's UUID as client user ID
- Returns the Plaid `link_token` for the frontend SDK

**`exchangePublicToken(UUID, String)`**
- Calls `PlaidApi.itemPublicTokenExchange()` with the short-lived public token
- Stores the returned `access_token` on the User entity
- Persists the updated user record

**`syncTransactions(UUID)`**
- Retrieves the user's stored `access_token` and `plaidCursor`
- Calls `PlaidApi.transactionsSync()` in a pagination loop until `hasMore = false`
- Maps each Plaid `Transaction` to a `UserTransaction` entity via `mapPlaidToUserTransaction()`
- Saves all new transactions to the database
- Updates `plaidCursor` on the user record with the new cursor returned by Plaid

**`getAccounts(UUID)`**
- Calls `PlaidApi.accountsGet()` with the user's access token
- Maps Plaid account objects to `PlaidAccountResponse` DTOs with name, type, mask, and current balance

**Transaction mapping (`mapPlaidToUserTransaction`)**
- Uses Plaid's first category entry as the initial category string
- Preserves Plaid's `transactionId` as the `UserTransaction` UUID
- Sets `source = PLAID`

---

### AIService

Orchestrates the full spending analysis pipeline.

**`analyzeSpending(UUID)`**

```
1. Fetch current transactions   → last 2 months via TransactionService
2. Fetch historical transactions → 3 months prior to the current window
3. Convert both sets to TransactionDTO lists
4. Build AnalyzeRequest { userId, currentTransactions, historicalTransactions }
5. POST to http://127.0.0.1:8000/api/analyze via RestTemplate
6. Deserialize AnalyzeResponse
7. For each transaction in the categories map:
      - Update that transaction's category in the database
8. Transform response: replace transaction UUIDs with merchant descriptions
9. Save analysis to AnalysisData table via AnalysisService
10. Return transformed AnalyzeResponse
```

**`changeResponseCategoryToName(AnalyzeResponse)`**

Since the AI service returns a map keyed by transaction ID, this method replaces each ID with the human-readable transaction description before returning the response to the frontend.

---

### AnalysisService

Handles persistence and retrieval of analysis results.

**`saveAnalysis(UUID, AnalyzeResponse)`**
- Serializes the four analysis components (categories, overspending, recommendations, predictions) to JSON strings using Jackson `ObjectMapper`
- Creates or updates an `AnalysisData` record for the user

**`getLatestAnalysis(UUID)`**
- Retrieves the most recent `AnalysisData` row by `createdAt DESC`
- Deserializes each JSON field back into typed Java objects

**`getAllAnalyses(UUID)`**
- Returns all historical analysis records for the user
- Deserializes each with individual error handling to tolerate partially corrupt records

---

## AI Microservice Integration

### Communication Protocol

The Spring Boot backend communicates with the Python AI service over plain HTTP using Spring's `RestTemplate`.

**Request:**
```
POST http://127.0.0.1:8000/api/analyze
Content-Type: application/json

{
  "userId": "550e8400-...",
  "currentTransactions": [
    { "id": "txn-uuid", "amount": 42.50, "description": "Whole Foods", "date": "2026-03-28", "category": "Groceries", "source": "PLAID" }
  ],
  "historicalTransactions": [ ... ]
}
```

**Response:**
```json
{
  "categories":      { "txn-uuid": "Groceries", ... },
  "overspending":    [ { "category": "Dining Out", "currentSpend": 420, "averageSpend": 210, "severity": "HIGH", "insight": "..." } ],
  "recommendations": [ { "category": "Subscriptions", "suggestion": "...", "potentialSaving": 35, "difficulty": "EASY", "timeToImpact": "IMMEDIATE" } ],
  "predictions":     [ { "category": "Groceries", "currentSpend": 380, "predictedAmount": 400, "historicalAverage": 350, "onTrack": false, "variance": 14.3, "alert": "..." } ]
}
```

### Analysis Pipeline

```
Spring Boot                          Python AI Service
     │                                      │
     │  POST /api/analyze                   │
     │  { currentTxns, historicalTxns }     │
     │ ────────────────────────────────────►│
     │                                      │  1. Classify each transaction
     │                                      │     category via OpenAI
     │                                      │  2. Compare current vs historical
     │                                      │     spend per category
     │                                      │  3. Flag overspending (severity scoring)
     │                                      │  4. Generate savings recommendations
     │                                      │  5. Predict next-period spend
     │  { categories, overspending,         │
     │    recommendations, predictions }    │
     │ ◄────────────────────────────────────│
     │                                      │
     │  Update transaction categories in DB │
     │  Save AnalysisData record            │
     │  Return response to frontend         │
```

### Response Transformation

The AI service keys its `categories` map by transaction UUID. Before returning to the frontend, `AIService.changeResponseCategoryToName()` looks up each UUID in the database and replaces it with the transaction's description string, producing a more readable response like:

```json
{
  "categories": {
    "Whole Foods Market": "Groceries",
    "Netflix": "Entertainment"
  }
}
```

instead of:

```json
{
  "categories": {
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890": "Groceries",
    "b2c3d4e5-f6a7-8901-bcde-f01234567891": "Entertainment"
  }
}
```

---

## Database Design

### Schema

The application uses Hibernate's `ddl-auto=update` to manage schema evolution automatically. The three core tables are:

```sql
-- Users table
CREATE TABLE users (
    id                UUID PRIMARY KEY,
    name              VARCHAR,
    email             VARCHAR UNIQUE NOT NULL,
    password_hash     VARCHAR NOT NULL,
    created_at        TIMESTAMP,
    plaid_access_token VARCHAR,
    plaid_cursor      VARCHAR
);

-- Transactions table
CREATE TABLE user_transaction (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id),
    amount      DOUBLE PRECISION,
    description VARCHAR,
    category    VARCHAR,
    source      VARCHAR,        -- 'PLAID' or 'MANUAL'
    date        DATE,
    created_at  TIMESTAMP
);

-- Analysis results table
CREATE TABLE analysis_data (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    categories      TEXT,       -- JSON
    overspending    TEXT,       -- JSON
    recommendations TEXT,       -- JSON
    predictions     TEXT,       -- JSON
    created_at      TIMESTAMP
);
```

### Repository Layer

Spring Data JPA repositories provide all data access. Custom query methods use Spring's method naming convention:

**`TransactionRepository`** — most query-rich repository:

```java
// All transactions for a user
List<UserTransaction> findAllByUserIdOrderByDateDesc(UUID userId);

// Date range (used for current vs historical period separation)
List<UserTransaction> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end);

// Source filter (PLAID/MANUAL)
List<UserTransaction> findByUserIdAndSource(UUID userId, TransactionSource source);

// Ownership-scoped single record (prevents cross-user data access)
Optional<UserTransaction> findByUserIdAndId(UUID userId, UUID transactionId);
```

**`AnalysisRepository`:**

```java
// Most recent analysis (used by GET /api/analyze/latest)
Optional<AnalysisData> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

// All analyses for historical trend viewing
List<AnalysisData> findAllByUserId(UUID userId);

// Cleanup
void deleteByUserId(UUID userId);
```

---

## Plaid Integration Deep Dive

### Link Flow

Plaid's security model requires a multi-step token exchange to prevent client-side exposure of the bank access token:

```
Step 1: Backend creates link_token (server-to-Plaid API call)
        → link_token is short-lived (~30 min), safe to send to frontend

Step 2: Frontend initializes Plaid Link SDK with link_token
        → User selects their bank, enters credentials in Plaid's hosted UI
        → Plaid returns a short-lived public_token to the frontend

Step 3: Frontend sends public_token to POST /api/plaid/exchange
        → Backend exchanges it for a long-lived access_token (server-to-Plaid)
        → access_token is stored in the users table, NEVER sent to the frontend

Step 4: All subsequent bank data fetches use the stored access_token
```

### Incremental Transaction Sync

Plaid's Transactions Sync API uses a cursor-based system that enables efficient incremental updates:

```
First sync (cursor = null):
  → Plaid returns ALL historical transactions + new cursor
  → Backend saves all transactions
  → Backend stores cursor on user record

Subsequent syncs (cursor = stored value):
  → Plaid returns ONLY transactions added/modified since last cursor
  → Backend saves only new/changed transactions
  → Backend updates stored cursor

If hasMore = true:
  → Loop continues fetching pages until hasMore = false
```

This design means that even users with years of bank history only pay the cost of a full import once.

---

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | — | JDBC connection URL |
| `spring.datasource.username` | — | Database username |
| `spring.datasource.password` | — | Database password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema strategy (`update`/`validate`/`create`) |
| `spring.jpa.show-sql` | `true` | Log SQL to stdout (disable in production) |
| `jwt.secret` | — | Base64-encoded HMAC signing key (min 256 bits) |
| `jwt.expiration` | `86400000` | Token TTL in milliseconds (default: 24 hours) |
| `plaid.client-id` | — | Plaid developer client ID |
| `plaid.secret` | — | Plaid environment secret |
| `plaid.env` | `sandbox` | Plaid environment: `sandbox`, `development`, `production` |
| `ai.service.url` | `http://127.0.0.1:8000` | Base URL of the Python AI microservice |
