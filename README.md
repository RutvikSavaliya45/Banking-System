<<<<<<< HEAD
# Banking System Backend

Spring Boot backend for a banking system with JWT authentication, authorization, account management, fund transfers, transaction history, and loan management.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Maven

## Features

- User registration and login
- JWT-protected APIs
- Role-based authorization for admin loan decisions
- Account creation, listing, details, and closure
- Deposits and withdrawals
- Fund transfers between accounts
- Transaction history per account
- Loan applications and admin approval/rejection
- Admin view of every bank account, including user-owned and admin-owned accounts
- Admin account freeze/unfreeze controls
- Admin account inspection with balance, transaction history, and running loans

## Run PostgreSQL

```bash
docker compose up -d
```

The default database config is in `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banking_db
spring.datasource.username=postgres
spring.datasource.password=0000
```

## Run the App

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

## Run the React Frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

The frontend points to the backend through `VITE_API_BASE_URL`. Copy `frontend/.env.example` to `frontend/.env` if you need to change the API URL.

## Default Admin

The app seeds one admin user on startup:

```text
email: admin@bank.local
password: admin123
```

Use this account for admin-only loan endpoints.

## API Examples

Health check in browser:

```http
GET /
GET /api/health
```

Register:

```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Asha Sharma",
  "email": "asha@example.com",
  "password": "secret123"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "asha@example.com",
  "password": "secret123"
}
```

Use the returned token on protected requests:

```http
Authorization: Bearer <token>
```

Create account:

```http
POST /api/accounts
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "SAVINGS"
}
```

Deposit:

```http
POST /api/accounts/1/deposit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 5000,
  "description": "Initial deposit"
}
```

Withdraw:

```http
POST /api/accounts/1/withdraw
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 1000,
  "description": "ATM withdrawal"
}
```

Transfer:

```http
POST /api/transfers
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountNumber": "BA1234567890",
  "amount": 750,
  "description": "Rent payment"
}
```

Transaction history:

```http
GET /api/accounts/1/transactions
Authorization: Bearer <token>
```

Apply for loan:

```http
POST /api/loans
Authorization: Bearer <token>
Content-Type: application/json

{
  "principal": 100000,
  "annualInterestRate": 9.5,
  "termMonths": 24
}
```

Admin approve or reject loan:

```http
PATCH /api/loans/1/decision
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "status": "APPROVED"
}
```

## Main Endpoints

| Method | Endpoint | Access |
| --- | --- | --- |
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/accounts` | User |
| GET | `/api/accounts` | User |
| GET | `/api/accounts/{accountId}` | Account owner |
| DELETE | `/api/accounts/{accountId}` | Account owner |
| POST | `/api/accounts/{accountId}/deposit` | Account owner |
| POST | `/api/accounts/{accountId}/withdraw` | Account owner |
| POST | `/api/transfers` | Account owner |
| GET | `/api/accounts/{accountId}/transactions` | Account owner |
| POST | `/api/loans` | User |
| GET | `/api/loans/mine` | User |
| GET | `/api/loans` | Admin |
| PATCH | `/api/loans/{loanId}/decision` | Admin |
| GET | `/api/admin/accounts` | Admin |
| GET | `/api/admin/accounts/{accountId}` | Admin |
| PATCH | `/api/admin/accounts/{accountId}/freeze` | Admin |
| PATCH | `/api/admin/accounts/{accountId}/unfreeze` | Admin |

## Admin Account Controls

Admins can see all accounts in the bank, regardless of whether the owner has `ROLE_USER` or `ROLE_ADMIN`.

```http
GET /api/admin/accounts
Authorization: Bearer <admin-token>
```

Clicking an account in the React admin dashboard calls:

```http
GET /api/admin/accounts/{accountId}
Authorization: Bearer <admin-token>
```

That response includes the account balance, account owner, transaction history, and approved running loans for the account owner.

Freeze an account:

```http
PATCH /api/admin/accounts/{accountId}/freeze
Authorization: Bearer <admin-token>
```

Unfreeze an account:

```http
PATCH /api/admin/accounts/{accountId}/unfreeze
Authorization: Bearer <admin-token>
```


