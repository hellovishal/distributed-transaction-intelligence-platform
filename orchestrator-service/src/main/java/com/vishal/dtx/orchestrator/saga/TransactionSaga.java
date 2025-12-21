package com.vishal.dtx.orchestrator.saga;

import com.vishal.dtx.orchestrator.model.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TransactionSaga {

    private final ConcurrentHashMap<String, SagaState> sagaStore = new ConcurrentHashMap<>();

    public void handle(TransactionEvent event) {
        sagaStore.putIfAbsent(event.getTransactionId(), SagaState.STARTED);

        SagaState state = sagaStore.get(event.getTransactionId());

        switch (state) {
            case STARTED -> reserveInventory(event);
            case INVENTORY_RESERVED -> processPayment(event);
            case PAYMENT_COMPLETED -> completeTransaction(event);
            default -> log.warn("Saga ended or invalid state");
        }
    }

    private void reserveInventory(TransactionEvent event) {
        log.info("Reserving inventory for {}", event.getTransactionId());
        sagaStore.put(event.getTransactionId(), SagaState.INVENTORY_RESERVED);
    }

    private void processPayment(TransactionEvent event) {
        log.info("Processing payment for {}", event.getTransactionId());
        sagaStore.put(event.getTransactionId(), SagaState.PAYMENT_COMPLETED);
    }

    private void completeTransaction(TransactionEvent event) {
        log.info("Transaction completed {}", event.getTransactionId());
        sagaStore.put(event.getTransactionId(), SagaState.COMPLETED);
    }
}
