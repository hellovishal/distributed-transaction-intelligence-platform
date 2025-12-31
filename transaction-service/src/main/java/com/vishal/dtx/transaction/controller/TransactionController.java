package com.vishal.dtx.transaction.controller;

import com.vishal.dtx.transaction.service.TransactionProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionProducer producer;

    @PostMapping
    public String createTransaction(@RequestBody TransactionEvent request) {
        request.setTransactionId(UUID.randomUUID().toString());
        request.setStatus("CREATED");
        producer.publish(request);
        return request.getTransactionId();
    }
}
