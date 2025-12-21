package com.vishal.dtx.orchestrator.model;

import lombok.Data;

@Data
public class TransactionEvent {
    private String transactionId;
    private String userId;
    private double amount;
    private String status;
}
