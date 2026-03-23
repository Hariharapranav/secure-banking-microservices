package com.banking.transaction.dto;

import com.banking.transaction.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private String referenceId;
    private String transactionType;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String currency;
    private String status;
    private String description;
    private String initiatedBy;
    private LocalDateTime transactionDate;

    public static TransactionResponse fromEntity(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .referenceId(txn.getReferenceId())
                .transactionType(txn.getTransactionType().name())
                .fromAccountNumber(txn.getFromAccountNumber())
                .toAccountNumber(txn.getToAccountNumber())
                .amount(txn.getAmount())
                .balanceAfterTransaction(txn.getBalanceAfterTransaction())
                .currency(txn.getCurrency())
                .status(txn.getStatus().name())
                .description(txn.getDescription())
                .initiatedBy(txn.getInitiatedBy())
                .transactionDate(txn.getTransactionDate())
                .build();
    }
}
