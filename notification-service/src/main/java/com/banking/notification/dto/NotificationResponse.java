package com.banking.notification.dto;

import com.banking.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String transactionReferenceId;
    private String recipientUsername;
    private String type;
    private String subject;
    private String message;
    private String status;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .transactionReferenceId(notification.getTransactionReferenceId())
                .recipientUsername(notification.getRecipientUsername())
                .type(notification.getType().name())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus().name())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
