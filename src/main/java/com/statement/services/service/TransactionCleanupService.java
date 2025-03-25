package com.statement.services.service;

import com.statement.services.entity.ArchivedTransaction;
import com.statement.services.entity.Transaction;
import com.statement.services.repository.ArchivedTransactionRepository;
import com.statement.services.repository.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TransactionCleanupService {

    private final TransactionsRepository transactionsRepository;
    private final ArchivedTransactionRepository archivedTransactionRepository;

    public TransactionCleanupService(TransactionsRepository transactionsRepository,
                                     ArchivedTransactionRepository archivedTransactionRepository) {
        this.transactionsRepository = transactionsRepository;
        this.archivedTransactionRepository = archivedTransactionRepository;
    }

    /**
     * Runs every 2 minutes (for testing) to archive & delete transactions older than 30 days
     */
    @Scheduled(cron = "0 0 0 * * *") // Runs every 2 minutes
    @Transactional
    public void archiveAndDeleteOldTransactions() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(0); // Changeable to 1 for testing

        System.out.println("⏳ Fetching transactions before: " + thirtyDaysAgo);

        // Step 1: Fetch old transactions
        List<Transaction> oldTransactions = transactionsRepository.findByTransactionDateBefore(thirtyDaysAgo);

        if (oldTransactions.isEmpty()) {
            System.out.println("No old transactions found to archive.");
            return;
        }

        System.out.println(" Found " + oldTransactions.size() + " transactions to archive.");

        // Step 2: Archive transactions
        List<ArchivedTransaction> archivedTransactions = oldTransactions.stream()
                .map(ArchivedTransaction::new)
                .collect(Collectors.toList());
        archivedTransactionRepository.saveAll(archivedTransactions);
        System.out.println(" Archived " + archivedTransactions.size() + " transactions.");

        // Step 3: Delete transactions from original table
        transactionsRepository.deleteOldTransactions(thirtyDaysAgo);
        System.out.println(" Transactions deleted successfully.");
    }
}


