package com.kenya.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private String transType;      // DEPOSIT, WITHDRAW, TRANSFER
    private BigDecimal transAmount;
    private LocalDateTime transDate;
    private Integer sourceId;      // using the wrapper type so it can be null
    private Integer destId;        // Integer so it can be null

    public Transaction(int transactionId, String transType, BigDecimal transAmount,
                       LocalDateTime transDate, Integer sourceId, Integer destId) {
        this.transactionId = transactionId;
        this.transType = transType;
        this.transAmount = transAmount;
        this.transDate = transDate;
        this.sourceId = sourceId;
        this.destId = destId;
    }

    public int getTransactionId()     { return transactionId; }
    public String getTransType()      { return transType; }
    public BigDecimal getTransAmount(){ return transAmount; }
    public LocalDateTime getTransDate(){ return transDate; }
    public Integer getSourceId()      { return sourceId; }
    public Integer getDestId()        { return destId; }
}