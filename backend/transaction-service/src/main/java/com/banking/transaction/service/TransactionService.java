package com.banking.transaction.service;

import com.banking.transaction.dto.*;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.exception.TransactionException;
import com.banking.transaction.exception.ResourceNotFoundException;
import com.banking.transaction.kafka.TransactionEventProducer;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionEventProducer eventProducer;
    private final RestTemplate restTemplate;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    @Transactional
    public TransactionResponse deposit(DepositRequest request, String username, String token) {
        log.info("Processing deposit: account={}, amount={}, user={}",
                request.getAccountNumber(), request.getAmount(), username);

        String referenceId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .referenceId(referenceId)
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .fromAccountNumber(request.getAccountNumber())
                .toAccountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Cash Deposit")
                .initiatedBy(username)
                .build();

        transaction = repository.save(transaction);

        try {
            // Call Account Service to credit balance
            BigDecimal newBalance = updateAccountBalance(
                    request.getAccountNumber(), request.getAmount(), "CREDIT", token);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setBalanceAfterTransaction(newBalance);
            transaction = repository.save(transaction);

            // Publish Kafka event
            publishEvent(transaction);

            log.info("Deposit completed: referenceId={}, newBalance={}", referenceId, newBalance);

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setDescription(transaction.getDescription() + " | FAILED: " + e.getMessage());
            repository.save(transaction);

            publishFailedEvent(transaction);
            throw new TransactionException("Deposit failed: " + e.getMessage());
        }

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, String username, String token) {
        log.info("Processing withdrawal: account={}, amount={}, user={}",
                request.getAccountNumber(), request.getAmount(), username);

        String referenceId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .referenceId(referenceId)
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .fromAccountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Cash Withdrawal")
                .initiatedBy(username)
                .build();

        transaction = repository.save(transaction);

        try {
            BigDecimal newBalance = updateAccountBalance(
                    request.getAccountNumber(), request.getAmount(), "DEBIT", token);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setBalanceAfterTransaction(newBalance);
            transaction = repository.save(transaction);

            publishEvent(transaction);
            log.info("Withdrawal completed: referenceId={}, newBalance={}", referenceId, newBalance);

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setDescription(transaction.getDescription() + " | FAILED: " + e.getMessage());
            repository.save(transaction);

            publishFailedEvent(transaction);
            throw new TransactionException("Withdrawal failed: " + e.getMessage());
        }

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String username, String token) {
        log.info("Processing transfer: from={}, to={}, amount={}, user={}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount(), username);

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new TransactionException("Cannot transfer to the same account");
        }

        String referenceId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .referenceId(referenceId)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Fund Transfer")
                .initiatedBy(username)
                .build();

        transaction = repository.save(transaction);

        try {
            // Debit from source account
            BigDecimal sourceBalance = updateAccountBalance(
                    request.getFromAccountNumber(), request.getAmount(), "DEBIT", token);

            try {
                // Credit to destination account
                updateAccountBalance(
                        request.getToAccountNumber(), request.getAmount(), "CREDIT", token);
            } catch (Exception e) {
                // Rollback: credit back the source account
                log.error("Credit to destination failed, rolling back debit from source");
                updateAccountBalance(
                        request.getFromAccountNumber(), request.getAmount(), "CREDIT", token);
                throw e;
            }

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setBalanceAfterTransaction(sourceBalance);
            transaction = repository.save(transaction);

            publishEvent(transaction);
            log.info("Transfer completed: referenceId={}", referenceId);

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setDescription(transaction.getDescription() + " | FAILED: " + e.getMessage());
            repository.save(transaction);

            publishFailedEvent(transaction);
            throw new TransactionException("Transfer failed: " + e.getMessage());
        }

        return TransactionResponse.fromEntity(transaction);
    }

    public Page<TransactionResponse> getTransactionHistory(String accountNumber, int page, int size, String sortBy, String direction) {
        log.info("Fetching transaction history for account: {}", accountNumber);

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return repository.findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber, pageable)
                .map(TransactionResponse::fromEntity);
    }

    public TransactionResponse getTransactionByReferenceId(String referenceId) {
        Transaction transaction = repository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + referenceId));
        return TransactionResponse.fromEntity(transaction);
    }

    public Page<TransactionResponse> getTransactionsByDateRange(
            String accountNumber, LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return repository.findByAccountNumberAndDateRange(accountNumber, startDate, endDate, pageable)
                .map(TransactionResponse::fromEntity);
    }

    private BigDecimal updateAccountBalance(String accountNumber, BigDecimal amount,
                                            String operationType, String token) {
        String url = accountServiceUrl + "/api/accounts/balance";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("accountNumber", accountNumber);
        body.put("amount", amount);
        body.put("operationType", operationType);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object balanceObj = response.getBody().get("balance");
                if (balanceObj != null) {
                    return new BigDecimal(balanceObj.toString());
                }
            }
            throw new TransactionException("Failed to update account balance");
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling account service: {}", e.getMessage());
            throw new TransactionException("Account service unavailable: " + e.getMessage());
        }
    }

    private void publishEvent(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .referenceId(transaction.getReferenceId())
                .transactionType(transaction.getTransactionType().name())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .initiatedBy(transaction.getInitiatedBy())
                .transactionDate(transaction.getTransactionDate())
                .build();

        eventProducer.publishTransactionEvent(event);
    }

    private void publishFailedEvent(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .referenceId(transaction.getReferenceId())
                .transactionType(transaction.getTransactionType().name())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status("FAILED")
                .description(transaction.getDescription())
                .initiatedBy(transaction.getInitiatedBy())
                .transactionDate(transaction.getTransactionDate())
                .build();

        eventProducer.publishFailedTransactionEvent(event);
    }
}
