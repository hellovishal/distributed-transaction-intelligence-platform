package com.vishal.dtx.orchestrator.controller;

import com.vishal.dtx.common.constants.KafkaTopics;
import com.vishal.dtx.common.;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.common.util.CorrelationIdUtil;
import com.vishal.dtx.transaction.consumer.TransactionEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final KafkaTemplate<String, TransactionEventListener.TransactionEvent> kafkaTemplate;

    @PostMapping
    public String startTransaction(@RequestBody TransactionEventListener.TransactionEvent event) {

        event.setTransactionId(UUID.randomUUID().toString());
        event.setCorrelationId(CorrelationIdUtil.generate());
        event.setStatus(SagaState.STARTED);

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_EVENTS,
                event.getTransactionId(),
                event
        );

        return event.getTransactionId();
    }
}
