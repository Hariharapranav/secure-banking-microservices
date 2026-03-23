package com.banking.account.controller;

import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.BalanceUpdateRequest;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Bank Account Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get all accounts for a user")
    public ResponseEntity<List<AccountResponse>> getAccountsByUser(@PathVariable String username) {
        return ResponseEntity.ok(accountService.getAccountsByUsername(username));
    }

    @GetMapping
    @Operation(summary = "Get all accounts (Admin only)")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/balance")
    @Operation(summary = "Update account balance (internal use)")
    public ResponseEntity<AccountResponse> updateBalance(@Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateBalance(request));
    }

    @PatchMapping("/{accountNumber}/status")
    @Operation(summary = "Update account status")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam String status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountNumber, status));
    }
}
