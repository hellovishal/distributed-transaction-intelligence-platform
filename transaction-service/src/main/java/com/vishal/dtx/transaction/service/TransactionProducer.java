package com.vishal.dtx.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publish(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionId(), event);
    }
}
