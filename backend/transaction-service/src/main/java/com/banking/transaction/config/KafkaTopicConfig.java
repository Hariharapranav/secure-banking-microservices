package com.banking.transaction.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TRANSACTION_TOPIC = "transaction-events";
    public static final String TRANSACTION_FAILED_TOPIC = "transaction-failed-events";

    @Bean
    public NewTopic transactionTopic() {
        return TopicBuilder.name(TRANSACTION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionFailedTopic() {
        return TopicBuilder.name(TRANSACTION_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
