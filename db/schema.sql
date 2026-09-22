CREATE TABLE IF NOT EXISTS bank_user (
    user_id       SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    pin           VARCHAR(255) NOT NULL,
    date_of_birth DATE
);

CREATE TABLE IF NOT EXISTS account (
    account_id   SERIAL PRIMARY KEY,
    user_id      INTEGER NOT NULL REFERENCES bank_user(user_id),
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVINGS')),
    balance      NUMERIC(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS transaction (
    transaction_id SERIAL PRIMARY KEY,
    trans_type     VARCHAR(20) NOT NULL CHECK (trans_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),
    trans_amount   NUMERIC(15, 2) NOT NULL CHECK (trans_amount > 0),
    trans_date     TIMESTAMP NOT NULL DEFAULT now(),
    source_id      INTEGER REFERENCES account(account_id),
    dest_id        INTEGER REFERENCES account(account_id)
);
