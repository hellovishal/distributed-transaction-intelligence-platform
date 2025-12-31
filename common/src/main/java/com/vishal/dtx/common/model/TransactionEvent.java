package com.vishal.dtx.common.model;

import com.vishal.dtx.common.saga.SagaState;
import lombok.Data;

@Data
public class TransactionEvent {
    private String transactionId;
    private String correlationId;
    private String userId;
    private double amount;
    private SagaState status;
}
