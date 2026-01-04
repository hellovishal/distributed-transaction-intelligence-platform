package com.vishal.dtx.transaction.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.transaction.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionProducer producer;
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = "transaction-events",
            groupId = "transaction-service-group"
    )
    public void onEvent(TransactionEvent event) {

        log.info(
                "Transaction Service received event | txId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );
        String key = event.getTransactionId() + ":" + event.getStatus();

        if (!processed.add(key)) {
            log.warn("Duplicate event ignored | {}", key);
            return;
        }
        if ("STARTED".equals(event.getStatus().toString())) {

            event.setStatus(SagaState.CREATED);

            log.info(
                    "Transaction Service publishing CREATED | txId={}",
                    event.getTransactionId()
            );

            producer.publish(event);
        }
    }
}
