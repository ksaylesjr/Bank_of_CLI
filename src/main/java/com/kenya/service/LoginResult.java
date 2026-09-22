package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.domain.User;

// this class allows our login() to return an object containing both an Account and User
public class LoginResult {
    private final User user;
    private final Account account;

    public LoginResult(User user, Account account) {
        this.user = user;
        this.account = account;
    }

    public User getUser()       { return user; }
    public Account getAccount() { return account; }
}