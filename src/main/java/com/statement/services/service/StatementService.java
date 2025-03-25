package com.statement.services.service;


import com.statement.services.entity.ArchivedTransaction;
import com.statement.services.repository.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.statement.services.entity.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.FileOutputStream;

@Service
public class StatementService {

    private final TransactionsRepository transactionsRepository;
    private final TokenValidationServices tokenValidationServices;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public TransactionsRepository archivedTransactionRepository;

    @Autowired
    public StatementService(TransactionsRepository transactionsRepository,
                            TokenValidationServices tokenValidationServices,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.transactionsRepository = transactionsRepository;
        this.tokenValidationServices = tokenValidationServices;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void insertTransaction(Transaction transaction) {
        transactionsRepository.save(transaction);
        //kafkaTemplate.send("Transactions", transaction);
    }

    public String generateStatement(String token, UUID walletId, LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
        if (!tokenValidationServices.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }

        List<ArchivedTransaction> transactions = transactionsRepository.findByWalletIdAndDateRange(walletId, fromDate, toDate);
        BigDecimal availableBalance = calculateAvailableBalance(walletId);

        return generateStatementPdf(walletId, transactions, availableBalance);
    }

    private BigDecimal calculateAvailableBalance(UUID walletId) {
        List<ArchivedTransaction> transactions = transactionsRepository.findByWalletId(walletId);
        return transactions.stream()
                .map(tx -> tx.isCredit() ? tx.getAmount() : tx.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateStatementPdf(UUID walletId, List<ArchivedTransaction> transactions, BigDecimal availableBalance)
            throws DocumentException, IOException {

        Path directoryPath = Paths.get("/Users/chittirupas/Downloads/statements");
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
        String fileName = "Wallet_" + walletId + "_" + formattedDateTime + ".pdf";
        Path filePath = directoryPath.resolve(fileName);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
        document.open();

        document.add(new Paragraph("MAGULURI BANK"));
        document.add(new Paragraph("Wallet ID : " + walletId));
        document.add(new Paragraph("Available Balance : " + availableBalance + " Rupees\n"));

        document.add(new Paragraph("Date of Transaction | Type | Amount | CR/DR"));
        document.add(new Paragraph("-----------------------------------------------------"));

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ArchivedTransaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            document.add(new Paragraph(formattedTransactionDate + " | " + tx.getTransactionType() + " | " + tx.getAmount() + " | " + (tx.isCredit() ? "CR" : "DR")));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        document.close();

        return filePath.toString();
    }
    /*private String generateStatementPdf(UUID walletId, List<Transaction> transactions, BigDecimal availableBalance)
            throws DocumentException, IOException {

        Path directoryPath = Paths.get("/Users/chittirupas/Downloads/statements");
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
        String fileName = "Wallet_" + walletId + "_" + formattedDateTime + ".pdf";
        Path filePath = directoryPath.resolve(fileName);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
        document.open();

        document.add(new Paragraph("MAGULURI BANK"));
        document.add(new Paragraph("Wallet ID : " + walletId));
        document.add(new Paragraph("Available Balance : " + availableBalance + " Rupees\n"));

        document.add(new Paragraph("Date of Transaction | Type | From Wallet | To Wallet | Amount | CR/DR"));
        document.add(new Paragraph("---------------------------------------------------------------"));

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Transaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            String fromWalletId = tx.getFromWalletId() != null ? tx.getFromWalletId().toString() : "N/A";
            String toWalletId = tx.getToWalletId() != null ? tx.getToWalletId().toString() : "N/A";
            String transactionType = tx.getTransactionType();
            BigDecimal amount = tx.getAmount();
            String creditOrDebit = tx.isCredit() ? "CR" : "DR";

            document.add(new Paragraph(formattedTransactionDate + " | " + transactionType + " | " + fromWalletId + " | " + toWalletId + " | " + amount + " | " + creditOrDebit));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        document.close();

        return filePath.toString();
    }*/

}