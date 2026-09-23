package com.kenya.persistence;

import com.kenya.domain.Transaction;

public interface TransactionDAO {
    void recordTransaction(Transaction transaction);
}