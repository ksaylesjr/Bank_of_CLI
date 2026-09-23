package com.kenya.persistence;

import com.kenya.domain.Account;
import java.math.BigDecimal;

public interface AccountDAO {
    int createAccount(Account account);              // returns generated account_id
    Account getAccountByAccountId(int accountId);    // login step 1, and every balance operation
    void updateBalance(int accountId, BigDecimal newBalance);
}