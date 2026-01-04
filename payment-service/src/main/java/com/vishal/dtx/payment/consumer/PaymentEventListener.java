package com.vishal.dtx.payment.consumer;

import com.vishal.dtx.common.model.TransactionEvent;
import com.vishal.dtx.common.saga.SagaState;
import com.vishal.dtx.payment.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentEventProducer producer;
    @KafkaListener(
            topics = "transaction-events",
            groupId = "payment-service-group"
    )
    public void onEvent(TransactionEvent event) {

        if (!"INVENTORY_RESERVED".equals(event.getStatus().toString())) {
            return;
        }

        log.info(
                "Payment Service processing payment | txId={}",
                event.getTransactionId()
        );

        // 🔥 Simulate failure (e.g. random or amount-based)
        if (event.getAmount() > 400) {

            log.warn(
                    "Payment FAILED | txId={}",
                    event.getTransactionId()
            );

            event.setStatus(SagaState.PAYMENT_FAILED);
            producer.publish(event);
            return;
        }

        // Success
        event.setStatus(SagaState.PAYMENT_COMPLETED);
        producer.publish(event);
    }

}
