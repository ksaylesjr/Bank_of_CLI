package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.persistence.AccountDAO;
import com.kenya.persistence.TransactionDAO;
import com.kenya.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceImplTest {

    // ---- a fake AccountDAO we fully control ----
    static class FakeAccountDAO implements AccountDAO {
        Account stored;                 // the one account this fake "holds"
        BigDecimal lastUpdatedBalance;  // records what updateBalance was called with

        public FakeAccountDAO(Account account) { this.stored = account; }

        @Override
        public Account getAccountByAccountId(int accountId) {
            return (stored != null && stored.getAccountId() == accountId) ? stored : null;
        }

        @Override
        public void updateBalance(int accountId, BigDecimal newBalance) {
            this.lastUpdatedBalance = newBalance;   // just record it, no database
        }

        @Override
        public int createAccount(Account account) { return 0; }   // unused here

        @Override
        public void transfer(int s, int d, BigDecimal a, BigDecimal ns, BigDecimal nd) { }  // unused
    }

    // ---- a fake TransactionDAO that does nothing ----
    static class FakeTransactionDAO implements TransactionDAO {
        @Override public void recordTransaction(Transaction t) { }        // no-op
        @Override public List<Transaction> getTransactionsForAccount(int id) { return List.of(); }
    }

    @Test
    void withdraw_succeeds_with_sufficient_funds() {
        // account 1 has $100
        Account account = new Account(1, 1, "CHECKING", new BigDecimal("100.00"));
        FakeAccountDAO fakeAccount = new FakeAccountDAO(account);
        AccountService service = new AccountServiceImpl(fakeAccount, new FakeTransactionDAO());

        service.withdraw(1, new BigDecimal("30.00"));

        // the service should have told the DAO to set the balance to 70
        assertEquals(new BigDecimal("70.00"), fakeAccount.lastUpdatedBalance);
    }

    @Test
    void withdraw_rejects_insufficient_funds() {
        // account 1 has only $20
        Account account = new Account(1, 1, "CHECKING", new BigDecimal("20.00"));
        FakeAccountDAO fakeAccount = new FakeAccountDAO(account);
        AccountService service = new AccountServiceImpl(fakeAccount, new FakeTransactionDAO());

        // withdrawing 50 should throw, and NOT update the balance
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw(1, new BigDecimal("50.00")));
        assertNull(fakeAccount.lastUpdatedBalance);   // proves no write happened
    }
}