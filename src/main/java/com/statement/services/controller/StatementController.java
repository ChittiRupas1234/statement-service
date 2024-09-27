package com.statement.services.controller;

import com.statement.services.service.StatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/statement")
public class StatementController {
    @Autowired
    private StatementService statementService;
    /*@Autowired
    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }*/

    @GetMapping("/generate")
    public ResponseEntity<?> generateStatement(@RequestHeader("Authorization") String token,
                                               @RequestParam UUID walletId,
                                               @RequestParam LocalDateTime fromDate,
                                               @RequestParam String toDate) {
        // Call the statement service to generate the statement.
        try {
            String filePath = statementService.generateStatement(token, walletId, fromDate, LocalDateTime.parse(toDate));
            return ResponseEntity.ok().body("Statement generated successfully at: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating statement: " + e.getMessage());
        }
    }
}
