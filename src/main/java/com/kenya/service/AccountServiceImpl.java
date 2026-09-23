package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.domain.Transaction;
import com.kenya.persistence.AccountDAO;
import com.kenya.persistence.TransactionDAO;

import java.math.BigDecimal;

public class AccountServiceImpl implements AccountService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Account getAccount(int accountId) {
        Account account = accountDAO.getAccountByAccountId(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account;
    }

    @Override
    public Account deposit(int accountId, BigDecimal amount) {
        // verify the deposit amount is valid
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // fetch the current state, error handling is handled in getAccount
        Account account = getAccount(accountId);

        // compute the new balance
        BigDecimal newBalance = account.getBalance().add(amount);

        // first WRITE, update the balance
        accountDAO.updateBalance(accountId, newBalance);

        // second WRITE, record the transaction for ledger, deposit: money comes from outside source, so source is null
        Transaction record = new Transaction(0, "DEPOSIT", amount, null, accountId);
        transactionDAO.recordTransaction(record);

        // return the updated account so the REPL can show the new balance
        return getAccount(accountId);
    }
}