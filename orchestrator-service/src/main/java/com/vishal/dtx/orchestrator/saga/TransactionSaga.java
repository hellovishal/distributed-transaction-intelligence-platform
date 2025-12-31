package com.vishal.dtx.orchestrator.saga;

import com.vishal.dtx.common.constants.KafkaTopics;
import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionSaga {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void handle(TransactionEvent event) {

        switch (event.getStatus()) {

            case CREATED -> {
                log.info("Saga → CREATED, requesting inventory");
                event.setStatus(SagaState.INVENTORY_RESERVED);
                kafkaTemplate.send(
                        KafkaTopics.INVENTORY_EVENTS,
                        event.getTransactionId(),
                        event
                );
            }

            case INVENTORY_RESERVED -> {
                log.info("Saga → INVENTORY_RESERVED, requesting payment");
                event.setStatus(SagaState.PAYMENT_COMPLETED);
                kafkaTemplate.send(
                        KafkaTopics.PAYMENT_EVENTS,
                        event.getTransactionId(),
                        event
                );
            }

            case PAYMENT_COMPLETED -> {
                log.info("Saga → PAYMENT_COMPLETED, transaction COMPLETED");
                event.setStatus(SagaState.COMPLETED);
            }

            case FAILED -> {
                log.warn("Saga → FAILED, compensation required");
                // future: compensation logic
            }

            default -> log.info("Saga ignoring state {}", event.getStatus());
        }
    }
}
