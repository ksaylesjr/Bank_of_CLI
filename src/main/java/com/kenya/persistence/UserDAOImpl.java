package com.kenya.persistence;

import com.kenya.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class UserDAOImpl implements UserDAO {

    private static final String INSERT_SQL =
            "INSERT INTO bank_user (name, pin, date_of_birth) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT user_id, name, pin, date_of_birth FROM bank_user WHERE user_id = ?";

    @Override
    public int registerUser(User user) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getPin());
            statement.setObject(3, user.getDob());
            statement.executeUpdate();

            // Postgres generated the user_id, read it back
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);   // the new user_id
                }
            }
            throw new IllegalStateException("Insert succeeded but no ID was returned");

        } catch (SQLException e) {
            throw new IllegalStateException("Could not register user", e);
        }
    }

    @Override
    public User getUserByUserId(int userId) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch user" + e);
        }
        return null;   // no row matched
    }

    // turns one database row into a User object
    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("name"),
                resultSet.getString("pin"),
                resultSet.getObject("date_of_birth", LocalDate.class));
    }
}