package com.statement.services.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "archived_transactions")
public class ArchivedTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String transactionType;
    private boolean isCredit;
    private LocalDateTime transactionDate;

    // Constructor to map WalletTransaction -> ArchivedTransaction
    public ArchivedTransaction(Transaction transaction) {
        this.fromWalletId = transaction.getFromWalletId();
        this.toWalletId = transaction.getToWalletId();  // Fixed
        this.amount = transaction.getAmount();
        this.transactionType = transaction.getTransactionType();
        this.isCredit = transaction.isCredit();
        this.transactionDate = transaction.getTransactionDate();
    }
}