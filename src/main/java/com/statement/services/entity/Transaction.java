package com.statement.services.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Table;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions") // Ensure the table name matches your database table
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String transactionType;
    private boolean isCredit;
    private LocalDateTime transactionDate;

    public Transaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String transfer, boolean isCredit, LocalDateTime now) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.transactionType = transfer;
        this.isCredit = isCredit;
        this.transactionDate = now;
    }

}
