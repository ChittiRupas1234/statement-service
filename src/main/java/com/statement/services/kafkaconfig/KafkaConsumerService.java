package com.statement.services.kafkaconfig;


import com.payments.application.dto.TopupRequest;
import com.payments.application.dto.TransferRequest;
import com.payments.application.dto.WithdrawRequest;

import com.statement.services.entity.Transaction;
import com.statement.services.service.StatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class KafkaConsumerService {

    private final StatementService statementService;

    @Autowired
    public KafkaConsumerService(StatementService statementService) {
        this.statementService = statementService;
    }

    @KafkaListener(topics = "Transfer", groupId = "group_id")
    public void consumeTransfer(TransferRequest request) {
        Transaction transaction = new Transaction(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                "TRANSFER",
                false,
                LocalDateTime.now()
        );
        statementService.insertTransaction(transaction);
    }

    @KafkaListener(topics = "Topup", groupId = "group_id")
    public void consumeTopup(TopupRequest request) {
        Transaction transaction = new Transaction(
                request.getWalletId(),
                null,
                request.getAmount(),
                "TOPUP",
                true,
                LocalDateTime.now()
        );
        statementService.insertTransaction(transaction);
    }

    @KafkaListener(topics = "Withdrawal", groupId = "group_id")
    public void consumeWithdrawal(WithdrawRequest request) {
        Transaction transaction = new Transaction(
                request.getWalletId(),
                null,
                request.getAmount(),
                "WITHDRAWAL",
                false,
                LocalDateTime.now()
        );
        statementService.insertTransaction(transaction);
    }
}
