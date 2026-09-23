package com.kenya.api;

import com.kenya.persistence.*;
import com.kenya.service.*;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();
        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();

        UserService userService = new UserServiceImpl(userDAO, accountDAO);
        AccountService accountService = new AccountServiceImpl(accountDAO, transactionDAO);

        new UserRepl(userService, accountService).run();
    }
}