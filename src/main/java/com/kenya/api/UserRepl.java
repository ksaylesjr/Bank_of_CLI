package com.kenya.api;

import com.kenya.domain.Account;
import com.kenya.service.AccountService;
import com.kenya.service.LoginResult;
import com.kenya.service.UserService;

import java.math.BigDecimal;
import java.util.Scanner;

public class UserRepl {

    //global variables
    private final UserService userService;
    private final AccountService accountService;   // new
    private final Session session = new Session();
    private final Scanner sc = new Scanner(System.in);

    UserRepl(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    public void run() {
        while (true) {
            System.out.println("> ");
            String input = sc.nextLine().trim();
            String command = input.toLowerCase();

            if (command.equals("exit")) {
                return;     //exit while loop, exit application
            }

            // error handling for users input command
            try {
                handle(command);    //helper method to structure logic
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // router to display menu dependent on session state
    public void handle(String command) {
        if (!session.isLoggedIn()) {
            handleLoggedOut(command);
        } else {
            handleLoggedIn(command);
        }
    }
    // only these commands work when nobody is logged in
    private void handleLoggedOut(String command) {
        switch (command) {
            case "help" -> getHelp();
            case "login" -> login();
            // case "register" -> userService.registerUser(readUser());
            default -> System.out.println("Please log in or register first. Type \"help\" for options.");
        }
    }

    // these commands require an active session
    private void handleLoggedIn(String command) {
        switch (command) {
            case "help" -> getHelp();
            case "balance" -> checkBalance();
            case "deposit" -> deposit();
            case "withdraw" -> withdraw();
            // case "transfer" -> transfer();
            // case "history" -> history();
            case "logout" -> session.logout();
            default -> System.out.println("Unknown command. Type \"help\" for options.");
        }
    }

    public void getHelp() {
        if (!session.isLoggedIn()) {
            System.out.println("If you are a member, please input \"login\"\n" +
                    "if you would like to register, please input \"register\"\n");
        } else {
            System.out.println("Commands: balance, deposit, withdraw, transfer, history, logout\n");
        }
    }

    private void login() {
        System.out.print("Account ID: ");
        int accountId = Integer.parseInt(sc.nextLine().trim());

        System.out.print("PIN: ");
        String pin = sc.nextLine().trim();

        LoginResult result = userService.login(accountId, pin);   // error handling, throws if invalid
        session.login(result.getUser(), result.getAccount());

        System.out.println("Welcome, " + result.getUser().getName() + "!");
    }

    private void checkBalance() {
        int accountId = session.getActiveAccount().getAccountId();   // the session knows which account, so we getAccountId by digging two levels deep
        Account account = accountService.getAccount(accountId);      // fetch its current state from the DB, session currently holds stale value from login
        System.out.println("Current balance: $" + account.getBalance());
    }

    private void deposit() {
        System.out.print("Deposit amount: ");
        BigDecimal amount = new BigDecimal(sc.nextLine().trim());

        int accountId = session.getActiveAccount().getAccountId();
        Account updated = accountService.deposit(accountId, amount);

        System.out.println("Deposited $" + amount + ". New balance: $" + updated.getBalance());
    }

    private void withdraw() {
        System.out.print("Amount to withdraw: ");
        BigDecimal amount = new BigDecimal(sc.nextLine().trim());

        int accountId = session.getActiveAccount().getAccountId();
        Account updated = accountService.withdraw(accountId, amount);

        System.out.println("Withdrew $" + amount + ". New balance: $" + updated.getBalance());
    }

}
