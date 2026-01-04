package com.vishal.dtx.orchestrator.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.orchestrator.saga.TransactionSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionSaga saga;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "orchestrator-group"
    )
    public void onEvent(TransactionEvent event) {

        log.info(
                "Orchestrator received event | txId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );

        saga.handle(event);
    }
}
