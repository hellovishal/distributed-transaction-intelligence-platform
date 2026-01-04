package com.vishal.dtx.orchestrator.saga;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.orchestrator.domain.SagaInstance;
import com.vishal.dtx.orchestrator.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionSaga {

    private final SagaRepository sagaRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "saga:";

    @Transactional
    public void handle(TransactionEvent event) {

        String txId = event.getTransactionId();
        String redisKey = REDIS_KEY_PREFIX + txId;

        SagaInstance saga = sagaRepository
                .findById(txId)
                .orElseGet(() ->
                        new SagaInstance(
                                txId,
                                SagaState.STARTED.name(),
                                Instant.now()
                        )
                );

        switch (event.getStatus().toString()) {

            case "CREATED" -> {
                log.info(
                        "Saga → CREATED, requesting inventory | txId={}",
                        txId
                );
                saga.setState(SagaState.INVENTORY_RESERVED.name());
            }

            case "INVENTORY_RESERVED" -> {
                log.info(
                        "Saga → INVENTORY_RESERVED, requesting payment | txId={}",
                        txId
                );
                saga.setState(SagaState.PAYMENT_COMPLETED.name());
            }

            case "PAYMENT_COMPLETED" -> {
                log.info(
                        "Saga → PAYMENT_COMPLETED, completing transaction | txId={}",
                        txId
                );
                saga.setState(SagaState.COMPLETED.name());
            }

            case "PAYMENT_FAILED" -> {
                log.warn(
                        "Saga → PAYMENT_FAILED, triggering compensation | txId={}",
                        txId
                );
                saga.setState(SagaState.FAILED.name());
            }

            case "INVENTORY_RELEASED" -> {
                log.warn(
                        "Saga → INVENTORY_RELEASED, saga FAILED | txId={}",
                        txId
                );
                saga.setState(SagaState.FAILED.name());
            }

            default -> {
                log.info(
                        "Saga ignoring state {} | txId={}",
                        event.getStatus(),
                        txId
                );
                return;
            }
        }

        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);
        redisTemplate.opsForValue().set(redisKey, saga.getState());
    }
}
