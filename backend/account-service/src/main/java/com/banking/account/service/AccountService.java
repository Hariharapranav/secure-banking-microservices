package com.banking.account.service;

import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.BalanceUpdateRequest;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Account;
import com.banking.account.exception.AccountException;
import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.ResourceNotFoundException;
import com.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository repository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for user: {}", request.getUsername());

        if (request.getInitialDeposit().compareTo(BigDecimal.valueOf(1000)) < 0) {
            throw new AccountException("Minimum initial deposit is ₹1000");
        }

        Account.AccountType accountType;
        try {
            accountType = Account.AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AccountException("Invalid account type. Valid types: SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT");
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .username(request.getUsername())
                .accountType(accountType)
                .balance(request.getInitialDeposit())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(Account.AccountStatus.ACTIVE)
                .branchCode(request.getBranchCode() != null ? request.getBranchCode() : "BR001")
                .ifscCode(request.getIfscCode() != null ? request.getIfscCode() : "BANK0000001")
                .build();

        account = repository.save(account);
        log.info("Account created: {} for user: {}", accountNumber, request.getUsername());

        return AccountResponse.fromEntity(account);
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Fetching account: {}", accountNumber);
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return AccountResponse.fromEntity(account);
    }

    public List<AccountResponse> getAccountsByUsername(String username) {
        log.info("Fetching accounts for user: {}", username);
        return repository.findByUsername(username).stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AccountResponse> getAllAccounts() {
        log.info("Fetching all accounts");
        return repository.findAll().stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#request.accountNumber")
    public AccountResponse updateBalance(BalanceUpdateRequest request) {
        log.info("Updating balance for account: {}, operation: {}, amount: {}",
                request.getAccountNumber(), request.getOperationType(), request.getAmount());

        Account account = repository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountException("Account is not active. Current status: " + account.getStatus());
        }

        if ("CREDIT".equalsIgnoreCase(request.getOperationType())) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else if ("DEBIT".equalsIgnoreCase(request.getOperationType())) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance. Available: ₹" + account.getBalance());
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else {
            throw new AccountException("Invalid operation type. Use CREDIT or DEBIT");
        }

        account = repository.save(account);
        log.info("Balance updated for account: {}. New balance: {}", account.getAccountNumber(), account.getBalance());

        return AccountResponse.fromEntity(account);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse updateAccountStatus(String accountNumber, String status) {
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        try {
            account.setStatus(Account.AccountStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AccountException("Invalid status. Valid: ACTIVE, INACTIVE, FROZEN, CLOSED");
        }

        account = repository.save(account);
        return AccountResponse.fromEntity(account);
    }

    private String generateAccountNumber() {
        String accountNumber;
        Random random = new Random();
        do {
            long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (repository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
