package com.kenya.persistence;

import com.kenya.domain.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionDAOImpl implements TransactionDAO {

    private static final String INSERT_SQL =
            "INSERT INTO transaction (trans_type, trans_amount, source_id, dest_id) " +
                    "VALUES (?, ?, ?, ?)";
    // OR allows to catch EVERY transaction
    private static final String FIND_BY_ACCOUNT_SQL =
            "SELECT transaction_id, trans_type, trans_amount, trans_date, source_id, dest_id " +
                    "FROM transaction WHERE source_id = ? OR dest_id = ? ORDER BY trans_date DESC";

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

    @Override
    public List<Transaction> getTransactionsForAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ACCOUNT_SQL)) {

            statement.setInt(1, accountId);   // fills source_id = ?
            statement.setInt(2, accountId);   // fills dest_id = ?

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch transaction history", e);
        }
        return transactions;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        // source_id and dest_id can be null, getInt returns 0 for null, so guard with getObject
        Integer sourceId = rs.getObject("source_id", Integer.class);
        Integer destId = rs.getObject("dest_id", Integer.class);

        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getString("trans_type"),
                rs.getBigDecimal("trans_amount"),
                rs.getObject("trans_date", LocalDateTime.class),
                sourceId,
                destId);
    }

}