package com.statement.services.service;

import com.statement.services.entity.Transaction;
import com.statement.services.repository.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
/*
@Service
public class TransactionService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    @KafkaListener(topics = "transfer-topic", groupId = "group_id")
    public void consumeTransfer(Transaction transaction) {
        transactionsRepository.save(transaction);
    }

    @KafkaListener(topics = "topup-topic", groupId = "group_id")
    public void consumeTopup(Transaction transaction) {
        transactionsRepository.save(transaction);
    }

    @KafkaListener(topics = "withdrawal-topic", groupId = "group_id")
    public void consumeWithdrawal(Transaction transaction) {
        transactionsRepository.save(transaction);
    }
}
*/
