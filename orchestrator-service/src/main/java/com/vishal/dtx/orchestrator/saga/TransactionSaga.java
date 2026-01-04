package com.vishal.dtx.orchestrator.saga;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TransactionSaga {

    private final Map<String, SagaState> sagaStore = new ConcurrentHashMap<>();

    public void handle(TransactionEvent event) {
        sagaStore.putIfAbsent(event.getTransactionId(), SagaState.STARTED);
        switch (event.getStatus().toString()) {

            case "CREATED" -> {
                log.info(
                        "Saga → CREATED, requesting inventory | txId={}",
                        event.getTransactionId()
                );
                sagaStore.put(event.getTransactionId(), SagaState.INVENTORY_RESERVED);
            }

            case "INVENTORY_RESERVED" -> {
                log.info(
                        "Saga → INVENTORY_RESERVED, requesting payment | txId={}",
                        event.getTransactionId()
                );
                sagaStore.put(event.getTransactionId(), SagaState.PAYMENT_COMPLETED);
            }

            case "PAYMENT_COMPLETED" -> {
                log.info(
                        "Saga → PAYMENT_COMPLETED, completing transaction | txId={}",
                        event.getTransactionId()
                );
                sagaStore.put(event.getTransactionId(), SagaState.COMPLETED);
            }
            case "PAYMENT_FAILED" -> {
                log.warn(
                        "Saga → PAYMENT_FAILED, triggering compensation | txId={}",
                        event.getTransactionId()
                );
                sagaStore.put(event.getTransactionId(), SagaState.FAILED);
            }

            case "INVENTORY_RELEASED" -> {
                log.warn(
                        "Saga → INVENTORY_RELEASED, saga FAILED | txId={}",
                        event.getTransactionId()
                );
                sagaStore.put(event.getTransactionId(), SagaState.FAILED);
            }

            default -> log.info(
                    "Saga ignoring state {} | txId={}",
                    event.getStatus(),
                    event.getTransactionId()
            );
        }
    }
}
