package com.banking.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String transactionReferenceId;

    @Column(nullable = false, length = 50)
    private String recipientUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(length = 20)
    private String channel; // EMAIL, SMS, PUSH, IN_APP

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public enum NotificationType {
        DEPOSIT, WITHDRAWAL, TRANSFER_SENT, TRANSFER_RECEIVED, TRANSACTION_FAILED, ACCOUNT_CREATED
    }

    public enum NotificationStatus {
        SENT, DELIVERED, READ, FAILED
    }
}
