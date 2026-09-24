package com.kenya.persistence;

import com.kenya.domain.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDAOImpl implements AccountDAO {

    private static final String INSERT_SQL =
            // we do not insert the account_id, SERIAL will increment and return this value from DB
            "INSERT INTO account (user_id, account_type, balance) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT account_id, user_id, account_type, balance FROM account WHERE account_id = ?";
    private static final String UPDATE_BALANCE_SQL =
            "UPDATE account SET balance = ? WHERE account_id = ?";
    private static final String INSERT_TRANSACTION_SQL =
            "INSERT INTO transaction (trans_type, trans_amount, source_id, dest_id) VALUES (?, ?, ?, ?)";

    @Override
    public int createAccount(Account account) {
        // try w/resources to auto-close connect and statement
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, account.getUserId());
            statement.setString(2, account.getAccountType());
            statement.setBigDecimal(3, account.getBalance());
            statement.executeUpdate();

            // we get the account_id from DB
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new IllegalStateException("Insert succeeded but no account ID was returned");

        } catch (SQLException e) {
            throw new IllegalStateException("Could not create account", e);
        }
    }

    @Override
    public Account getAccountByAccountId(int accountId) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setInt(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch account", e);
        }
        return null;    // no row matched, caller must handle account not found
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getInt("account_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("account_type"),
                resultSet.getBigDecimal("balance"));
    }

    @Override
    public void updateBalance(int accountId, BigDecimal newBalance) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BALANCE_SQL)) {

            statement.setBigDecimal(1, newBalance);   // fills the first ?, to set the newBalance
            statement.setInt(2, accountId);           // fills the second ?, WHERE account_id = ?
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Could not update balance", e);
        }
    }

    // this method applies atomicity for each transaction
    @Override
    public void transfer(int sourceId, int destId, BigDecimal amount,
                         BigDecimal newSourceBalance, BigDecimal newDestBalance) {
        Connection connection = null;   // not using try with resources because of atomicity, conn closes in catch block
        try {
            connection = ConnectionFactory.getConnectionFactory().getConnection();
            connection.setAutoCommit(false);   // transaction writes won't be permanent until commit()

            // first WRITE, debit the source
            try (PreparedStatement debit = connection.prepareStatement(UPDATE_BALANCE_SQL)) {
                debit.setBigDecimal(1, newSourceBalance);
                debit.setInt(2, sourceId);
                debit.executeUpdate();
            }


            // second WRITE, credit the destination
            try (PreparedStatement credit = connection.prepareStatement(UPDATE_BALANCE_SQL)) {
                credit.setBigDecimal(1, newDestBalance);
                credit.setInt(2, destId);
                credit.executeUpdate();
            }

            // third WRITE, record the audit trail, transfer has a source AND dest
            try (PreparedStatement record = connection.prepareStatement(INSERT_TRANSACTION_SQL)) {
                record.setString(1, "TRANSFER");
                record.setBigDecimal(2, amount);
                record.setInt(3, sourceId);
                record.setInt(4, destId);
                record.executeUpdate();
            }

            connection.commit();   // If all three succeed, make them permanent, together

        } catch (SQLException e) {
            // if at least one fails, undo everything as if none of it happened
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new IllegalStateException("Transfer failed and rollback also failed", rollbackEx);
                }
            }
            throw new IllegalStateException("Transfer failed and was rolled back", e);

        } finally {
            // restore normal mode and close, no matter what happened
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    // nothing more we can do, close the connection
                }
            }
        }
    }
}
