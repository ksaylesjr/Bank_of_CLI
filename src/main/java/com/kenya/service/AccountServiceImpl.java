package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.persistence.AccountDAO;

public class AccountServiceImpl implements AccountService {
    private final AccountDAO accountDAO;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account getAccount(int accountId) {
        Account account = accountDAO.getAccountByAccountId(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account;
    }
}