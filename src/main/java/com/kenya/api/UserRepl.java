package com.kenya.api;

import com.kenya.domain.Account;
import com.kenya.domain.Transaction;
import com.kenya.service.AccountService;
import com.kenya.service.LoginResult;
import com.kenya.service.UserService;
import com.kenya.domain.User;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        printWelcome();
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().trim();
            String command = input.toLowerCase();

            if (command.equals("exit")) {
                return;     //exit while loop, exit application
            }

            // error handling for users input command
            try {
                handle(command);
            } catch (IllegalArgumentException e) {
                // user error — show them exactly what they did wrong
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalStateException e) {
                // system error — friendly message, don't leak technical details and display a scary message to the user
                System.out.println("Service temporarily unavailable. Please try again.");
            }
        }
    }

    private void printWelcome() {
        System.out.println("=================================");
        System.out.println("   Welcome to the Bank of CLI");
        System.out.println("=================================");
        getHelp();
        System.out.println();
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
            case "register" -> register();
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
            case "transfer" -> transfer();
            case "history" -> history();
            case "logout" -> logout();

            default -> System.out.println("Unknown command. Type \"help\" for options.");
        }
    }

    public void getHelp() {
        if (!session.isLoggedIn()) {
            System.out.print("If you are a member, please input \"login\"\n" +
                    "If you would like to register, please input \"register\"\n");
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
        getHelp();
    }

    private void logout() {
        session.logout();
        System.out.println("You have been logged out.");
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

    private void transfer() {
        int sourceId = session.getActiveAccount().getAccountId();   // from = the logged-in account

        System.out.print("Destination account ID: ");
        int destId = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Amount to transfer: ");
        BigDecimal amount = new BigDecimal(sc.nextLine().trim());

        accountService.transfer(sourceId, destId, amount);
        System.out.println("Transferred $" + amount + " to account " + destId + ".");
    }

    private void history() {
        int accountId = session.getActiveAccount().getAccountId();
        List<Transaction> transactions = accountService.getHistory(accountId);

        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("Transaction history:");
        for (Transaction t : transactions) {
            System.out.println("  " + t.getTransDate().format(fmt)
                    + "  " + t.getTransType()
                    + " $" + t.getTransAmount()
                    + "  (from: " + t.getSourceId() + ", to: " + t.getDestId() + ")");
        }
    }

    private User readUser() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();

        System.out.print("PIN: ");
        String pin = sc.nextLine().trim();

        System.out.print("Date of birth (YYYY-MM-DD): ");
        LocalDate dob = LocalDate.parse(sc.nextLine().trim());

        return new User(0, name, pin, dob);   // userId 0, the DB generates the real one to return
    }

    private void register() {
        User user = readUser();
        int accountId = userService.registerUser(user);
        System.out.println("Registered! Your account ID is " + accountId + ". Please log in.");
    }

}
