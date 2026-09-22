package com.kenya.api;

import com.kenya.domain.Account;
import com.kenya.domain.User;

public class Session {

    private User user;
    private Account activeAccount;

    // no fields set, nobody is logged in yet
    public Session() {
    }

    // the login state is dependent on if a user has been assigned to this session
    public boolean isLoggedIn() {
        return user != null;
    }

    // stores a session that has been validated, sets both the user and their active account
    public void login(User user, Account activeAccount) {
        this.user = user;
        this.activeAccount = activeAccount;
    }

    // reset, session drops references to instances
    public void logout() {
        this.user = null;
        this.activeAccount = null;
    }

    public User getUser() {
        return user;
    }

    public Account getActiveAccount() {
        return activeAccount;
    }
}