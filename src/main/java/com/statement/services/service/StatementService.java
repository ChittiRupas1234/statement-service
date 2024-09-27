package com.statement.services.service;


import com.statement.services.repository.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.statement.services.entity.Transaction;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatementService {

    private final TransactionsRepository transactionsRepository;
    private final TokenValidationServices tokenValidationServices;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        kafkaTemplate.send("Transactions", transaction);
    }

    public String generateStatement(String token, UUID walletId, LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
        if (!tokenValidationServices.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }

        List<Transaction> transactions = transactionsRepository.findByWalletIdAndDateRange(walletId, fromDate, toDate);
        BigDecimal availableBalance = calculateAvailableBalance(walletId);

        return generateStatementPdf(walletId, transactions, availableBalance);
    }

    private BigDecimal calculateAvailableBalance(UUID walletId) {
        List<Transaction> transactions = transactionsRepository.findByWalletId(walletId);
        return transactions.stream()
                .map(tx -> tx.isCredit() ? tx.getAmount() : tx.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateStatementPdf(UUID walletId, List<Transaction> transactions, BigDecimal availableBalance)
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
        for (Transaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            document.add(new Paragraph(formattedTransactionDate + " | " + tx.getTransactionType() + " | " + tx.getAmount() + " | " + (tx.isCredit() ? "CR" : "DR")));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        document.close();

        return filePath.toString();
    }
}

/*@Service
public class StatementService {
    private final TransactionsRepository transactionsRepository;
    private final TokenValidationService tokenValidationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Autowired
    public StatementService(TransactionsRepository transactionsRepository,
                            TokenValidationService tokenValidationService,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.transactionsRepository = transactionsRepository;
        this.tokenValidationService = tokenValidationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void insertTransaction(Transaction transaction) {
        transactionsRepository.save(transaction);
        kafkaTemplate.send("Transactions", transaction);
    }

    public String generateStatement(String token, UUID walletId, LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
        if (!tokenValidationService.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }

        List<Transaction> transactions = transactionsRepository.findByWalletIdAndDateRange(walletId, fromDate, toDate);
        BigDecimal availableBalance = calculateAvailableBalance(walletId);

        return generateStatementPdf(walletId, transactions, availableBalance);
    }

    private BigDecimal calculateAvailableBalance(UUID walletId) {
        // Calculate the available balance based on the transactions
        // This assumes that transactions with positive amounts are credits and negative amounts are debits
        List<Transaction> transactions = transactionsRepository.findByWalletId(walletId);
        return transactions.stream()
                .map(tx -> tx.isCredit() ? tx.getAmount() : tx.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateStatementPdf(UUID walletId, List<Transaction> transactions, BigDecimal availableBalance) throws DocumentException, IOException {
        Path directoryPath = Paths.get("/path/to/statements/");
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
        for (Transaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            document.add(new Paragraph(formattedTransactionDate + " | " + tx.getTransactionType() + " | " + tx.getAmount() + " | " + (tx.isCredit() ? "CR" : "DR")));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        document.close();

        return filePath.toString();
    }
}*/

/*@Service
public class StatementService {
    private final TransactionsRepository transactionsRepository;
    private final TokenValidationService tokenValidationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public StatementService(TransactionsRepository transactionsRepository,
                            TokenValidationService tokenValidationService,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.transactionsRepository = transactionsRepository;
        this.tokenValidationService = tokenValidationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void insertTransaction(Transaction transaction) {
        transactionsRepository.save(transaction);
        // Send transaction to Kafka topic
        kafkaTemplate.send("TransactionsTopic", transaction);
    }

    public String generateStatement(String token, UUID walletId, String fromDate, String toDate) throws Exception {
        if (!tokenValidationService.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }

        LocalDateTime from = LocalDateTime.parse(fromDate);
        LocalDateTime to = LocalDateTime.parse(toDate);

        List<Transaction> transactions = transactionsRepository.findByWalletIdAndDateRange(walletId, from, to);

        return generateStatementPdf(walletId, transactions);
    }
    private BigDecimal calculateAvailableBalance(UUID walletId) {
        // Calculate the available balance based on the transactions
        // This assumes that transactions with positive amounts are credits and negative amounts are debits
        List<Transaction> transactions = transactionsRepository.findByWalletId(walletId);
        BigDecimal availableBalance = calculateAvailableBalance(walletId);
        return transactions.stream()
                .map(tx -> tx.isCredit() ? tx.getAmount() : tx.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateStatementPdf(UUID walletId, List<Transaction> transactions) throws DocumentException, IOException {
        // Ensure the directory exists
        Path directoryPath = Paths.get("/path/to/statements/");
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Format the LocalDateTime for the filename and transaction dates
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
        String fileName = "Wallet_" + walletId + "_" + formattedDateTime + ".pdf";
        Path filePath = directoryPath.resolve(fileName);

        // Create a new PDF document
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
        document.open();

        // Add document content
        document.add(new Paragraph("MAGULURI BANK"));
        document.add(new Paragraph("Wallet ID : " + walletId));
        document.add(new Paragraph("Available Balance : " + availableBalance +" Rupees\n"));

        document.add(new Paragraph("Date of Transaction     | Type | Amount  | CR/DR"));
        document.add(new Paragraph("-----------------------------------------------------"));

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Transaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            document.add(new Paragraph(formattedTransactionDate + " | " + tx.getTransactionType() + " | " + tx.getAmount() + " | " + (tx.isCredit() ? "CR" : "DR")));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        // Close the document
        document.close();

        return filePath.toString();
    }
}*/


/*@Service
public class StatementService {
    private final TransactionsRepository transactionsRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TokenValidationService tokenValidationService;
    //private final PaymentService paymentService;


    @Autowired
    public StatementService(TransactionsRepository transactionsRepository,
                            KafkaTemplate<String, Object> kafkaTemplate,
                            TokenValidationService tokenValidationService) {
        this.transactionsRepository = transactionsRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.tokenValidationService = tokenValidationService;
    }

    public void insertTransaction(Transaction transaction) {
        transactionsRepository.save(transaction);
    }

    public String generateStatement(String token, UUID walletId, String fromDate, String toDate) throws Exception {
        // Validate token
        if (!tokenValidationService.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }

        // Get balance from Payment Service
        //BigDecimal balance = paymentService.getBalance(walletId);

        // Parse date strings into LocalDateTime
        LocalDateTime from = LocalDateTime.parse(fromDate);
        LocalDateTime to = LocalDateTime.parse(toDate);

        // Get transactions from the repository within the date range
        List<Transaction> transactions = transactionsRepository.findByWalletIdAndDateRange(walletId, from, to);

        // Generate the PDF document
        return generateStatementPdf(walletId, transactions);
    }

    private String generateStatementPdf(UUID walletId, List<Transaction> transactions) throws DocumentException, IOException {
        // Ensure the directory exists
        Path directoryPath = Paths.get("/path/to/statements/");
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Format the LocalDateTime for the filename and transaction dates
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
        String fileName = "Wallet_" + walletId + "_" + formattedDateTime + ".pdf";
        Path filePath = directoryPath.resolve(fileName);

        // Create a new PDF document
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
        document.open();

        // Add document content
        document.add(new Paragraph("MAGULURI BANK"));
        document.add(new Paragraph("Wallet ID : " + walletId));
        document.add(new Paragraph("Available Balance : " + " Rupees\n"));

        document.add(new Paragraph("Date of Transaction     | Type | Amount  | CR/DR"));
        document.add(new Paragraph("-----------------------------------------------------"));

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Transaction tx : transactions) {
            String formattedTransactionDate = tx.getTransactionDate().format(transactionFormatter);
            document.add(new Paragraph(formattedTransactionDate + " | " + tx.getTransactionType() + " | " + tx.getAmount() + " | " + (tx.isCredit() ? "CR" : "DR")));
        }

        document.add(new Paragraph("\nThank you for banking with us.\n"));
        document.add(new Paragraph("Customer Care: Sorry, inka antha budget ledu"));

        // Close the document
        document.close();

        return filePath.toString();
    }
}*/
