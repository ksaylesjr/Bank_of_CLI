package com.kenya.persistence;

import com.kenya.domain.Transaction;
import java.util.List;

public interface TransactionDAO {
    void recordTransaction(Transaction transaction);
    List<Transaction> getTransactionsForAccount(int accountId);
}