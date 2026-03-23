package com.banking.transaction.kafka;

import com.banking.transaction.config.KafkaTopicConfig;
import com.banking.transaction.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing transaction event: referenceId={}, type={}", 
                event.getReferenceId(), event.getTransactionType());

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_TOPIC, event.getReferenceId(), event);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to publish transaction event: referenceId={}, error={}",
                        event.getReferenceId(), throwable.getMessage());
            } else {
                log.info("Transaction event published successfully: referenceId={}, partition={}, offset={}",
                        event.getReferenceId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    public void publishFailedTransactionEvent(TransactionEvent event) {
        log.warn("Publishing failed transaction event: referenceId={}", event.getReferenceId());
        kafkaTemplate.send(KafkaTopicConfig.TRANSACTION_FAILED_TOPIC, event.getReferenceId(), event);
    }
}
