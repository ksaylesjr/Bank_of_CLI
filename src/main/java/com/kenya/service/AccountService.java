package com.kenya.service;

import com.kenya.domain.Account;

import java.math.BigDecimal;

public interface AccountService {
    Account getAccount(int accountId);
    Account deposit(int accountId, BigDecimal amount);
}