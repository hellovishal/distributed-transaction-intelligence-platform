package com.vishal.dtx.inventory.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.inventory.producer.InventoryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryEventProducer producer;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "inventory-service-group"
    )
    public void onEvent(TransactionEvent event) {

        switch (event.getStatus().toString()) {

            case "CREATED" -> {
                log.info(
                        "Inventory reserving | txId={}",
                        event.getTransactionId()
                );
                event.setStatus(SagaState.INVENTORY_RESERVED);
                producer.publish(event);
            }

            case "PAYMENT_FAILED" -> {
                log.warn(
                        "Inventory compensation (release) | txId={}",
                        event.getTransactionId()
                );
                event.setStatus(SagaState.INVENTORY_RELEASED);
                producer.publish(event);
            }
        }
    }
}
