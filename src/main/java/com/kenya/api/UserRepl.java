package com.kenya.api;

import com.kenya.service.LoginResult;
import com.kenya.service.UserService;

import java.util.Scanner;

public class UserRepl {

    //global variables
    private final Scanner sc = new Scanner(System.in);
    private final UserService userService;
    private final Session session = new Session();

    //constructor to initialize UserRepl objects
    UserRepl(UserService userService){
        this.userService = userService;
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
            // case "balance" -> checkBalance();
            // case "deposit" -> deposit();
            // case "withdraw" -> withdraw();
            // case "transfer" -> transfer();
            // case "history" -> history();
            // case "logout" -> session.logout();
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

}
