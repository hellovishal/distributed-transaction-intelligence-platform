package com.vishal.dtx.inventory.producer;

import com.vishal.dtx.common.model.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publish(TransactionEvent event) {

        log.info(
                "Publishing INVENTORY_RESERVED | txId={}",
                event.getTransactionId()
        );

        kafkaTemplate.send(
                TOPIC,
                event.getTransactionId(),
                event
        );
    }
}
