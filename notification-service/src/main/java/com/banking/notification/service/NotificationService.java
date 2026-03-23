package com.banking.notification.service;

import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.dto.TransactionEvent;
import com.banking.notification.entity.Notification;
import com.banking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void processTransactionEvent(TransactionEvent event) {
        log.info("Processing notification for transaction: {}", event.getReferenceId());

        Notification.NotificationType notificationType = mapTransactionType(event.getTransactionType());
        String subject = generateSubject(event);
        String message = generateMessage(event);

        Notification notification = Notification.builder()
                .transactionReferenceId(event.getReferenceId())
                .recipientUsername(event.getInitiatedBy())
                .type(notificationType)
                .subject(subject)
                .message(message)
                .status(Notification.NotificationStatus.SENT)
                .channel("IN_APP")
                .build();

        repository.save(notification);
        log.info("✅ Notification saved for user: {} | Subject: {}", event.getInitiatedBy(), subject);
    }

    @Transactional
    public void processFailedTransactionEvent(TransactionEvent event) {
        log.warn("Processing failed transaction notification for: {}", event.getReferenceId());

        Notification notification = Notification.builder()
                .transactionReferenceId(event.getReferenceId())
                .recipientUsername(event.getInitiatedBy())
                .type(Notification.NotificationType.TRANSACTION_FAILED)
                .subject("Transaction Failed - " + event.getTransactionType())
                .message(String.format(
                        "Your %s transaction of %s %s has FAILED. Reference: %s. Reason: %s",
                        event.getTransactionType(),
                        event.getCurrency(),
                        event.getAmount(),
                        event.getReferenceId(),
                        event.getDescription()
                ))
                .status(Notification.NotificationStatus.SENT)
                .channel("IN_APP")
                .build();

        repository.save(notification);
        log.warn("⚠ Failed transaction notification saved for user: {}", event.getInitiatedBy());
    }

    public Page<NotificationResponse> getUserNotifications(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByRecipientUsernameOrderByCreatedAtDesc(username, pageable)
                .map(NotificationResponse::fromEntity);
    }

    public Map<String, Object> getUnreadCount(String username) {
        long count = repository.countByRecipientUsernameAndStatus(
                username, Notification.NotificationStatus.SENT);
        return Map.of("username", username, "unreadCount", count);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        notification.setStatus(Notification.NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notification = repository.save(notification);

        return NotificationResponse.fromEntity(notification);
    }

    private Notification.NotificationType mapTransactionType(String transactionType) {
        return switch (transactionType.toUpperCase()) {
            case "DEPOSIT" -> Notification.NotificationType.DEPOSIT;
            case "WITHDRAWAL" -> Notification.NotificationType.WITHDRAWAL;
            case "TRANSFER" -> Notification.NotificationType.TRANSFER_SENT;
            default -> Notification.NotificationType.DEPOSIT;
        };
    }

    private String generateSubject(TransactionEvent event) {
        return switch (event.getTransactionType().toUpperCase()) {
            case "DEPOSIT" -> "💰 Money Deposited Successfully";
            case "WITHDRAWAL" -> "💸 Money Withdrawn Successfully";
            case "TRANSFER" -> "🔄 Money Transfer Successful";
            default -> "📋 Transaction Update";
        };
    }

    private String generateMessage(TransactionEvent event) {
        return switch (event.getTransactionType().toUpperCase()) {
            case "DEPOSIT" -> String.format(
                    "₹%s has been deposited to your account %s. Reference: %s",
                    event.getAmount(), event.getFromAccountNumber(), event.getReferenceId()
            );
            case "WITHDRAWAL" -> String.format(
                    "₹%s has been withdrawn from your account %s. Reference: %s",
                    event.getAmount(), event.getFromAccountNumber(), event.getReferenceId()
            );
            case "TRANSFER" -> String.format(
                    "₹%s has been transferred from account %s to account %s. Reference: %s",
                    event.getAmount(), event.getFromAccountNumber(),
                    event.getToAccountNumber(), event.getReferenceId()
            );
            default -> String.format(
                    "Transaction %s of ₹%s completed. Reference: %s",
                    event.getTransactionType(), event.getAmount(), event.getReferenceId()
            );
        };
    }
}
