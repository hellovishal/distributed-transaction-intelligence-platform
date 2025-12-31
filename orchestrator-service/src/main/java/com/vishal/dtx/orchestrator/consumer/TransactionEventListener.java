package com.vishal.dtx.transaction.consumer;

import com.vishal.dtx.transaction.model.TransactionEvent;
import com.vishal.dtx.transaction.service.TransactionProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionProducer producer;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "transaction-service-group"
    )
    public void onEvent(TransactionEvent event) {

        if (event.getStatus() == SagaState.STARTED) {
            log.info("Transaction service processing STARTED for {}", event.getTransactionId());

            // Simulate persistence / validation
            event.setStatus(SagaState.CREATED);

            producer.publish(event);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionEvent {

        private String transactionId;
        private String userId;
        private double amount;
        private String status; // CREATED
    }
}
