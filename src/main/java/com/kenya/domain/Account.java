package com.kenya.domain;

import java.math.BigDecimal;

public class Account {

    private int accountId;
    private int userId;          // FK → user, linking many accounts belong to one user
    private String accountType;  // CHECKING or SAVINGS
    private BigDecimal balance;     //BigDecimal correlates to NUMERIC for database

    public Account(int accountId, int userId, String accountType, BigDecimal balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountType = accountType;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("Account ID: %d | Type: %s | Balance: $%s",
                accountId, accountType, balance);
    }

    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
