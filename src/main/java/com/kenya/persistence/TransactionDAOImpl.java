package com.kenya.persistence;

import com.kenya.domain.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class TransactionDAOImpl implements TransactionDAO {

    private static final String INSERT_SQL =
            "INSERT INTO transaction (trans_type, trans_amount, source_id, dest_id) " +
                    "VALUES (?, ?, ?, ?)";

    @Override
    public void recordTransaction(Transaction transaction) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, transaction.getTransType());
            statement.setBigDecimal(2, transaction.getTransAmount());

            // source_id may be null, a deposit has no source
            if (transaction.getSourceId() != null) {
                statement.setInt(3, transaction.getSourceId());
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            // dest_id may be null, a withdrawal has no destination
            if (transaction.getDestId() != null) {
                statement.setInt(4, transaction.getDestId());
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Could not record transaction", e);
        }
    }
}