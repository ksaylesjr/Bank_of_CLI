package com.kenya.service;

import com.kenya.domain.Account;
import com.kenya.domain.User;
import com.kenya.persistence.AccountDAO;
import com.kenya.persistence.UserDAO;
import org.mindrot.jbcrypt.BCrypt;      // hashing functionality for PIN
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    public UserServiceImpl(UserDAO userDAO, AccountDAO accountDAO) {
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public int registerUser(User user) {
        // hash the raw PIN before it ever touches the database
        String hashedPin = BCrypt.hashpw(user.getPin(), BCrypt.gensalt());  //turns raw pin into salted hash
        User userToStore = new User(0, user.getName(), hashedPin, user.getDob());

        int userId = userDAO.registerUser(userToStore);

        // create account
        Account account = new Account(0, userId, "CHECKING", BigDecimal.ZERO);
        int accountId = accountDAO.createAccount(account);

        logger.info("New user registered: user {} with account {}", userId, accountId);

        return accountId;   // the new account id from accountDAO.createAccount()
    }


    @Override
    public LoginResult login(int accountId, String pin) {
        // find the account they're logging into
        Account account = accountDAO.getAccountByAccountId(accountId);
        if (account == null) {
            logger.error("Login failed - account not found: {}", accountId);
            throw new IllegalArgumentException("Account not found");    // the user provided invalid input
        }

        // find the user who owns that account
        User user = userDAO.getUserByUserId(account.getUserId());
        if (user == null) {
            logger.error("Login failed - account {} has no owning user", accountId);
            throw new IllegalStateException("Account has no owning user");  // the account exists without user, invalid DB state
        }

        // verify the PIN
        if (!BCrypt.checkpw(pin, user.getPin())) {
            logger.error("Login failed - incorrect PIN for account {}", accountId);
// boolean check the raw pin matches the stored hashed pin
            throw new IllegalArgumentException("Incorrect PIN");
        }

        logger.info("User successfully logged in: account {}", accountId);
        // successful login
        return new LoginResult(user, account);   // ← was: return user;
    }
}