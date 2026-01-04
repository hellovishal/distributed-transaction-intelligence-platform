package com.vishal.dtx.orchestrator.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DLQListener {

    @KafkaListener(
            topics = "transaction-events-DLT",
            groupId = "dlq-monitor-group"
    )
    public void onDLQ(TransactionEvent event) {
        log.error(
                "DLQ received | txId={} | status={}",
                event.getTransactionId(),
                event.getStatus()
        );
    }
}
