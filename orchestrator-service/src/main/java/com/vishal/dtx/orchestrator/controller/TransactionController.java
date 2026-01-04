package com.vishal.dtx.orchestrator.controller;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.orchestrator.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionProducer producer;

    @PostMapping
    public String create(@RequestBody TransactionEvent request) {
        request.setTransactionId(UUID.randomUUID().toString());
        request.setStatus(SagaState.STARTED);
        producer.publish(request);
        return request.getTransactionId();
    }
}
