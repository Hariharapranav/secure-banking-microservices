package com.banking.notification.kafka;

import com.banking.notification.dto.TransactionEvent;
import com.banking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  TRANSACTION EVENT RECEIVED                                 ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Reference ID  : {}",  event.getReferenceId());
        log.info("║  Type          : {}",  event.getTransactionType());
        log.info("║  From Account  : {}",  event.getFromAccountNumber());
        log.info("║  To Account    : {}",  event.getToAccountNumber());
        log.info("║  Amount        : {} {}",  event.getCurrency(), event.getAmount());
        log.info("║  Status        : {}",  event.getStatus());
        log.info("║  Initiated By  : {}",  event.getInitiatedBy());
        log.info("║  Date          : {}",  event.getTransactionDate());
        log.info("╚══════════════════════════════════════════════════════════════╝");

        try {
            notificationService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error processing transaction event: referenceId={}, error={}",
                    event.getReferenceId(), e.getMessage());
        }
    }

    @KafkaListener(
            topics = "transaction-failed-events",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeFailedTransactionEvent(TransactionEvent event) {
        log.warn("╔══════════════════════════════════════════════════════════════╗");
        log.warn("║  ⚠ FAILED TRANSACTION EVENT RECEIVED                       ║");
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("║  Reference ID  : {}",  event.getReferenceId());
        log.warn("║  Type          : {}",  event.getTransactionType());
        log.warn("║  Amount        : {} {}",  event.getCurrency(), event.getAmount());
        log.warn("║  Description   : {}",  event.getDescription());
        log.warn("╚══════════════════════════════════════════════════════════════╝");

        try {
            notificationService.processFailedTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error processing failed transaction event: {}", e.getMessage());
        }
    }
}
