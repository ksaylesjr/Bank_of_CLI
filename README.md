# Bank of CLI

A functional banking application that runs entirely in the terminal. Users can
register, log in, and manage money — every action backed by a persistent
PostgreSQL database. Built in Java following a strict layered architecture.

---

## Features

- **Secure access** — register and log in with an Account ID and PIN. PINs are
  hashed with BCrypt and never stored in plain text.
- **Balance** — check the current account balance, read live from the database.
- **Deposit** — add funds to an account.
- **Withdraw** — remove funds, with overdraft protection (a withdrawal beyond
  the available balance is rejected).
- **Transfer** — move money between two accounts as a single atomic transaction:
  if any part fails, the whole operation rolls back so money can never vanish.
- **Transaction history** — view a timestamped ledger of all activity on an
  account.
- **Logging** — application activity is written to a log file, using `INFO` for
  successful actions and `ERROR` for failures.

---

## Architecture

The application follows a three-layer architecture, with each layer talking only
to the one below it:

| Layer          | Package                  | Responsibility                                   |
|----------------|--------------------------|--------------------------------------------------|
| API Layer      | `com.kenya.api`          | Terminal input/output, menus, command routing    |
| Service Layer  | `com.kenya.service`      | Banking rules, validation, business decisions    |
| Repository Layer | `com.kenya.persistence` | All SQL / JDBC communication with the database   |
| Domain         | `com.kenya.domain`       | Plain data objects passed between the layers      |

This separation means the API never touches SQL and the database never sees the
terminal. Each data-access class is defined as an interface (`UserDAO`,
`AccountDAO`, `TransactionDAO`) with a separate implementation, which keeps the
layers decoupled and makes the service layer testable in isolation.

### Database schema

Three tables model the system — `bank_user`, `account`, and `transaction`.
A user has many accounts (one-to-many); each transaction references a source
account and a destination account, which is how deposits, withdrawals, and
transfers are all recorded in a single ledger.

![ERD](db/BankOfCLI_ERD.png)

---

## Tech Stack

- **Language:** Java 17
- **Build tool:** Maven
- **Database:** PostgreSQL (run locally via Docker)
- **Security:** BCrypt (jBCrypt) for PIN hashing
- **Logging:** SLF4J + Logback
- **Testing:** JUnit 5
- **Version control:** Git & GitHub

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- Docker (to run PostgreSQL)

### 1. Start a PostgreSQL database

This project was developed against a Postgres container. Start one and create
the database:

```bash
docker run -d --name bank-postgres -p 5432:5432 \
  -e POSTGRES_USER=your_user \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=bankofcli \
  postgres:17
```

### 2. Create the tables

Load the schema into the database:

```bash
docker exec -i bank-postgres psql -U your_user -d bankofcli < db/schema.sql
```

### 3. Configure database credentials

Database credentials are read from `src/main/resources/db.properties`, which is
not committed to the repository. Create it based on the example file:

```
DB_URL=jdbc:postgresql://localhost:5432/bankofcli
DB_USER=your_user
DB_PASSWORD=your_password
```

### 4. Build and run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.kenya.api.Main"
```

Or run `Main` directly from your IDE.

---

## Usage

When the application starts, register a new user, then log in with the Account ID
you receive:

```
register
  → Name, PIN, and date of birth
  → returns an Account ID
login
  → Account ID + PIN
balance
deposit
withdraw
transfer
history
logout
exit
```

---

## Testing

Run the test suite with:

```bash
mvn test
```

Service-layer logic is tested with JUnit 5 using fake data-access
implementations, so the business rules are validated in isolation without a
database. The suite includes a positive test (a valid withdrawal succeeds and
updates the balance) and a negative test (an overdraft is rejected and the
balance is left unchanged).

---

## Author

**Kenya Sayles Jr.**
GitHub: [@ksaylesjr](https://github.com/ksaylesjr)
