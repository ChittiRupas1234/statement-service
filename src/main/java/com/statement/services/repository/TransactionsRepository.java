package com.statement.services.repository;


import com.statement.services.entity.ArchivedTransaction;
import com.statement.services.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface TransactionsRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM ArchivedTransaction t WHERE t.fromWalletId = :walletId AND t.transactionDate BETWEEN :fromDate AND :toDate")
    List<ArchivedTransaction> findByWalletIdAndDateRange(@Param("walletId") UUID walletId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate);

    @Query("SELECT t FROM ArchivedTransaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<ArchivedTransaction> findByWalletId(@Param("walletId") UUID walletId);

    List<Transaction> findByTransactionDateBefore(LocalDateTime cutoffDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.transactionDate < :cutoffDate")
    void deleteOldTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);

    /*List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ✅ Delete transactions for a specific date range
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    void deleteOldTransactions(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);*/
}