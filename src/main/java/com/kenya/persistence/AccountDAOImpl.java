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
}
