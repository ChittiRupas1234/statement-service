package com.statement.services.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateStatementRequest {
    private String walletId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Getters and Setters
}
