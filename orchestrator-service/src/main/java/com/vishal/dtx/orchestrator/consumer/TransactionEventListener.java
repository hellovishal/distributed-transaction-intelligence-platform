package com.vishal.dtx.orchestrator.consumer;

import com.vishal.dtx.orchestrator.model.TransactionEvent;
import com.vishal.dtx.orchestrator.saga.TransactionSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionSaga saga;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "orchestrator-group"
    )
    public void onEvent(TransactionEvent event) {
        saga.handle(event);
    }
}
