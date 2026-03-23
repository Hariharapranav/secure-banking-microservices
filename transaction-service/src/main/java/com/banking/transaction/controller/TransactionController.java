package com.banking.transaction.controller;

import com.banking.transaction.dto.*;
import com.banking.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Banking Transaction APIs")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into an account")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = authentication.getName();
        String token = authHeader != null ? authHeader.replace("Bearer ", "") : "";
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.deposit(request, username, token));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from an account")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = authentication.getName();
        String token = authHeader != null ? authHeader.replace("Bearer ", "") : "";
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.withdraw(request, username, token));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = authentication.getName();
        String token = authHeader != null ? authHeader.replace("Bearer ", "") : "";
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.transfer(request, username, token));
    }

    @GetMapping("/history/{accountNumber}")
    @Operation(summary = "Get transaction history with pagination and sorting")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(
                transactionService.getTransactionHistory(accountNumber, page, size, sortBy, direction));
    }

    @GetMapping("/reference/{referenceId}")
    @Operation(summary = "Get transaction by reference ID")
    public ResponseEntity<TransactionResponse> getByReferenceId(@PathVariable String referenceId) {
        return ResponseEntity.ok(transactionService.getTransactionByReferenceId(referenceId));
    }

    @GetMapping("/history/{accountNumber}/range")
    @Operation(summary = "Get transactions in a date range")
    public ResponseEntity<Page<TransactionResponse>> getByDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByDateRange(accountNumber, startDate, endDate, page, size));
    }
}
