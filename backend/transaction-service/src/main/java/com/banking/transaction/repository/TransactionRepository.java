package com.banking.transaction.repository;

import com.banking.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromAccountNumberOrToAccountNumber(
            String fromAccountNumber, String toAccountNumber, Pageable pageable);

    Page<Transaction> findByFromAccountNumber(String accountNumber, Pageable pageable);

    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber) " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Page<Transaction> findByAccountNumberAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    Page<Transaction> findByInitiatedBy(String username, Pageable pageable);
}
