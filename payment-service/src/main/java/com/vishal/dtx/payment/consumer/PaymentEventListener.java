package com.vishal.dtx.payment.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.payment.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentEventProducer producer;

    // Idempotency store
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            autoCreateTopics = "true",
            dltTopicSuffix = "-DLT"
    )
    @KafkaListener(
            topics = "transaction-events",
            groupId = "payment-service-group"
    )
    public void onEvent(TransactionEvent event) {

        if (!"INVENTORY_RESERVED".equals(event.getStatus().toString())) {
            return;
        }

        String key = event.getTransactionId() + ":" + event.getStatus();

        // ⚠️ IMPORTANT: idempotency AFTER retries succeed
        if (processed.contains(key)) {
            log.warn("Duplicate payment event ignored | {}", key);
            return;
        }

        log.info(
                "Payment Service processing payment | txId={}",
                event.getTransactionId()
        );

        // for testing DLQ logic implementation
        if (event.getAmount() == 999) {
            log.error("Payment gateway timeout | txId={}", event.getTransactionId());
            throw new RuntimeException("Payment gateway timeout");
        }

        if (event.getAmount() > 400) {
            log.warn("Payment DECLINED | txId={}", event.getTransactionId());
            event.setStatus(SagaState.valueOf("PAYMENT_FAILED"));
            producer.publish(event);
            processed.add(key);
            return;
        }

        event.setStatus(SagaState.valueOf("PAYMENT_COMPLETED"));
        producer.publish(event);
        processed.add(key);
    }
}
