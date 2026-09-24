package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.domain.Transaction;
import com.kenya.persistence.AccountDAO;
import com.kenya.persistence.TransactionDAO;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountServiceImpl implements AccountService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

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
            logger.error("Deposit rejected - non-positive amount {} on account {}", amount, accountId);
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // fetch the current state, error handling is handled in getAccount
        Account account = getAccount(accountId);

        // compute the new balance
        BigDecimal newBalance = account.getBalance().add(amount);

        // first WRITE, update the balance
        accountDAO.updateBalance(accountId, newBalance);

        // second WRITE, record the transaction for ledger, deposit: money comes from outside source, so source is null
        Transaction record = new Transaction(0, "DEPOSIT", amount, null, null, accountId);
        transactionDAO.recordTransaction(record);

        logger.info("Deposit of {} to account {}", amount, accountId);

        // return the updated account so the REPL can show the new balance
        return getAccount(accountId);
    }

    @Override
    public Account withdraw(int accountId, BigDecimal amount) {
        // reject non-positive amounts, DB also has constraint CHECK (balance >= 0)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Withdrawal rejected - non-positive amount {} on account {}", amount, accountId);
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        // fetch current state
        Account account = getAccount(accountId);

        // prevent overdraft, can't withdraw more than the available balance
        if (account.getBalance().compareTo(amount) < 0) {
            logger.error("Withdrawal rejected - insufficient funds on account {}", accountId);
            throw new IllegalArgumentException("Insufficient funds");
        }

        // compute the new balance
        BigDecimal newBalance = account.getBalance().subtract(amount);

        // first WRITE, change the money
        accountDAO.updateBalance(accountId, newBalance);

        // second WRITE, record the audit trail, withdraw: money leaves this account to outside, so dest is null
        Transaction record = new Transaction(0, "WITHDRAW", amount, null, accountId, null);
        transactionDAO.recordTransaction(record);

        logger.info("Withdrawal of {} from account {}", amount, accountId);
        // return the updated account
        return getAccount(accountId);
    }

    @Override
    public void transfer(int sourceId, int destId, BigDecimal amount) {
        // first rule, positive amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Transfer rejected - non-positive amount {} from account {}", amount, sourceId);
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // second rule, can't transfer to the same account
        if (sourceId == destId) {
            logger.error("Transfer rejected - same source and destination account {}", sourceId);
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // fetch both accounts, getAccount throws if either doesn't exist
        Account source = getAccount(sourceId);
        Account dest = getAccount(destId);

        // third rule, must have sufficient funds in the source
        if (source.getBalance().compareTo(amount) < 0) {
            logger.error("Transfer rejected - insufficient funds on account {}", sourceId);
            throw new IllegalArgumentException("Insufficient funds");
        }

        // compute both new balances
        BigDecimal newSourceBalance = source.getBalance().subtract(amount);
        BigDecimal newDestBalance = dest.getBalance().add(amount);

        // hand the pre-computed values to the DAO, which does all three writes atomically
        accountDAO.transfer(sourceId, destId, amount, newSourceBalance, newDestBalance);

        logger.info("Transfer of {} from account {} to account {}", amount, sourceId, destId);
    }

    @Override
    public List<Transaction> getHistory(int accountId) {
        return transactionDAO.getTransactionsForAccount(accountId);
    }
}