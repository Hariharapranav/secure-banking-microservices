package com.banking.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_from_account", columnList = "fromAccountNumber"),
        @Index(name = "idx_to_account", columnList = "toAccountNumber"),
        @Index(name = "idx_transaction_date", columnList = "transactionDate"),
        @Index(name = "idx_reference_id", columnList = "referenceId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, length = 20)
    private String fromAccountNumber;

    @Column(length = 20)
    private String toAccountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String initiatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime transactionDate;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, REFUND
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, REVERSED
    }
}
