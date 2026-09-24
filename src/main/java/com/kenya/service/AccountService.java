package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account getAccount(int accountId);
    Account deposit(int accountId, BigDecimal amount);
    Account withdraw(int accountId, BigDecimal amount);
    void transfer(int sourceId, int destId, BigDecimal amount);
    List<Transaction> getHistory(int accountId);
}