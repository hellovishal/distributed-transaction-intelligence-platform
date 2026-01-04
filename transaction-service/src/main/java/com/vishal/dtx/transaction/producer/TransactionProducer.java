package com.vishal.dtx.transaction.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.vishal.dtx.common.model.TransactionEvent;

@Service
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publish(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionId(), event);
    }
}
